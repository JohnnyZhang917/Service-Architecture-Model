package eu.pmsoft.sam.see

import com.google.inject.{Injector, Key}
import java.lang.reflect.{InvocationHandler, Method}
import eu.pmsoft.sam.injection.{ExternalInstanceProvider, ExternalBindingSwitch}
import scala.annotation.tailrec
import eu.pmsoft.sam.model.ExternalServiceBind
import scala.collection.mutable
import scala.collection.immutable.Stack

object CanonicalRecordingLayer {

}


sealed class CanonicalProtocolInstance(val instanceNr: Int, val key: Key[_])

case class BindingKeyInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ReturnBindingKeyInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class ExternalInstanceBinding(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class PendingDataInstance(override val instanceNr: Int, override val key: Key[_]) extends CanonicalProtocolInstance(instanceNr, key)

case class FilledDataInstance(override val instanceNr: Int, override val key: Key[_], data: java.io.Serializable) extends CanonicalProtocolInstance(instanceNr, key)

case class DataInstance(override val instanceNr: Int, override val key: Key[_], data: java.io.Serializable) extends CanonicalProtocolInstance(instanceNr, key)


sealed class CanonicalProtocolMethodCall(
                                          val instanceNr: Int,
                                          val arguments: Array[Int],
                                          val returnInstance: Option[Int])

case class VoidMethodCall(
                           override val instanceNr: Int,
                           override val arguments: Array[Int]
                           )
  extends CanonicalProtocolMethodCall(instanceNr, arguments, None)

case class InterfaceMethodCall(
                                override val instanceNr: Int,
                                override val arguments: Array[Int],
                                val returnNr: Int
                                )
  extends CanonicalProtocolMethodCall(instanceNr, arguments, Some(returnNr))

case class ReturnMethodCall(
                             override val instanceNr: Int,
                             override val arguments: Array[Int],
                             val returnNr: Int
                             )
  extends CanonicalProtocolMethodCall(instanceNr, arguments, Some(returnNr))



class CanonicalProtocolServiceRecordContext(val transactionContext: TransactionRecordContext) {

  var status: RecordQueue = RecordQueue()

  private def update(instance: CanonicalProtocolInstance) = {
    status = status.copy(instances =  status.instances :+ instance )
    instance
  }

  private def update(call: CanonicalProtocolMethodCall) = {
    status = status.copy(calls =  status.calls :+ call )
    call
  }

  def parseArguments(types: Array[Class[_]], arguments: Array[AnyRef]): Array[Int] = {
    val params = types zip arguments
    params.map(extractInternalInstanceNumber _)
  }

  def createKeyBinding[T](key: Key[T]): CanonicalProtocolInstance = update(BindingKeyInstance(status.instances.size, key))

  private def createExternalInstanceBinding(externalSlotInstance: CanonicalInstanceRecorder[_]): CanonicalProtocolInstance = {
    require(false, "TODO - map between slots")
    update(ExternalInstanceBinding(status.instances.size, externalSlotInstance.instance.key))
  }

  private def createDataBinding(key: Key[_], serializable: java.io.Serializable): CanonicalProtocolInstance = {
    update(DataInstance(status.instances.size, key, serializable))
  }

  @tailrec
  private def extractInternalInstanceNumber(param: (Class[_], AnyRef)): Int = {
    val arg = param._2
    val atype = param._1
    arg match {
      case null => -1
      case serializable: java.io.Serializable => createDataBinding(Key.get(atype), serializable).instanceNr
      case recorder: CanonicalInstanceRecorder[_] if recorder.context == this => recorder.instance.instanceNr
      case recorder: CanonicalInstanceRecorder[_] => createExternalInstanceBinding(recorder).instanceNr
      case proxy: ExternalBindingSwitch[_] => extractInternalInstanceNumber((atype, proxy.getInternalInstance.asInstanceOf[AnyRef]))
      case _ => throw new RuntimeException("can not handle this argument type")
    }
  }

  def recordExternalMethodCall(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef]): Unit = {
    val arguments = parseArguments(method.getParameterTypes, args)
    update(VoidMethodCall(status.calls.size, arguments))
  }

  def recordExternalMethodCallWithInterfaceReturnType(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef], returnType: Class[_]): CanonicalProtocolInstance = {
    val arguments = parseArguments(method.getParameterTypes, args)

    val binding = update(ReturnBindingKeyInstance(status.instances.size, Key.get(returnType)))

    update(InterfaceMethodCall(instance.instanceNr,arguments,binding.instanceNr))

    binding
  }

  def recordExternalMethodCallWithReturnType(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef], returnType: Class[_]): AnyRef = {
    val arguments = parseArguments(method.getParameterTypes, args)
    val binding = update(PendingDataInstance(status.instances.size,Key.get(returnType)))
    update(ReturnMethodCall(instance.instanceNr,arguments,binding.instanceNr))
    transactionContext.forceContextExecution(this)
    getInstanceForFilledInstance(binding)
  }

  private def getInstanceForFilledInstance(binding: CanonicalProtocolInstance): AnyRef = {
    status.instances(binding.instanceNr) match {
      case FilledDataInstance(_,_,data) => data.asInstanceOf[AnyRef]
      case _ => throw new RuntimeException("not expected status")
    }
  }

}

