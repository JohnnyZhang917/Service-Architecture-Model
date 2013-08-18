package eu.pmsoft.sam.see

import eu.pmsoft.sam.model._
import eu.pmsoft.sam.model.SamTransactionModel._
import eu.pmsoft.see.api.data.architecture.SeeTestArchitecture
import eu.pmsoft.see.api.data.impl.TestImplementationDeclaration
import java.util.concurrent.atomic.AtomicInteger
import eu.pmsoft.sam.model.HeadServiceInstanceReference
import eu.pmsoft.sam.model.ServiceInstanceURL
import eu.pmsoft.sam.model.SamServiceImplementationKey
import eu.pmsoft.sam.execution.ServiceAction
import scala.concurrent.Future

trait SamTestUtil {

  def liftDefinition(env: ServiceExecutionEnvironment, definition: InjectionConfigurationDefinition) = {
    val configuration = buildConfiguration(env.architectureManager, env.executionNode, definition)
    val externalConfigId = env.executionNode.registerInjectionConfiguration(configuration)
    env.transaction.liftServiceConfiguration(externalConfigId)
  }

  def executeTestAction[T](env: ServiceExecutionEnvironment, definition: InjectionConfigurationDefinition, action : ServiceAction[Boolean,T]) : Future[Boolean] = {
    val liftId = liftDefinition(env, definition)
    env.transactionApi.executeServiceAction(liftId, action)
  }

  def createSimpleInstanceAndGetUrl(env: ServiceExecutionEnvironment, implKey: SamServiceImplementationKey): ServiceInstanceURL = {
    val instance = env.executionNode.createServiceInstance(implKey)
    import SamTransactionModel._
    val definition = definitionElement(HeadServiceInstanceReference(instance))
    val liftId = liftDefinition(env, definition)
    liftId.url
  }

  def createEnvironment = {
    val port = getAnyPortValue
    val builder = ServiceExecutionEnvironment.configurationBuilder(port)
    val config = builder.withArchitecture(new SeeTestArchitecture())
      .withImplementation(new TestImplementationDeclaration()).build
    ServiceExecutionEnvironment(config)
  }

  private val portCounter = new AtomicInteger(3000)

  def getAnyPortValue: Int = portCounter.getAndIncrement()

}
