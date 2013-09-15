package eu.pmsoft.sam.model

import java.util.concurrent.atomic.AtomicInteger
import java.net.URL
import com.google.inject.{Module, PrivateModule, Injector}
import eu.pmsoft.sam.see.{SamExecutionNodeApi, InstanceProvider, SamArchitectureManagementApi}
import eu.pmsoft.sam.injection.{FreeBindingInjectionUtil, ExternalBindingController, DependenciesBindingContext}

object SamTransactionModel {

  def externalReference(contractKey: SamServiceKey, url: ServiceInstanceURL): InjectionConfigurationDefinition = ExternalServiceReference(contractKey, url)

  // for java accessibility
  def definitionElement(end: ServiceHeadReference): InjectionConfigurationDefinition = definitionElement(end, Seq())

  def definitionElement(end: ServiceHeadReference, binding: Array[InjectionConfigurationDefinition]): InjectionConfigurationDefinition = definitionElement(end, binding.toSeq)

  def definitionElement(end: ServiceHeadReference, binding: Seq[InjectionConfigurationDefinition]): InjectionConfigurationDefinition = end match {
    case HeadServiceInstanceReference(head) => InstanceServiceReference(head.implementation.implKey.contract, binding, end)
    case HeadServiceImplementationReference(head) => InstanceServiceReference(head.contract, binding, end)
  }

  def buildConfiguration(registry: SamArchitectureManagementApi, nodeApi: SamExecutionNodeApi, definition: InjectionConfigurationDefinition): InjectionConfigurationElement = definition match {
    case ExternalServiceReference(contractKey, url) => InjectionConfigurationBuilder.externalServiceBind(contractKey, url)
    case InstanceServiceReference(contractKey, binding, headRef) => {
      val serviceInstance = headRef match {
        case HeadServiceInstanceReference(head) => head
        case HeadServiceImplementationReference(head) => nodeApi.createServiceInstance(head)
      }
      InjectionConfigurationBuilder.complexInstanceBind(registry, serviceInstance, binding map {
        buildConfiguration(registry, nodeApi, _)
      } toSeq)
    }
  }

}


sealed abstract class ServiceHeadReference

case class HeadServiceInstanceReference(head: SamServiceInstance) extends ServiceHeadReference

case class HeadServiceImplementationReference(head: SamServiceImplementationKey) extends ServiceHeadReference

sealed abstract class InjectionConfigurationDefinition(val contractKey: SamServiceKey)

case class ExternalServiceReference(override val contractKey: SamServiceKey, url: ServiceInstanceURL) extends InjectionConfigurationDefinition(contractKey)

case class InstanceServiceReference(override val contractKey: SamServiceKey, binding: Seq[InjectionConfigurationDefinition], headRef: ServiceHeadReference) extends InjectionConfigurationDefinition(contractKey)


object ServiceConfigurationID {
  private val counter: AtomicInteger = new AtomicInteger(0)

  def apply() = new ServiceConfigurationID(counter.addAndGet(1))
}

class ServiceConfigurationID(val id: Int)

case class LiftedServiceConfiguration(url: ServiceInstanceURL, configId: ServiceConfigurationID)

object ServiceTransactionID {
  private val counter: AtomicInteger = new AtomicInteger(0)

  def apply() = new ServiceTransactionID(counter.addAndGet(1))
}

class ServiceTransactionID(val id: Int)

case class ServiceInstanceURL(val url: URL)

case class ExposedServiceTransaction(url: ServiceInstanceURL, contract: SamServiceKey)

case class SamServiceInstance(instanceId: ServiceInstanceID, implementation: SamServiceImplementation, injector: Injector)

case class InjectionConfiguration(configurationRoot: InjectionConfigurationElement, configurationId: ServiceConfigurationID = ServiceConfigurationID())


sealed abstract class InjectionConfigurationElement(val contractKey: SamServiceKey)

case class ExternalServiceBind(override val contractKey: SamServiceKey, url: ServiceInstanceURL) extends InjectionConfigurationElement(contractKey)

case class InstanceServiceBind(override val contractKey: SamServiceKey, binding: Seq[InjectionConfigurationElement], headInstance: SamServiceInstance) extends InjectionConfigurationElement(contractKey)


object InjectionConfigurationBuilder {

  def singleInstanceBind(registry: SamArchitectureManagementApi, instance: SamServiceInstance): InjectionConfigurationElement = {
    require(instance.implementation.bindServices.isEmpty)
    val service = registry.getService(instance.implementation.implKey.contract)
    InstanceServiceBind(service.id, Seq.empty, instance)
  }