case class RecordQueue(instances: Vector[CanonicalProtocolInstance] = Vector.empty, calls: Stack[CanonicalProtocolMethodCall] = Vector.empty)





trait InstanceProvider {
  def getInstance[T](key: Key[T]): T
}


class ClientRecordingInstanceRegistry(
                                       val bind: ExternalServiceBind,
                                       val context: CanonicalProtocolServiceRecordContext
                                       ) extends InstanceProvider {

  def getInstance[T](key: Key[T]): T = {
    new CanonicalInstanceRecorder(context, context.createKeyBinding(key)).getInstance
  }
}

class DirectServiceInstanceProvider(val injector: Injector) extends InstanceProvider {
  def getInstance[T](key: Key[T]): T = injector.getExistingBinding(key).getProvider.get()
}

class MemoizedInstanceProvider(val instanceProvider: InstanceProvider) {

  val instanceMap: mutable.Map[Key[_], mutable.HashMap[Int, _]] = mutable.Map.empty

  private def getTable[T](key: Key[T]): mutable.HashMap[Int, T] = {
    instanceMap.get(key) match {
      case Some(table) => table.asInstanceOf[mutable.HashMap[Int, T]]
      case None => {
        val instanceValues: mutable.HashMap[Int, T] = mutable.HashMap.empty
        instanceMap.put(key, instanceValues)
        instanceValues
      }
    }
  }

  def getReference[T](key: Key[T], instanceNr: Int): T = {
    val instancePerKey: mutable.HashMap[Int, T] = getTable(key)
    instancePerKey.get(instanceNr) match {
      case Some(instance) => instance
      case None => {
        val instance = instanceProvider.getInstance(key)
        instancePerKey.put(instanceNr, instance)
        instance
      }
    }
  }
}

class InstanceProviderTransactionContext(val contractMap: Map[Key[_], MemoizedInstanceProvider]) extends ExternalInstanceProvider {

  def getReference[T](key: Key[T], instanceReferenceNr: Int): T = {
    contractMap.get(key).get.getReference(key, instanceReferenceNr)
  }
}


class CanonicalInstanceRecorder[T](val context: CanonicalProtocolServiceRecordContext, val instance: CanonicalProtocolInstance) extends InvocationHandler {

  lazy val instanceRef: T = {
    val returnedType = instance.key.getTypeLiteral.getRawType
    java.lang.reflect.Proxy.newProxyInstance(returnedType.getClassLoader(), Array(returnedType), this).asInstanceOf[T]
  }

  def invoke(proxy: Any, method: Method, args: Array[AnyRef]): AnyRef = {
    val returnType = method.getReturnType
    if (method.getName.compare("toString") == 0) return this.toString
    if (returnType.equals(Void.TYPE)) {
      context.recordExternalMethodCall(instance, method, args)
      return null
    }
    if (returnType.isInterface()) {
      val canonicalInstance = context.recordExternalMethodCallWithInterfaceReturnType(instance, method, args, returnType)
      val handler = new CanonicalInstanceRecorder(context,canonicalInstance)
      return handler.getInstance;
    }
    return context.recordExternalMethodCallWithReturnType(instance, method, args, returnType)
  }

  def getInstance: T = instanceRef
}


