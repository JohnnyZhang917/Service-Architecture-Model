package eu.pmsoft.sam.see

import eu.pmsoft.sam.model._
import com.google.inject.util.Modules
import com.google.inject._
import javax.inject.Inject
import eu.pmsoft.sam.model.SIID
import eu.pmsoft.sam.model.SamServiceImplementation
import eu.pmsoft.sam.model.SamServiceKey
import eu.pmsoft.sam.model.SamArchitecture
import eu.pmsoft.sam.model.SamService
import eu.pmsoft.sam.model.SamServiceInstance
import eu.pmsoft.sam.injection.{FreeBindingInjectionUtil, ExternalBindingController, ExtrenalBindingInfrastructureModule}
import scala.collection.mutable

object ServiceExecutionEnvironment {

  lazy val productionModule = Modules.combine()
}

class ServiceExecutionEnvironmentModule extends AbstractModule {

  def configure() {
    bind(classOf[SamArchitectureManagement]).to(classOf[SamArchitectureManagementImpl]).asEagerSingleton()
    bind(classOf[SamServiceRegistry]).to(classOf[SamServiceRegistryImpl]).asEagerSingleton()
    bind(classOf[SamExecutionNode]).to(classOf[SamExecutionNodeImpl])
  }

}


trait SamArchitectureManagement {
  def registerArchitecture(architecture: SamArchitecture):Unit
  def getService(key : SamServiceKey):SamService
}

trait SamServiceRegistry {
  def registerImplementations(implementationPackage: Set[SamServiceImplementation])
  def getImplementation(key : SamServiceImplementationKey) : SamServiceImplementation
}

trait SamExecutionNode {
  def createServiceInstance(key : SamServiceImplementationKey) : SamServiceInstance
  def getServiceInstances(key : SamServiceImplementationKey) : Set[SIID]
  def getInstance(id: SIID) : SamServiceInstance

  def setupInjectionTransaction(element: InjectionElement) : STID
  def getTransaction(tid: STID) : InjectionTransaction

  def liftServiceTransaction(tis: STID) : SIURL
}

class SamArchitectureManagementImpl extends SamArchitectureManagement {
  // TODO change to val after component seal
  var serviceByKey : Map[SamServiceKey,SamService] = Map.empty
  def registerArchitecture(architecture: SamArchitecture) = {
    serviceByKey = serviceByKey ++ architecture.categories.flatMap( c => c.services).map( s => s.id -> s)
  }

  def getService(key: SamServiceKey) = serviceByKey.get(key).get
}

class SamServiceRegistryImpl extends SamServiceRegistry {

  var register : Map[SamServiceImplementationKey,SamServiceImplementation] = Map.empty

  def registerImplementations(implementationPackage: Set[SamServiceImplementation]) = {
    register = register ++ ( implementationPackage map ( s => s.implKey -> s))
  }

  def getImplementation(key : SamServiceImplementationKey) : SamServiceImplementation = {
    register.getOrElse(key, null)
  }

}

class SamExecutionNodeImpl @Inject() extends SamExecutionNode {

  @Inject var serviceRegistry : SamServiceRegistry = null

  @Inject var architectureManager : SamArchitectureManagement = null

  val instanceRunning : mutable.Map[SIID,SamServiceInstance] = mutable.Map.empty

  def createServiceInstance(key: SamServiceImplementationKey): SamServiceInstance = {
    val implementation = serviceRegistry.getImplementation(key)
    val instanceInjector : Injector = createServiceInjector(implementation);
    val instance = SamServiceInstance(SIID(), implementation, instanceInjector)
    instanceRunning.update(instance.instanceId,instance)
    instance
  }

  private def createServiceInjector(implementation: SamServiceImplementation) : Injector = {
    val moduleInstance = implementation.implKey.module.newInstance()
    val serviceContracts : Set[Set[Key[_]]] = implementation.bindServices.map( architectureManager.getService _ ).map( _.api)
    Guice.createInjector(moduleInstance, FreeBindingBuilder.buildFreeBindingModule(serviceContracts))
  }

  def getServiceInstances(key: SamServiceImplementationKey): Set[SIID] = {
    instanceRunning.filter( _._2.implementation.implKey == key).map( _._1).toSet
  }

  def getInstance(id: SIID): SamServiceInstance = instanceRunning.getOrElse(id,null)

  val transactions : mutable.Map[STID,InjectionTransaction] = mutable.Map.empty

  def setupInjectionTransaction(element: InjectionElement): STID = {
    val transaction = InjectionTransaction(element)
    transactions.update(transaction.tid,transaction)
    transaction.tid
  }

  def getTransaction(tid: STID): InjectionTransaction = transactions.getOrElse(tid,null)

  val lifting : mutable.Map[STID,SIURL] = mutable.Map.empty
  def liftServiceTransaction(tis: STID): SIURL = {
    lifting.getOrElseUpdate(tis,SIURL(new java.net.URL("http","localhost","service" + tis.id)))
  }
}


private object FreeBindingBuilder {

  def buildFreeBindingModule(contracts: Set[Set[Key[_]]]) : Module = {
    new PrivateModule {
      def configure() {
        val b =binder()
        b.requireExplicitBindings()
        for {
          service <- contracts
          key <- service
        } yield FreeBindingInjectionUtil.createIntermediateProvider(b,key)
        install(new ExtrenalBindingInfrastructureModule());
        expose(classOf[ExternalBindingController]);
      }

    }
  }
}