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
import org.testng.annotations.{BeforeClass, Test}
import org.testng.Assert._
import eu.pmsoft.see.api.data.architecture.SeeTestArchitecture
import eu.pmsoft.see.api.data.impl.{TestServiceOneModule, TestServiceZeroModule}
import com.google.inject.Guice
import java.net.{URL, InetSocketAddress}
import scala.concurrent._
import scala.concurrent.duration._
import eu.pmsoft.sam.model._
import eu.pmsoft.see.api.data.architecture.service.{TestServiceOne, TestServiceZero}
import eu.pmsoft.sam.model.ExposedServiceTransaction
import eu.pmsoft.sam.model.SamServiceKey
import eu.pmsoft.sam.idgenerator.LongLongIdGenerator

class SamEnvironmentExternalApiTest extends SamTestUtil {

  val idGenerator: LongLongIdGenerator = LongLongIdGenerator.createGenerator()

  var externalApi: SamEnvironmentExternalApi = _

  var expectedServices: Seq[ExposedServiceTransaction] = _

  @BeforeClass
  def setup() {
    val client = createEnvironment
    val target = createEnvironment

    val testZero = SamModelBuilder.implementationKey(classOf[TestServiceZeroModule], SamServiceKey(classOf[TestServiceZero]))
    val testOne = SamModelBuilder.implementationKey(classOf[TestServiceOneModule], SamServiceKey(classOf[TestServiceOne]))

    val urlZero = createSimpleInstanceAndGetUrl(target, testZero)
    val urlOne = createSimpleInstanceAndGetUrl(target, testOne)
    expectedServices = Seq(
      ExposedServiceTransaction(urlZero, SamServiceKey(classOf[TestServiceZero])),
      ExposedServiceTransaction(urlOne, SamServiceKey(classOf[TestServiceOne]))
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
    assertTrue(result.filterNot(exposed => expectedServices.contains(exposed)).size == 0, "expected services don't match results")
  }

  @Test
  def testRegisterAndUnregisteredTransaction() {
    val transactionId = GlobalTransactionIdentifier(idGenerator.getNextID)
    val headId = ThreadExecutionIdentifier(idGenerator.getNextID)
    val surl = ServiceInstanceURL(new URL("http://test.com:2999/"))
    val pipeRef = PipeReference(headId, PipeIdentifier(idGenerator.getNextID), surl)
    val services: Future[Seq[ExposedServiceTransaction]] = externalApi.getExposedServices()
    val toRegister = services map {
      _.head
    }
    val regOK = toRegister flatMap {
      exposed =>
        externalApi.registerTransactionRemote(transactionId, pipeRef, exposed)
    } flatMap {
      ok =>
        externalApi.unRegisterTransactionRemote(transactionId)
    }
    assertTrue(Await.result(regOK, 2 second), " Failed to register and unregister transaction")
  }

}
