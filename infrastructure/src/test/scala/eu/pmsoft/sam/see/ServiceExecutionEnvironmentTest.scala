package eu.pmsoft.sam.see

import org.scalatest.Assertions
import org.testng.annotations.Test
import eu.pmsoft.see.api.data.architecture.SeeTestArchitecture
import eu.pmsoft.see.api.data.impl._
import eu.pmsoft.sam.model._
import eu.pmsoft.see.api.data.architecture.contract.{TestInterfaceTwo0, TestInterfaceOne}
import com.google.inject.Key
import eu.pmsoft.see.api.data.impl.shopping.TestShoppingModule
import eu.pmsoft.see.api.data.impl.courier.TestCourierServiceModule
import eu.pmsoft.see.api.data.impl.store.TestStoreServiceModule
import eu.pmsoft.see.api.data.architecture.contract.store.StoreServiceContract
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierServiceContract
import eu.pmsoft.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction
import org.slf4j.LoggerFactory
import eu.pmsoft.see.api.data.architecture.service.{CourierService, StoreService}
import eu.pmsoft.sam.execution.ServiceAction
import scala.concurrent.Await
import scala.concurrent.duration._

class ServiceExecutionEnvironmentTest extends Assertions {

  val logger = LoggerFactory.getLogger(this.getClass)

  @Test def builderApi() {
    val anyPort = 3000
    val expected = SEEConfiguration(Set(), Set(),anyPort)
    val result = ServiceExecutionEnvironment.configurationBuilder(anyPort).build
    assert(expected == result)
  }


  @Test def simpleExecutionLocalTest() {
    val anyPort = 3002
    val builder = ServiceExecutionEnvironment.configurationBuilder(anyPort)
    val config = builder.withArchitecture(new SeeTestArchitecture())
      .withImplementation(new TestImplementationDeclaration()).build

    val env = ServiceExecutionEnvironment(config)

    val expectedImplementation = Seq(
      classOf[TestServiceZeroModule],
      classOf[TestServiceOneModule],
      classOf[TestServiceTwoModule]
    )
    val implKeys = expectedImplementation.map(SamModelBuilder.implementationKey _)
    assert(
      implKeys.map {
        env.serviceRegistry.getImplementation _
      } forall {
        _ != null
      }
    )

    val serviceInstance = implKeys map {
      env.executionNode.createServiceInstance _
    }
    assert(
      serviceInstance.map {
        _.implementation.implKey
      } zip implKeys forall {
        case (instanceKey, implementationKey) => instanceKey == implementationKey
      }
    )

    val sZero = serviceInstance(0)
    val sOne = serviceInstance(1)
    val sTwo = serviceInstance(2)
    val eZero = InjectionConfigurationBuilder.singleInstanceBind(env.architectureManager, sZero)
    val eOne = InjectionConfigurationBuilder.singleInstanceBind(env.architectureManager, sOne)
    val eTwo = InjectionConfigurationBuilder.complexInstanceBind(env.architectureManager, sTwo,
      Array(eZero, eOne)
    )
    val bindConfigElements = Seq(eZero, eOne, eTwo)
    val bindConfigurationIDs = bindConfigElements.map {
      env.executionNode.registerInjectionConfiguration _
    }
    val bindTransactions = bindConfigurationIDs map {
      env.transactionApi.getTransaction _
    }

    val transactionOne = bindTransactions(1)
    val apiRef = transactionOne.getTransactionInjector.getExistingBinding(Key.get(classOf[TestInterfaceOne])).getProvider.get()
    assert(apiRef.runTest())

    val complexTransactionTwo = bindTransactions(2)
    val apiTwoInterface = Key.get(classOf[TestInterfaceTwo0])
    assert(complexTransactionTwo.getTransactionInjector.getExistingBinding(Key.get(classOf[TestInterfaceOne])) == null)
    assert(complexTransactionTwo.getTransactionInjector.getExistingBinding(apiTwoInterface) != null)

    complexTransactionTwo.bindTransaction(None)
    val apiTwoRef = complexTransactionTwo.getTransactionInjector.getInstance(apiTwoInterface)
    assert(apiTwoRef.runTest())
    complexTransactionTwo.unBindTransaction



    val toUseAsExternalConfigurationElements = bindConfigElements.take(2)
    val toUseAsExternalConfiguration = bindConfigurationIDs.take(2)
    val externalConfigElements = toUseAsExternalConfigurationElements zip toUseAsExternalConfiguration map {
      case (element,config) => (element.contract , env.transactionApi.liftServiceConfiguration(config))
    } map {
      case (contract,url) => InjectionConfigurationBuilder.externalServiceBind(contract,url)
    }
    val externalConfig = InjectionConfigurationBuilder.complexInstanceBind(env.architectureManager,sTwo,externalConfigElements.toArray)
    val externalConfigId = env.executionNode.registerInjectionConfiguration(externalConfig)
    val complexTransactionTwoExternal = env.transactionApi.getTransaction(externalConfigId)

    // direct use of the transaction injector
    // Simple call
    for ( i <- 0 to 3 ) {
      complexTransactionTwoExternal.bindTransaction(None)
      val apiTwoRefExternal = complexTransactionTwoExternal.getTransactionInjector.getInstance(apiTwoInterface)
      assert(apiTwoRefExternal.runTest())
      complexTransactionTwoExternal.unBindTransaction
    }

  }

