package eu.pmsoft.sam.see

import org.testng.annotations.{BeforeClass, Test}
import org.testng.Assert._
import eu.pmsoft.see.api.data.architecture.SeeTestArchitecture
import eu.pmsoft.see.api.data.impl.TestImplementationDeclaration
import com.google.inject.Guice
import java.net.InetSocketAddress
import scala.concurrent._
import scala.concurrent.duration._

class SamEnvironmentExternalApiTest {

  private def createEnvironment(port: Int): ServiceExecutionEnvironment = {
    val config = ServiceExecutionEnvironment.configurationBuilder(port).withArchitecture(new SeeTestArchitecture())
      .withImplementation(new TestImplementationDeclaration()).build
    ServiceExecutionEnvironment(config)
  }

  var externalApi: SamEnvironmentExternalApi = _

  @BeforeClass
  def setup() {
    val client = createEnvironment(4001)
    val targetPort = 4002
    val targetAddress = new InetSocketAddress(targetPort)
    createEnvironment(targetPort)
    val clientInjector = Guice.createInjector(client.clientApiModule)
    val connector = clientInjector.getInstance(classOf[SamEnvironmentExternalConnector])
    externalApi = connector.onEnvironment(targetAddress)
  }


  @Test
  def testGetArchitectureSignature() {
    val architectureSignature = externalApi.getArchitectureSignature()
    assertEquals(SeeTestArchitecture.signature, Await.result(architectureSignature, 1 second))
  }

  @Test
  def testPing() {
    assertTrue(Await.result(externalApi.ping(), 1 second))
  }


}
