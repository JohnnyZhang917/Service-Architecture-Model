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

package eu.pmsoft.sam.model

import com.google.inject.Key
import java.lang.reflect.Method

object CanonicalProtocol {
  def isClientInstance(instance: CanonicalProtocolInstance): Boolean = {
    instance match {
      case ClientBindingKeyInstance(instanceNr, key) => true
      case ClientReturnBindingKeyInstance(instanceNr, key) => true
      case ClientExternalInstanceBinding(instanceNr, key) => true
      case ClientPendingDataInstance(instanceNr, key) => true
      case ClientFilledDataInstance(instanceNr, key, data) => true
      case ClientDataInstance(instanceNr, key, data) => true
      case ServerBindingKeyInstance(instanceNr, key) => false
      case ServerReturnBindingKeyInstance(instanceNr, key) => false
      case ServerExternalInstanceBinding(instanceNr, key) => false
      case ServerPendingDataInstance(instanceNr, key) => false
      case ServerFilledDataInstance(instanceNr, key, data) => false
      case ServerDataInstance(instanceNr, key, data) => false
    }
  }

  def isFilledInstance(instance: CanonicalProtocolInstance) = {
    instance match {
      case ClientFilledDataInstance(instanceNr, key, data) => true
      case ServerFilledDataInstance(instanceNr, key, data) => true
      case _ => false
    }
  }

  def isServerInstance(instance: CanonicalProtocolInstance) = !isClientInstance(instance)

}


abstract sealed class ReflectionAbstractCall(val instance: CanonicalProtocolInstance, val method: Method, val args: Option[Array[AnyRef]])

case class ReflectionVoidMethodCall(override val instance: CanonicalProtocolInstance, override val method: Method, override val args: Option[Array[AnyRef]]) extends ReflectionAbstractCall(instance, method, args)

case class ReflectionInterfaceMethodCall(override val instance: CanonicalProtocolInstance, override val method: Method, override val args: Option[Array[AnyRef]], returnType: Class[_]) extends ReflectionAbstractCall(instance, method, args)

case class ReflectionDataMethodCall(override val instance: CanonicalProtocolInstance, override val method: Method, override val args: Option[Array[AnyRef]], returnType: Class[_]) extends ReflectionAbstractCall(instance, method, args)


abstract sealed class CanonicalProtocolInstance(val instanceNr: Int, val key: Key[_])


case class ClientBindingKeyInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ClientReturnBindingKeyInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ClientExternalInstanceBinding(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ClientPendingDataInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ClientFilledDataInstance(override val instanceNr: Int, override val key: Key[_], data: java.io.Serializable) extends CanonicalProtocolInstance(instanceNr, key)

case class ClientDataInstance(override val instanceNr: Int, override val key: Key[_], data: java.io.Serializable) extends CanonicalProtocolInstance(instanceNr, key)

case class ServerBindingKeyInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ServerReturnBindingKeyInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ServerExternalInstanceBinding(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ServerPendingDataInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ServerFilledDataInstance(override val instanceNr: Int, override val key: Key[_], data: java.io.Serializable) extends CanonicalProtocolInstance(instanceNr, key)

case class ServerDataInstance(override val instanceNr: Int, override val key: Key[_], data: java.io.Serializable) extends CanonicalProtocolInstance(instanceNr, key)


abstract sealed class CanonicalProtocolMethodCall(
                                                   val instanceNr: Int,
                                                   val methodSignature: Int,
                                                   val arguments: Array[Int],
                                                   val returnInstance: Option[Int]) {

  def toCallString = s"$instanceNr.${methodSignature}(${arguments.mkString(",")}):#$returnInstance"

}

case class VoidMethodCall(
                           override val instanceNr: Int,
                           override val methodSignature: Int,
                           override val arguments: Array[Int]
                           )
  extends CanonicalProtocolMethodCall(instanceNr, methodSignature, arguments, None)

case class InterfaceMethodCall(
                                override val instanceNr: Int,
                                override val methodSignature: Int,
                                override val arguments: Array[Int],
                                val returnNr: Int
                                )
  extends CanonicalProtocolMethodCall(instanceNr, methodSignature, arguments, Some(returnNr))

case class ReturnMethodCall(
                             override val instanceNr: Int,
                             override val methodSignature: Int,
                             override val arguments: Array[Int],
                             val returnNr: Int
                             )
  extends CanonicalProtocolMethodCall(instanceNr, methodSignature, arguments, Some(returnNr))


