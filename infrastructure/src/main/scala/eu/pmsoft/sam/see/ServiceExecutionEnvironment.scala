/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.pmsoft.sam.see


import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.collection.concurrent
import com.google.inject._
import org.slf4j.LoggerFactory
import java.net.URL
import eu.pmsoft.sam.model._
import eu.pmsoft.sam.injection.{FreeBindingInjectionUtil, ExternalBindingController, ExtrenalBindingInfrastructureModule}
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract
import eu.pmsoft.sam.execution.ServiceAction
import eu.pmsoft.sam.idgenerator.LongLongIdGenerator

import eu.pmsoft.sam.definition.service.SamServiceDefinition
import scala.Predef._
import eu.pmsoft.sam.model.InjectionConfiguration
import eu.pmsoft.sam.model.SamService
import scala.Some
import eu.pmsoft.sam.model.ExposedServiceTransaction
import eu.pmsoft.sam.model.SamServiceImplementation
import eu.pmsoft.sam.model.ServiceInstanceURL
import eu.pmsoft.sam.model.LiftedServiceConfiguration
import eu.pmsoft.sam.model.SamServiceKey
import eu.pmsoft.sam.model.SamServiceInstance
import eu.pmsoft.sam.model.ExternalServiceBind
import eu.pmsoft.sam.model.SEEConfigurationBuilder
import eu.pmsoft.sam.model.SEEConfiguration
import eu.pmsoft.sam.model.SamServiceImplementationKey
import scala.util.{Failure, Success}
import scala.collection.concurrent.TrieMap
import scala.annotation.tailrec

object ServiceExecutionEnvironment {

  def apply(configuration: SEEConfiguration) = new ServiceExecutionEnvironment(configuration)

  def configurationBuilder(port: Int) = SEEConfigurationBuilder(SEEConfiguration(Set(), Set(), port))
}

class ServiceExecutionEnvironment(val configuration: SEEConfiguration) extends EnvironmentExternalApiLogic with SamInjectionTransactionApi {
  override val logger = LoggerFactory.getLogger(this.getClass)
  private[this] val status = new ServiceExecutionEnvironmentStatus(configuration.implementations, configuration.architectures)
  private[this] val server = new CanonicalTransportServer(this, configuration.port)
  private[this] val architectureManager: SamArchitectureManagementApi = new SamArchitectureManagement(status)
  private[this] val transaction = new SamInjectionTransaction(architectureManager, status, server)
  // Visible api
  val serviceRegistry: SamServiceRegistryApi = new SamServiceRegistry(status)
  val architectureManagerApi: SamArchitectureManagementApi = architectureManager
  val executionNode: SamExecutionNodeApi = new SamExecutionNode(serviceRegistry, architectureManager, server.createUrl _, status)
  val transactionApi: SamInjectionTransactionApi = this
  val externalConnector: SamEnvironmentExternalConnector = server

  lazy val clientApiModule: Module = new AbstractModule() {
    def configure() {
      bind(classOf[SamArchitectureManagementApi]).toInstance(architectureManager)
      bind(classOf[SamServiceRegistryApi]).toInstance(serviceRegistry)
      bind(classOf[SamExecutionNodeApi]).toInstance(executionNode)
      bind(classOf[SamInjectionTransactionApi]).toInstance(transactionApi)
      bind(classOf[SamEnvironmentExternalConnector]).toInstance(externalConnector)
    }
  }

  def getArchitectureInfo(): Future[String] = Future {
    status.architectures map {
      arch => arch.signature
    } mkString (":")
  }

  def getExposedServiceTransaction(): Future[Seq[ExposedServiceTransaction]] = Future {
    status.getExposedServiceConfigurations.values map {
      liftId =>
        ExposedServiceTransaction(liftId.url, status.getConfigurations(liftId.configId).configurationRoot.contractKey)
    } toSeq
  }

  private val bindIdGenerator: LongLongIdGenerator = LongLongIdGenerator.createGenerator()

