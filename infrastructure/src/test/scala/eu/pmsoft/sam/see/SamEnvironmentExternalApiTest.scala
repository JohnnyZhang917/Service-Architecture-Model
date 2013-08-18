package eu.pmsoft.sam.see

import org.testng.annotations.{BeforeClass, Test}
import org.testng.Assert._
import eu.pmsoft.see.api.data.architecture.SeeTestArchitecture
import eu.pmsoft.see.api.data.impl.{TestServiceTwoModule, TestServiceOneModule, TestServiceZeroModule, TestImplementationDeclaration}
import com.google.inject.Guice
import java.net.{URL, InetSocketAddress}
import scala.concurrent._
import scala.concurrent.duration._
import eu.pmsoft.sam.model._
import eu.pmsoft.see.api.data.architecture.service.{TestServiceTwo, TestServiceOne, TestServiceZero}
import eu.pmsoft.sam.model.SamTransactionModel._
import eu.pmsoft.sam.model.ExposedServiceTransaction
import eu.pmsoft.sam.model.SamServiceKey
import eu.pmsoft.sam.model.ExposedServiceTransaction
import eu.pmsoft.sam.model.SamServiceKey

class SamEnvironmentExternalApiTest extends SamTestUtil {

  var externalApi: SamEnvironmentExternalApi = _

  var expectedServices : Seq[ExposedServiceTransaction] = _

  @BeforeClass
  def setup() {
    val client = createEnvironment
    val target = createEnvironment

    val testZero = SamModelBuilder.implementationKey(classOf[TestServiceZeroModule], SamServiceKey(classOf[TestServiceZero]))
    val testOne = SamModelBuilder.implementationKey(classOf[TestServiceOneModule], SamServiceKey(classOf[TestServiceOne]))

    val urlZero = createSimpleInstanceAndGetUrl(target,testZero)
    val urlOne = createSimpleInstanceAndGetUrl(target,testOne)
    expectedServices = Seq(
      ExposedServiceTransaction(urlZero,SamServiceKey(classOf[TestServiceZero])),
      ExposedServiceTransaction(urlOne ,SamServiceKey(classOf[TestServiceOne]))
    )

    val targetPort = target.configuration.port
    val targetAddress = new InetSocketAddress(targetPort)

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

  @Test
  def testGetAccessiblyServices() {
    val services: Future[Seq[ExposedServiceTransaction]] = externalApi.getExposedServices()
    val result: Seq[ExposedServiceTransaction] = Await.result(services, 2 seconds)
    assertTrue(result.size == 2, "expected 2 exposed services")
    assertTrue(result.filterNot( exposed => expectedServices.contains( exposed ) ).size == 0 , "expected services don't match results" )
  }

//  @Test
//  def testAccessiblyServiceSignatureAndType() {
//    assertTrue(false)
//  }
//
//  @Test
//  def testCheckTransactionConfigurationPositive() {
//    assertTrue(false)
//  }
//
//  @Test
//  def testCheckTransactionConfigurationNegative() {
//    assertTrue(false)
//  }
//
//  @Test
//  def testTransactionGlobalIdSetup() {
//    assertTrue(false)
//  }
//
//  @Test
//  def testTransactionCommunicationByGlobalId() {
//    assertTrue(false)
//  }

}
