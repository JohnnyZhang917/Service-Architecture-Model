package eu.pmsoft.sam.model

import com.google.inject.Key
import java.lang.reflect.Method

object CanonicalProtocol {

}


abstract sealed class ReflectionAbstractCall(val instance: CanonicalProtocolInstance, val method: Method, val args: Option[Array[AnyRef]])

case class ReflectionVoidMethodCall(override val instance: CanonicalProtocolInstance, override val method: Method, override val args: Option[Array[AnyRef]]) extends ReflectionAbstractCall(instance, method, args)

case class ReflectionInterfaceMethodCall(override val instance: CanonicalProtocolInstance, override val method: Method, override val args: Option[Array[AnyRef]], returnType: Class[_]) extends ReflectionAbstractCall(instance, method, args)

case class ReflectionDataMethodCall(override val instance: CanonicalProtocolInstance, override val method: Method, override val args: Option[Array[AnyRef]], returnType: Class[_]) extends ReflectionAbstractCall(instance, method, args)


abstract sealed class CanonicalProtocolInstance(val instanceNr: Int, val key: Key[_])

case class BindingKeyInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ReturnBindingKeyInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ExternalInstanceBinding(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class PendingDataInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class FilledDataInstance(override val instanceNr: Int, override val key: Key[_], data: java.io.Serializable) extends CanonicalProtocolInstance(instanceNr, key)

case class DataInstance(override val instanceNr: Int, override val key: Key[_], data: java.io.Serializable) extends CanonicalProtocolInstance(instanceNr, key)


sealed class CanonicalProtocolMethodCall(
                                          val instanceNr: Int,
                                          val methodSignature: Int,
                                          val arguments: Array[Int],
                                          val returnInstance: Option[Int]) {

  def toCallString = s"$instanceNr.${methodSignature}(${arguments.mkString(",")}):#$returnInstance"

}

case class VoidMethodCall(
                           override val instanceNr: Int,
                           val method: Method,
                           override val arguments: Array[Int]
                           )
  extends CanonicalProtocolMethodCall(instanceNr, method.hashCode(), arguments, None)

case class InterfaceMethodCall(
                                override val instanceNr: Int,
                                val method: Method,
                                override val arguments: Array[Int],
                                val returnNr: Int
                                )
  extends CanonicalProtocolMethodCall(instanceNr, method.hashCode(), arguments, Some(returnNr))

case class ReturnMethodCall(
                             override val instanceNr: Int,
                             val method: Method,
                             override val arguments: Array[Int],
                             val returnNr: Int
                             )
  extends CanonicalProtocolMethodCall(instanceNr, method.hashCode(), arguments, Some(returnNr))