  def registerTransaction(globalID: GlobalTransactionIdentifier, clientPipeReference: PipeReference, serviceURL: ServiceInstanceURL, setupPendingMode: Boolean): Future[TransactionRegistrationResult] = {
    val serviceConfiguration = status.getExposedServiceConfigurations(serviceURL)
    val transactionContextBindID = ThreadExecutionIdentifier(bindIdGenerator.getNextID)
    bindNestedTransaction(globalID, transactionContextBindID, clientPipeReference, serviceConfiguration) map {
      tb =>
        val serviceTransaction = transaction.createTransactionContext(serviceConfiguration, tb)
        val executor = ThreadExecutionModel.openTransactionThread(transactionContextBindID, serviceTransaction)
        val executorID = status.linkGlobalTransaction(globalID, transactionContextBindID, executor)
        if (setupPendingMode) {
          executor.startPendingThreadForExecution()
        }
        TransactionRegistrationResult(globalID,executorID, PipeReference(executorID, tb.localHeadPipeId, serviceURL))
    }

  }

  def unregisterTransaction(tid: GlobalTransactionIdentifier): Future[Boolean] = Future {
    status.unlinkGlobalTransaction(tid)
  }

  val transactionIdGenerator = LongLongIdGenerator.createGenerator()
  val pipeIdGenerator = LongLongIdGenerator.createGenerator()

  private def bindNestedTransaction(globalID: GlobalTransactionIdentifier, tid: ThreadExecutionIdentifier, clientPipeReference: PipeReference, configuration: LiftedServiceConfiguration): Future[TransactionBinding] = {
    val serviceConf: InjectionConfiguration = status.getConfigurations(configuration.configId)
    val externalBind = InjectionTransaction.getExternalBind(serviceConf.configurationRoot)
    val localHeadPipeID = PipeIdentifier(pipeIdGenerator.getNextID)

    val externalServiceRef = externalBind map {
      case e@ExternalServiceBind(contract, url) => {
        (ExposedServiceTransaction(url, contract), PipeReference(tid, PipeIdentifier(pipeIdGenerator.getNextID), configuration.url))
      }
    }

    val externalBinds = externalServiceRef map {
      case (exposed, connectionPipeRef) => {
        server.getConnectionFromServiceURL(exposed.url).registerTransactionRemote(globalID, connectionPipeRef, exposed)
      }
    }
    val internalBind = externalServiceRef map {
      case (exposed, connectionPipeRef) => exposed.url -> connectionPipeRef.pipeID
    } toMap


    Future.fold(externalBinds)(Map[ServiceInstanceURL, PipeReference]())((data, registration) => data + (registration.surl -> registration.transactionHeadPipeRef)) map {
      externalBind => TransactionBinding(globalID, tid, localHeadPipeID, clientPipeReference, internalBind, externalBind)
    }

  }


  def executeServiceAction[R, T](configuration: LiftedServiceConfiguration, action: ServiceAction[R, T]): Future[R] = {
    val globalTransactionID = GlobalTransactionIdentifier(transactionIdGenerator.getNextID)
    val tid = ThreadExecutionIdentifier(transactionIdGenerator.getNextID)
    val fakePipeRef = PipeReference(tid, PipeIdentifier(pipeIdGenerator.getNextID), configuration.url)
    registerTransaction(globalTransactionID, fakePipeRef, configuration.url, false) flatMap {
      registration =>
        val executor = status.getTransactionContextById(registration.transactionID).get
        val actionResultF = executor.executeServiceAction(action)
        actionResultF.onComplete { case _ =>  status.unlinkGlobalTransaction(registration.globalTransactionID) }
        actionResultF
    }
  }

  def dispatchMessage(routingInfo: MessageRoutingInformation): Future[Unit] = {
    logger.trace("dispatch message for routingInfo {}", routingInfo)
    val executor = status.getTransactionContextById(routingInfo.remoteTargetPipeRef.transactionBindID).get
    executor.dispatchMessage(routingInfo)
  }
}


trait EnvironmentExternalApiLogic {

  def getArchitectureInfo(): Future[String]

  def getExposedServiceTransaction(): Future[Seq[ExposedServiceTransaction]]

  def registerTransaction(globalID: GlobalTransactionIdentifier, headPipeReference: PipeReference, serviceURL: ServiceInstanceURL, setupPendingMode: Boolean): Future[TransactionRegistrationResult]

