package eu.pmsoft.sam.see

import scala.concurrent.Await
import scala.concurrent.duration._
import org.testng.annotations.Test
import org.testng.Assert._
import com.google.inject.Key
import org.slf4j.LoggerFactory
import eu.pmsoft.see.api.data.architecture.SeeTestArchitecture
import eu.pmsoft.see.api.data.impl._
import eu.pmsoft.sam.model._
import eu.pmsoft.see.api.data.architecture.contract.{TestInterfaceTwo1, TestInterfaceTwo0, TestInterfaceOne}
import eu.pmsoft.see.api.data.impl.shopping.TestShoppingModule
import eu.pmsoft.see.api.data.impl.courier.TestCourierServiceModule
import eu.pmsoft.see.api.data.impl.store.TestStoreServiceModule
import eu.pmsoft.see.api.data.architecture.contract.store.StoreServiceContract
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierServiceContract
import eu.pmsoft.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction
import eu.pmsoft.see.api.data.architecture.service._
import eu.pmsoft.sam.execution.ServiceAction
import eu.pmsoft.sam.model.SamTransactionModel._
import eu.pmsoft.sam.model.ServiceInstanceURL
import eu.pmsoft.sam.model.SamServiceKey
import eu.pmsoft.sam.model.HeadServiceImplementationReference
import eu.pmsoft.sam.model.SEEConfiguration
import eu.pmsoft.sam.model.SamServiceImplementationKey
import java.util.concurrent.atomic.AtomicInteger

class ServiceExecutionEnvironmentTest extends SamTestUtil {

  val logger = LoggerFactory.getLogger(this.getClass)

  @Test def builderApi() {
    val anyPort = getAnyPortValue
    val expected = SEEConfiguration(Set(), Set(), anyPort)
    val result = ServiceExecutionEnvironment.configurationBuilder(anyPort).build
    assert(expected == result)
  }

  @Test def architectureIsLoadCorrectly() {
    val env = createEnvironment

    val expectedImplementation = Seq(
      (classOf[TestStoreServiceModule], SamServiceKey(classOf[StoreService])),
      (classOf[TestCourierServiceModule], SamServiceKey(classOf[CourierService])),
      (classOf[TestShoppingModule], SamServiceKey(classOf[ShoppingService]))
    )
    val implKeys = expectedImplementation.map(p => SamModelBuilder.implementationKey(p._1, p._2))
    assertTrue(
      implKeys.map {
        env.serviceRegistry.getImplementation _
      } forall {
        _ != null
      }
    )

    val serviceInstance = implKeys map {
      env.executionNode.createServiceInstance _
    }
    assertTrue(
      serviceInstance.map {
        _.implementation.implKey
      } zip implKeys forall {
        case (instanceKey, implementationKey) => instanceKey == implementationKey
      }
    )
  }


  @Test def testSimpleServiceDirect() {
    val env = createEnvironment
    val testZero = SamModelBuilder.implementationKey(classOf[TestServiceZeroModule], SamServiceKey(classOf[TestServiceZero]))
    val testOne = SamModelBuilder.implementationKey(classOf[TestServiceOneModule], SamServiceKey(classOf[TestServiceOne]))
    val testTwo = SamModelBuilder.implementationKey(classOf[TestServiceTwoModule], SamServiceKey(classOf[TestServiceTwo]))
    import SamTransactionModel._
    val definition = definitionElement(
      HeadServiceImplementationReference(testTwo),
      Seq(
        definitionElement(HeadServiceImplementationReference(testOne)),
        definitionElement(HeadServiceImplementationReference(testZero))
      )
    )

    val action : ServiceAction[Boolean,TestInterfaceTwo1] = new ServiceAction[Boolean,TestInterfaceTwo1](Key.get(classOf[TestInterfaceTwo1])) {
      def executeInteraction(serviceTwoApi: TestInterfaceTwo1): Boolean = serviceTwoApi.runTest()
    }
    val res = executeTestAction(env,definition,action)
    assertTrue(Await.result(res, 3 seconds))
  }


  @Test def testSimpleServiceByUrl() {
    val env = createEnvironment

    val testZero = SamModelBuilder.implementationKey(classOf[TestServiceZeroModule], SamServiceKey(classOf[TestServiceZero]))
    val testOne = SamModelBuilder.implementationKey(classOf[TestServiceOneModule], SamServiceKey(classOf[TestServiceOne]))
    val testTwo = SamModelBuilder.implementationKey(classOf[TestServiceTwoModule], SamServiceKey(classOf[TestServiceTwo]))

    val urlOne = createSimpleInstanceAndGetUrl(env,testOne)
    val urlZero = createSimpleInstanceAndGetUrl(env,testZero)

    import SamTransactionModel._

    val definition = definitionElement(
      HeadServiceImplementationReference(testTwo),
      Seq(
        externalReference(SamServiceKey(classOf[TestServiceOne]),urlOne),
        externalReference(SamServiceKey(classOf[TestServiceZero]),urlZero)
      )
    )

    val action : ServiceAction[Boolean,TestInterfaceTwo1] = new ServiceAction[Boolean,TestInterfaceTwo1](Key.get(classOf[TestInterfaceTwo1])) {
      def executeInteraction(serviceTwoApi: TestInterfaceTwo1): Boolean = serviceTwoApi.runTest()
    }
    val res = executeTestAction(env,definition,action)
    assertTrue(Await.result(res, 3 seconds))
  }

  @Test def directShoppingExecutionDirect() {
    val env = createEnvironment

    val shoppingServiceImplKey = SamModelBuilder.implementationKey(classOf[TestShoppingModule], SamServiceKey(classOf[ShoppingService]))
    val storeServiceImplKey = SamModelBuilder.implementationKey(classOf[TestStoreServiceModule], SamServiceKey(classOf[StoreService]))
    val courierServiceImplKey = SamModelBuilder.implementationKey(classOf[TestCourierServiceModule], SamServiceKey(classOf[CourierService]))
    import SamTransactionModel._
    val definition = definitionElement(
      HeadServiceImplementationReference(shoppingServiceImplKey),
      Seq(
        definitionElement(HeadServiceImplementationReference(storeServiceImplKey)),
        definitionElement(HeadServiceImplementationReference(courierServiceImplKey))
      )
    )
    val action : ServiceAction[Boolean,ShoppingStoreWithCourierInteraction] = new ServiceAction[Boolean,ShoppingStoreWithCourierInteraction](Key.get(classOf[ShoppingStoreWithCourierInteraction])) {
      def executeInteraction(service: ShoppingStoreWithCourierInteraction): Boolean = service.makeShoping() == 8800
    }
    val res = executeTestAction(env,definition,action)
    assertTrue(Await.result(res, 3 seconds))
  }

}