  def externalServiceBind(contract: SamServiceKey, url: ServiceInstanceURL): InjectionConfigurationElement = ExternalServiceBind(contract, url)

  def complexInstanceBind(registry: SamArchitectureManagementApi, instance: SamServiceInstance, bindings: Array[InjectionConfigurationElement]): InjectionConfigurationElement = {
    complexInstanceBind(registry, instance, bindings.toSeq)
  }

  def complexInstanceBind(registry: SamArchitectureManagementApi, instance: SamServiceInstance, bindings: Seq[InjectionConfigurationElement]): InjectionConfigurationElement = {
    val requiredBindings = instance.implementation.bindServices
    def errorMsg = s"Service instance implementation require services $requiredBindings, but provided is $bindings"
    require(requiredBindings.size == bindings.size, errorMsg)
    val serviceMatches = requiredBindings map {
      registry.getService _
    } map {
      service => (service, bindings.find(_.contractKey == service.id))
    }

    require(serviceMatches.filter(_._2.isEmpty).isEmpty, errorMsg)

    val service = registry.getService(instance.implementation.implKey.contract)
    val orderedBinding = serviceMatches.map(_._2.get)
    InstanceServiceBind(service.id, orderedBinding, instance)
  }

}

trait InjectionTransactionContext {
  def instanceProvider(externalBind: ExternalServiceBind): InstanceProvider

  def instanceProvider(injector: Injector): InstanceProvider

  def dependenciesBindContextCreator(bindings: Seq[InstanceProvider]): DependenciesBindingContext
}

object InjectionTransaction {

  def createNode(context: InjectionTransactionContext, injectionConfiguration: InjectionConfigurationElement): InjectionTransactionNode = {
    injectionConfiguration match {
      case b@ExternalServiceBind(contract, url) => {
        InjectionTransactionNode(b,
          context.instanceProvider(b),
          Seq.empty,
          context.dependenciesBindContextCreator _,
          None)
      }
      case i@InstanceServiceBind(contract, binding, head) => {
        val controller = head.injector.getInstance(classOf[ExternalBindingController])
        InjectionTransactionNode(i,
          context.instanceProvider(head.injector),
          binding.map(b => createNode(context, b)),
          context.dependenciesBindContextCreator _,
          Some(controller))
      }
    }
  }

  def glueInjector(registry: SamArchitectureManagementApi, injectionConfiguration: InjectionConfigurationElement): Option[Module] = {
    injectionConfiguration match {
      case b@ExternalServiceBind(contract, url) => {
        None
      }
      case i@InstanceServiceBind(contractKey, binding, head) => {
        val service = registry.getService(contractKey)
        val subModules = binding map {
          glueInjector(registry, _)
        } filter (_.isDefined) map (_.get)
        val glueModule = new PrivateModule() {
          def configure() {
            val b = binder()
            b.requireExplicitBindings()
            subModules.foreach(b.install(_))
            service.api.foreach(k => {
              require(k != null)
              FreeBindingInjectionUtil.createGlueBinding(b, k, head.injector)
              b.expose(k)
            })
          }
        }
        Some(glueModule)
      }
    }
  }

  def getExternalBind(injectionConfiguration: InjectionConfigurationElement): Seq[ExternalServiceBind] = {
    injectionConfiguration match {
      case b@ExternalServiceBind(contract, url) => {
        Seq(b)
      }
      case i@InstanceServiceBind(contract, binding, head) => {
        binding.toSeq.flatMap(getExternalBind _)
      }
    }
  }


}

case class InjectionTransactionNode(configurationElement: InjectionConfigurationElement,
                                    instanceProvider: InstanceProvider,
                                    serviceBinding: Seq[InjectionTransactionNode],
                                    externalInstanceProviderCreator: Seq[InstanceProvider] => DependenciesBindingContext,
                                    controller: Option[ExternalBindingController]) {

  private lazy val serviceBind: DependenciesBindingContext = externalInstanceProviderCreator(serviceBinding.map(_.instanceProvider))

  def bindSwitchingScope: Unit = {
    serviceBinding foreach (_.bindSwitchingScope)
    controller.map(_.bindRecordContext(serviceBind))
  }

  def unbindSwitchingScope: Unit = {
    serviceBinding foreach (_.unbindSwitchingScope)
    controller.map(_.unBindRecordContext())
  }
}

class InjectionTransaction(val transactionInjector: Injector, val rootNode: InjectionTransactionNode, val tid: ServiceTransactionID = ServiceTransactionID())





