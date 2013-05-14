package eu.pmsoft.sam.model

import com.google.inject.{PrivateModule, Guice, Injector, Key}
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract
import java.net.URL
import eu.pmsoft.sam.see._
import java.util.concurrent.atomic.AtomicInteger
import eu.pmsoft.sam.injection.{FreeBindingInjectionUtil, ExternalInstanceProvider, ExternalBindingController}
import scala.Some

sealed abstract class ArchitectureModel

case class SamServiceKey(signature: ServiceContract)

case class SamServiceImplementationKey(module: GuiceModule)


case class SamService(id: SamServiceKey, api: Set[Key[_]]) extends ArchitectureModel

case class SamCategory(id: String, services: Set[SamService]) extends ArchitectureModel

case class SamArchitecture(
                            categories: Set[SamCategory],
                            accessMap: Map[String, Set[String]]
                            ) extends ArchitectureModel


case class SamServiceImplementation(implKey: SamServiceImplementationKey,
                                    contract: SamServiceKey,
                                    bindServices: Set[SamServiceKey]
                                     ) extends ArchitectureModel

object SIID {
  private val counter: AtomicInteger = new AtomicInteger(0)

  def apply() = new SIID(counter.addAndGet(1))
}

class SIID(val id: Int)

object STID {
  private val counter: AtomicInteger = new AtomicInteger(0)

  def apply() = new STID(counter.addAndGet(1))
}

class STID(val id: Int)

object SIURL {
  def apply(url: URL) = new SIURL(url)
}

class SIURL(val url: URL)

case class SamServiceInstance(instanceId: SIID, implementation: SamServiceImplementation, injector: Injector)

sealed abstract class InjectionElement(val contract: SamService)

case class ExternalServiceBind(override val contract: SamService, url: SIURL) extends InjectionElement(contract)

case class InstanceServiceBind(override val contract: SamService, binding: Set[InjectionElement], headInstance: SamServiceInstance) extends InjectionElement(contract)

object InjectionTransactionBuilder {
  def singleInstanceBind(registry: SamArchitectureManagement, instance: SamServiceInstance): InjectionElement = {
    require(instance.implementation.bindServices.isEmpty)
    val service = registry.getService(instance.implementation.contract)
    InstanceServiceBind(service, Set.empty, instance)
  }

  def externalServiceBind(contract: SamService, url: SIURL): InjectionElement = ExternalServiceBind(contract, url)

  def complexInstanceBind(registry: SamArchitectureManagement, instance: SamServiceInstance, bindings: Array[InjectionElement]): InjectionElement = {
    val requiredBindings = instance.implementation.bindServices
    val serviceMatches = requiredBindings map {
      registry.getService _
    } map {
      service => (service, bindings.find(_.contract.id == service.id))
    }
    require(serviceMatches.filter(_._2.isEmpty).isEmpty)

    val service = registry.getService(instance.implementation.contract)
    InstanceServiceBind(service, bindings.toSet, instance)
  }

}

object InjectionTransaction {
  def apply(injectionConfiguration: InjectionElement): InjectionTransaction = {
    val headInjector = glueInjector(injectionConfiguration)
    require(headInjector.isDefined)
    val context = TransactionRecordContext(getExternalBind(injectionConfiguration))
    val rootNode = createNode(context, injectionConfiguration)
    new InjectionTransaction(headInjector.get, rootNode)
  }

  private def glueInjector(injectionConfiguration: InjectionElement): Option[Injector] = {
    injectionConfiguration match {
      case b@ExternalServiceBind(contract, url) => {
        None
      }
      case i@InstanceServiceBind(contract, binding, head) => {
       val glueModule = new PrivateModule(){
          def configure() {
            val b = binder()
            b.requireExplicitBindings()
            contract.api.foreach( k => {
              FreeBindingInjectionUtil.createGlueBinding(b,k,head.injector)
              b.expose(k)
            })
          }
        }
       Some( Guice.createInjector(glueModule))
      }
    }
  }

  private def getExternalBind(injectionConfiguration: InjectionElement): Set[ExternalServiceBind] = {
    injectionConfiguration match {
      case b@ExternalServiceBind(contract, url) => {
        Set(b)
      }
      case i@InstanceServiceBind(contract, binding, head) => {
        binding.flatMap(getExternalBind _)
      }
    }
  }

  private def createNode(context: TransactionRecordContext, injectionConfiguration: InjectionElement): InjectionTransactionNode = {
    injectionConfiguration match {
      case b@ExternalServiceBind(contract, url) => {
        InjectionTransactionNode(b, context.get(b), Set.empty, None)
      }
      case i@InstanceServiceBind(contract, binding, head) => {
        val controller = head.injector.getInstance(classOf[ExternalBindingController])
        InjectionTransactionNode(i, new DirectServiceInstanceProvider(head.injector), binding.map(b => createNode(context, b)), Some(controller))
      }
    }
  }
}


case class InjectionTransactionNode(configurationElement: InjectionElement,
                                    instanceProvider: InstanceProvider,
                                    serviceBinding: Set[InjectionTransactionNode],
                                    controller: Option[ExternalBindingController]) {

  private lazy val serviceBind: ExternalInstanceProvider = {
    val instanceAndApiKeys: Set[(Set[Key[_]], MemoizedInstanceProvider)] = serviceBinding.map(s => (s.configurationElement.contract.api, new MemoizedInstanceProvider(s.instanceProvider)))
    val contractMap: Map[Key[_], MemoizedInstanceProvider] = instanceAndApiKeys.flatMap(keysetToTarget => keysetToTarget._1.map(key => key -> keysetToTarget._2)).toMap
    new InstanceProviderTransactionContext(contractMap)
  }

  def bindTransaction: Unit = {
    serviceBinding foreach (_.bindTransaction)
    controller.map(_.bindRecordContext(serviceBind))
  }

  def unbindTransaction: Unit = {
    serviceBinding foreach (_.unbindTransaction)
    controller.map(_.unBindRecordContext())
  }
}

class InjectionTransaction(val transactionInjector: Injector,val rootNode: InjectionTransactionNode, val tid: STID = STID())


case class SEEConfiguration(
                             architectures: Set[SamArchitectureDefinition],
                             implementations: Set[SamServiceImplementationPackageContract])

case class SEENodeConfiguration()