  @Test def shoppingScenarioExecutionTest() {
    val storeServer = createEnviroment(3002)
    val courierServer = createEnviroment(3003)
    val shoppingServer = createEnviroment(3004)


    val storeUrl = bindSingleInstance(storeServer, SamModelBuilder.implementationKey(classOf[TestStoreServiceModule]))
    val storeContract = storeServer.architectureManager.getService(SamServiceKey(classOf[StoreService]))
    val storeConfigurationRef = InjectionConfigurationBuilder.externalServiceBind(storeContract, storeUrl)

    val courierUrl = bindSingleInstance(courierServer, SamModelBuilder.implementationKey(classOf[TestCourierServiceModule]))
    val courierContract = storeServer.architectureManager.getService(SamServiceKey(classOf[CourierService]))
    val courierConfigurationRef = InjectionConfigurationBuilder.externalServiceBind(courierContract, courierUrl)

    val shoppingServiceInstance = shoppingServer.executionNode.createServiceInstance(SamModelBuilder.implementationKey(classOf[TestShoppingModule]))
    val shoppingConfiguration = InjectionConfigurationBuilder.complexInstanceBind(shoppingServer.architectureManager, shoppingServiceInstance,
      Array(storeConfigurationRef, courierConfigurationRef)
    )

    val shoppingConfigId = shoppingServer.executionNode.registerInjectionConfiguration(shoppingConfiguration)

    // direct use of the transaction injector
    //    for ( i <- 0 to 3 ) {
    logger.debug("init shopping transaction")

    val shoppingPrice = shoppingServer.transactionApi.executeServiceAction(shoppingConfigId, new ServiceAction[Integer,ShoppingStoreWithCourierInteraction](Key.get(classOf[ShoppingStoreWithCourierInteraction])) {
      def executeInteraction(service: ShoppingStoreWithCourierInteraction): Integer = service.makeShoping()
    })

    Await.result(shoppingPrice, 40 seconds)

  }

  private def bindSingleInstance( env: ServiceExecutionEnvironment,  implementationKey : SamServiceImplementationKey) : ServiceInstanceURL = {
    val courierServiceInstance = env.executionNode.createServiceInstance(implementationKey)
    val courierInstance = InjectionConfigurationBuilder.singleInstanceBind(env.architectureManager, courierServiceInstance)
    val courierConfigId = env.executionNode.registerInjectionConfiguration(courierInstance)
    env.transactionApi.liftServiceConfiguration(courierConfigId)
  }

  private def createEnviroment(port : Int ) = {
    val builder = ServiceExecutionEnvironment.configurationBuilder(port)
    val config = builder.withArchitecture(new SeeTestArchitecture())
      .withImplementation(new TestImplementationDeclaration()).build
    ServiceExecutionEnvironment(config)
  }

  @Test def shoppingScenarioExecutionLocalTest() {
    val anyPort = 3001

    val env = createEnviroment(anyPort)

    val expectedImplementation = Seq(
      classOf[TestStoreServiceModule],
      classOf[TestCourierServiceModule],
      classOf[TestShoppingModule]
    )
    val implKeys = expectedImplementation.map(SamModelBuilder.implementationKey _)
    assert(
      implKeys.map {
        env.serviceRegistry.getImplementation _
      } forall {
        _ != null
      }
    )

    val serviceInstance = implKeys map {
      env.executionNode.createServiceInstance _
    }
    assert(
      serviceInstance.map {
        _.implementation.implKey
      } zip implKeys forall {
        case (instanceKey, implementationKey) => instanceKey == implementationKey
      }
    )

    val storeService = serviceInstance(0)
    val courierService = serviceInstance(1)
    val shoppingService = serviceInstance(2)
    val storeInstance = InjectionConfigurationBuilder.singleInstanceBind(env.architectureManager, storeService)
    val courierInstance = InjectionConfigurationBuilder.singleInstanceBind(env.architectureManager, courierService)
    val shoppingInstance = InjectionConfigurationBuilder.complexInstanceBind(env.architectureManager, shoppingService,
      Array(storeInstance, courierInstance)
    )
    val bindConfigElements = Seq(storeInstance, courierInstance, shoppingInstance)
    val bindConfigurationIDs = bindConfigElements.map {
      env.executionNode.registerInjectionConfiguration _
    }
    val bindTransactions = bindConfigurationIDs map {
      env.transactionApi.getTransaction _
    }

    val storeTransaction = bindTransactions(0)
    val storeApi = storeTransaction.getTransactionInjector.getExistingBinding(Key.get(classOf[StoreServiceContract]))
    assert(storeApi != null)

    val courierTransaction = bindTransactions(1)
    assert(courierTransaction.getTransactionInjector.getExistingBinding(Key.get(classOf[CourierServiceContract])) != null)

    val toUseAsExternalConfigurationElements = bindConfigElements.take(2)
    val toUseAsExternalConfiguration = bindConfigurationIDs.take(2)
    val externalConfigElements = toUseAsExternalConfigurationElements zip toUseAsExternalConfiguration map {
      case (element,config) => (element.contract , env.transactionApi.liftServiceConfiguration(config))
    } map {
      case (contract,url) => InjectionConfigurationBuilder.externalServiceBind(contract,url)
    }
    val externalConfig = InjectionConfigurationBuilder.complexInstanceBind(env.architectureManager,shoppingService,externalConfigElements.toArray)
    val externalConfigId = env.executionNode.registerInjectionConfiguration(externalConfig)
    val complexTransactionTwoExternal = env.transactionApi.getTransaction(externalConfigId)

    // direct use of the transaction injector
//    for ( i <- 0 to 3 ) {
      logger.debug("init shopping transaction")
      complexTransactionTwoExternal.bindTransaction(None)
      val shoppingApi = complexTransactionTwoExternal.getTransactionInjector.getInstance(Key.get(classOf[ShoppingStoreWithCourierInteraction]))
      assert(shoppingApi.makeShoping() != null)
      complexTransactionTwoExternal.unBindTransaction
//    }

  }

}
