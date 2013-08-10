package eu.pmsoft.sam.model

import java.util.concurrent.atomic.AtomicInteger
import java.net.URL
import com.google.inject.{Guice, PrivateModule, Injector}
import eu.pmsoft.sam.see.{InstanceProvider, SamArchitectureManagementApi}
import eu.pmsoft.sam.injection.{FreeBindingInjectionUtil, ExternalBindingController, DependenciesBindingContext}

object SamTransactionModel {

}

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


case class SamServiceInstance(instanceId: ServiceInstanceID, implementation: SamServiceImplementation, injector: Injector)

case class InjectionConfiguration(configurationRoot: InjectionConfigurationElement, configurationId: ServiceConfigurationID = ServiceConfigurationID())


sealed abstract class InjectionConfigurationElement(val contract: SamService)

case class ExternalServiceBind(override val contract: SamService, url: ServiceInstanceURL) extends InjectionConfigurationElement(contract)

case class InstanceServiceBind(override val contract: SamService, binding: Seq[InjectionConfigurationElement], headInstance: SamServiceInstance) extends InjectionConfigurationElement(contract)

object InjectionConfigurationBuilder {
  def singleInstanceBind(registry: SamArchitectureManagementApi, instance: SamServiceInstance): InjectionConfigurationElement = {
    require(instance.implementation.bindServices.isEmpty)
    val service = registry.getService(instance.implementation.contract)
    InstanceServiceBind(service, Seq.empty, instance)
  }

  def externalServiceBind(contract: SamService, url: ServiceInstanceURL): InjectionConfigurationElement = ExternalServiceBind(contract, url)

  def complexInstanceBind(registry: SamArchitectureManagementApi, instance: SamServiceInstance, bindings: Array[InjectionConfigurationElement]): InjectionConfigurationElement = {
    val requiredBindings = instance.implementation.bindServices
    val serviceMatches = requiredBindings map {
      registry.getService _
    } map {
      service => (service, bindings.find(_.contract.id == service.id))
    }
    require(serviceMatches.filter(_._2.isEmpty).isEmpty)

    val service = registry.getService(instance.implementation.contract)
    val orderedBinding = serviceMatches.map(_._2.get)
    InstanceServiceBind(service, orderedBinding, instance)
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

  def glueInjector(injectionConfiguration: InjectionConfigurationElement): Option[Injector] = {
    injectionConfiguration match {
      case b@ExternalServiceBind(contract, url) => {
        None
      }
      case i@InstanceServiceBind(contract, binding, head) => {
        val glueModule = new PrivateModule() {
          def configure() {
            val b = binder()
            b.requireExplicitBindings()
            contract.api.foreach(k => {
              FreeBindingInjectionUtil.createGlueBinding(b, k, head.injector)
              b.expose(k)
            })
          }
        }
        Some(Guice.createInjector(glueModule))
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