  def unregisterTransaction(tid: GlobalTransactionIdentifier): Future[Boolean]

  def dispatchMessage(routingInfo: MessageRoutingInformation): Future[Unit]

  val logger = LoggerFactory.getLogger(this.getClass)

  def handle(action: SamEnvironmentAction): Future[SamEnvironmentResult] = {
    import SamEnvironmentCommandType._
    import ProtocolTransformations._
    logger.trace("handle action {}", action.`command`)

    val result = SamEnvironmentResult.getDefaultInstance.copy(`command` = action.`command`)
    val fres = action.`command` match {
      case ARCHITECTURE_INFO => {
        getArchitectureInfo() map {
          info => result.setArchitectureInfoSignature(info)
        }
      }
      case PING => Future {
        result
      }
      case SERVICE_LIST => {
        getExposedServiceTransaction() map {
          services => result.addAllServiceList(services.map(mapExposedTo _))
        }
      }
      case REGISTER_TRANSACTION => {
        try {
          val regData = action.`registration`.get
          val hp = regData.`headPipeReference`
          val pipeRef: PipeReference = PipeReference(ThreadExecutionIdentifier(hp.`transactionBindId`), PipeIdentifier(hp.`pipeRefId`), createUrl(hp.`address`))
          val bindIdFut = registerTransaction(GlobalTransactionIdentifier(regData.`transactionGlobalId`), pipeRef, createUrl(regData.`serviceReference`.`url`), true)
          val res = bindIdFut map {
            registration =>
              logger.debug("transaction registered as {}", registration)
              val headPipeRef = PipeReferenceProto.getDefaultInstance.copy(registration.transactionID.tid, registration.transactionHeadPipeReference.pipeID.pid, registration.transactionHeadPipeReference.address.url.toExternalForm)
              val confirmation = SamTransactionRegistrationConfirmation.getDefaultInstance.copy(registration.transactionID.tid, headPipeRef)
              result.setRegistrationConfirmation(confirmation)
          }

          res onComplete {
            case Success(v) => logger.debug("registration done")
            case Failure(t) => logger.error("Error on registration {}", t)
          }
          res
        } catch {
          case e: Throwable => logger.error("error {}", e); ???
        }
      }
      case UNREGISTER_TRANSACTION => {
        unregisterTransaction(GlobalTransactionIdentifier(action.`transactionGlobalId`.get)) map {
          ok => result.setUnregisterOk(ok)
        }
      }
      case PROTOCOL_EXECUTION_MESSAGE => {
        try {
          val routing = ThreadMessageToProtoMapper.fromProto(action.`message`.get, createUrl _)
          dispatchMessage(routing) map {
            done =>
              result
          }
        } catch {
          case e: Throwable => logger.error("error {}", e); e.printStackTrace(); ???
        }

      }
      case _ => ???
    }
    fres
  }

  // FIXME: security check and validation
  def createUrl(urlStr: String): ServiceInstanceURL = ServiceInstanceURL(new URL(urlStr))

  def findServiceContract(contractStr: String): SamServiceKey = {
    val loadClass: Class[_] = this.getClass.getClassLoader.loadClass(contractStr)
    SamServiceKey(loadClass.asSubclass(classOf[SamServiceDefinition]))
  }

}


private class SamArchitectureManagement(val status: ServiceExecutionEnvironmentStatus) extends SamArchitectureManagementApi {

  def getService(key: SamServiceKey) = status.serviceByKey(key)
}

private class SamServiceRegistry(val status: ServiceExecutionEnvironmentStatus) extends SamServiceRegistryApi {

  def getImplementation(key: SamServiceImplementationKey): SamServiceImplementation = {
    status.implementationRegistry.getOrElse(key, null)
  }

}

