package eu.pmsoft.sam.see

import org.scalatest.Assertions
import org.testng.annotations.Test
import eu.pmsoft.see.api.data.architecture.SeeTestArchitecture
import eu.pmsoft.see.api.data.impl._
import eu.pmsoft.sam.model._
import eu.pmsoft.see.api.data.architecture.contract.{TestInterfaceTwo0, TestInterfaceOne}
import com.google.inject.Key

class ServiceExecutionEnvironmentTest extends Assertions {

  @Test def builderApi() {
    val anyPort = 3000
    val expected = SEEConfiguration(Set(), Set(),anyPort)
    val result = ServiceExecutionEnvironment.configurationBuilder(anyPort).build
    assert(expected == result)
  }

  @Test def EnvironmentCreation() {
    val anyPort = 3001
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

    complexTransactionTwo.bindTransaction
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
    complexTransactionTwoExternal.bindTransaction
    val apiTwoRefExternal = complexTransactionTwoExternal.getTransactionInjector.getInstance(apiTwoInterface)
    assert(apiTwoRefExternal.runTest())
    complexTransactionTwoExternal.unBindTransaction


  }

}
