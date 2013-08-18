package eu.pmsoft.sam.see

import eu.pmsoft.sam.model._
import eu.pmsoft.sam.model.SamTransactionModel._
import eu.pmsoft.sam.model.ServiceInstanceURL
import eu.pmsoft.sam.model.SamServiceImplementationKey
import eu.pmsoft.see.api.data.architecture.SeeTestArchitecture
import eu.pmsoft.see.api.data.impl.TestImplementationDeclaration
import java.util.concurrent.atomic.AtomicInteger
import org.testng.Assert._
import eu.pmsoft.sam.model.HeadServiceInstanceReference
import eu.pmsoft.sam.model.ServiceInstanceURL
import eu.pmsoft.sam.model.SamServiceImplementationKey

trait SamTestUtil {

  def liftDefinition(env: ServiceExecutionEnvironment, definition: InjectionConfigurationDefinition) = {
    val configuration = buildConfiguration(env.architectureManager, env.executionNode, definition)
    val externalConfigId = env.executionNode.registerInjectionConfiguration(configuration)
    env.transaction.liftServiceConfiguration(externalConfigId)
  }

  def createTransaction(env: ServiceExecutionEnvironment, definition: InjectionConfigurationDefinition) = {
    env.transaction.createExecutionContext(liftDefinition(env, definition))
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

  def testServiceSetup(env: ServiceExecutionEnvironment, definition: InjectionConfigurationDefinition, testFun: CanonicalRecordingLayer => Boolean) {
    val transaction = createTransaction(env, definition)
    for (i <- 0 to 3) {
      assertTrue(testFun(transaction))
    }
  }

}