private class SamExecutionNode(val serviceRegistry: SamServiceRegistryApi,
                               val architectureManager: SamArchitectureManagementApi,
                               val urlCreator: ServiceConfigurationID => ServiceInstanceURL,
                               val status: ServiceExecutionEnvironmentStatus) extends SamExecutionNodeApi {

  val logger = LoggerFactory.getLogger(this.getClass)

  def createServiceInstance(key: SamServiceImplementationKey): SamServiceInstance = {
    logger.trace("createServiceInstance {}", key)
    val implementation = serviceRegistry.getImplementation(key)
    val instanceInjector: Injector = createServiceInjector(implementation)
    val instance = SamServiceInstance(ServiceInstanceID(), implementation, instanceInjector)
    status.addServiceInstance(instance)
    instance
  }

  private def createServiceInjector(implementation: SamServiceImplementation): Injector = {
    logger.trace("createServiceInjector {}", implementation)
    val moduleInstance = implementation.implKey.module.newInstance()
    val serviceContracts: Seq[Set[Key[_]]] = implementation.bindServices.map(architectureManager.getService _).map(_.api)
    Guice.createInjector(moduleInstance, FreeBindingBuilder.buildFreeBindingModule(serviceContracts))
  }

  def getServiceInstances(key: SamServiceImplementationKey): Set[ServiceInstanceID] = {
    status.getRunningServiceInstances.filter(_._2.implementation.implKey == key).map(_._1).toSet
  }

  def getInstance(id: ServiceInstanceID): SamServiceInstance = status.getRunningServiceInstances.getOrElse(id, null)

  def registerInjectionConfiguration(element: InjectionConfigurationElement): ServiceConfigurationID = {
    logger.trace("registerInjectionConfiguration {}", element.contractKey)
    val configuration = InjectionConfiguration(element)
    status.addInjectionConfiguration(configuration)
    configuration.configurationId
  }

  def liftServiceConfiguration(configurationId: ServiceConfigurationID): LiftedServiceConfiguration = {
    // FIXME validate external configuration
    val lifted = status.exposeServiceConfiguration(configurationId, urlCreator)
    logger.trace("lift of service on URL: {} ", lifted.url)
    lifted
  }
}


private object FreeBindingBuilder {

  def buildFreeBindingModule(contracts: Seq[Set[Key[_]]]): Module = {
    new PrivateModule {
      def configure() {
        val b = binder()
        b.requireExplicitBindings()
        contracts.zipWithIndex.foreach {
          case (service, slotNr) => service.foreach {
            key => FreeBindingInjectionUtil.createIntermediateProvider(b, key, slotNr)
          }
        }
        install(new ExtrenalBindingInfrastructureModule())
        expose(classOf[ExternalBindingController])
      }
    }
  }
}

