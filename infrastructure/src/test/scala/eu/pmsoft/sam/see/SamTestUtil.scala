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
import org.slf4j.LoggerFactory

object SamTestUtil {
  private val portCounter  = new AtomicInteger(3000)
}

trait SamTestUtil {

  val logger = LoggerFactory.getLogger(classOf[SamTestUtil])

  def liftDefinition(env: ServiceExecutionEnvironment, definition: InjectionConfigurationDefinition) = {
    val configuration = buildConfiguration(env.architectureManagerApi, env.executionNode, definition)
    val externalConfigId = env.executionNode.registerInjectionConfiguration(configuration)
    env.executionNode.liftServiceConfiguration(externalConfigId)
  }

  def executeTestAction[T](env: ServiceExecutionEnvironment, definition: InjectionConfigurationDefinition, action: ServiceAction[Boolean, T]): Future[Boolean] = {
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

  def getAnyPortValue: Int = SamTestUtil.portCounter.getAndIncrement()

}