private class ServiceExecutionEnvironmentStatus(val implementationContracts: Set[SamServiceImplementationPackageContract],
                                                val architectureDefinitions: Set[SamArchitectureDefinition]) {
  private val instanceRunning: concurrent.Map[ServiceInstanceID, SamServiceInstance] = new TrieMap()
  private val configurations: concurrent.Map[ServiceConfigurationID, InjectionConfiguration] = new TrieMap()
  private val exposedServicesUrl: concurrent.Map[ServiceConfigurationID, LiftedServiceConfiguration] = new TrieMap()
  private val exposedServiceConfigurations: concurrent.Map[ServiceInstanceURL, LiftedServiceConfiguration] = new TrieMap()
  val logger = LoggerFactory.getLogger(this.getClass)

  // Registration of implementations
  val implementationRegistry: Map[SamServiceImplementationKey, SamServiceImplementation] = Map() ++ {
    implementationContracts map {
      SamModelBuilder.loadImplementationPackage _
    } flatMap {
      _ map (s => s.implKey -> s)
    }
  }

  val architectures = architectureDefinitions map {
    SamModelBuilder.loadArchitectureDefinition _
  }

  val serviceByKey: Map[SamServiceKey, SamService] = Map() ++ {
    architectures flatMap {
      _.categories.flatMap(c => c.services).map(s => s.id -> s)
    }
  }

  def addServiceInstance(instance: SamServiceInstance) = instanceRunning.update(instance.instanceId, instance)

  def addInjectionConfiguration(configuration: InjectionConfiguration) = configurations.update(configuration.configurationId, configuration)

  def exposeServiceConfiguration(configurationId: ServiceConfigurationID, urlCreator: ServiceConfigurationID => ServiceInstanceURL): LiftedServiceConfiguration = {
    val exposed = exposedServicesUrl.get(configurationId) match {
      case Some(url) => url
      case None => {
        val url = urlCreator(configurationId)
        val lifted = LiftedServiceConfiguration(url, configurationId)
        exposedServicesUrl.put(configurationId, lifted)
        exposedServiceConfigurations.put(url, lifted)
        lifted
      }
    }
    exposed
  }

  def getRunningServiceInstances = instanceRunning

  def getConfigurations = configurations

  def getExposedServicesUrl = exposedServicesUrl

  def getExposedServiceConfigurations = exposedServiceConfigurations

  private val linkedTransaction: concurrent.Map[GlobalTransactionIdentifier, Set[ThreadExecutionIdentifier]] = new TrieMap()
  private val bindTransactionMap: concurrent.Map[ThreadExecutionIdentifier, TransactionThreadStatus] = new TrieMap()

  @tailrec
  private def updateGlobalTransactionSet(globalTransactionID: GlobalTransactionIdentifier, bindId: ThreadExecutionIdentifier) : Unit = {
    val old = linkedTransaction.getOrElseUpdate(globalTransactionID,Set())
    if(! linkedTransaction.replace(globalTransactionID,old,old + bindId) ) {
      updateGlobalTransactionSet(globalTransactionID,bindId)
    }

  }

  def linkGlobalTransaction(globalTransactionID: GlobalTransactionIdentifier, bindId: ThreadExecutionIdentifier, context: TransactionThreadStatus): ThreadExecutionIdentifier = {
    logger.trace("bind global transaction {}",globalTransactionID.globalID)
    logger.trace("bind context {}",bindId.tid)
    bindTransactionMap.putIfAbsent(bindId, context)
    updateGlobalTransactionSet(globalTransactionID,bindId)
    bindId
  }

  def unlinkGlobalTransaction(globalTransactionID: GlobalTransactionIdentifier): Boolean = {
    logger.trace("unbind transaction {}",globalTransactionID.globalID)
    linkedTransaction.getOrElse(globalTransactionID, Set()) foreach {
      bid => {
        logger.trace("unbind context {}",bid.tid)
        bindTransactionMap.remove(bid).map {
          threadStatus => threadStatus.exitPendingMode()
        }
      }
    }
    true
  }

  def getTransactionContextById(transactionBindId: ThreadExecutionIdentifier): Option[TransactionThreadStatus] = bindTransactionMap.get(transactionBindId)
}

private class SamInjectionTransaction(
                                       val architectureManager: SamArchitectureManagementApi,
                                       val status: ServiceExecutionEnvironmentStatus,
                                       val connector: SamEnvironmentExternalConnector
                                       ) {

  val logger = LoggerFactory.getLogger(this.getClass)

  def createTransactionContext(configuration: LiftedServiceConfiguration, transactionBinding: TransactionBinding): InjectionTransactionAccessApi = {
    val config = status.getConfigurations(configuration.configId)
    val transportContext = createTransactionTransportContext(config, transactionBinding)
    val context = CanonicalRecordingLayer(architectureManager, config.configurationRoot, transportContext)
    logger.trace("created context for configuration {}", config.configurationRoot)
    context
  }

  private def createTransactionTransportContext(config: InjectionConfiguration, transactionBinding: TransactionBinding): TransactionTransportContext = {
    val externalBind = InjectionTransaction.getExternalBind(config.configurationRoot)
    val externalPipes = externalBind map {
      case e@ExternalServiceBind(_, url) => {
        (e, connector.openExecutionPipe(transactionBinding.internalBind.get(url).get, transactionBinding.externalBinds.get(url).get))
      }
    }

    val inputPipe = connector.openLoopbackPipe(transactionBinding.localHeadPipeId, transactionBinding.clientPipeReference)
    val externalPipesMap: Map[PipeIdentifier, TransportPipe[ThreadMessage]] = externalPipes map {
      case (e, pipe) => pipe.getPipeID -> pipe
    } toMap

    new TransactionTransportContext(externalPipesMap, externalPipes, inputPipe)
  }

}

