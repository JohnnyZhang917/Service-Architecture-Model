package eu.pmsoft.sam.see


import java.lang.reflect.{InvocationHandler, Method}
import eu.pmsoft.sam.model._
import scala._
import scala.AnyRef
import eu.pmsoft.sam.model.ExternalServiceBind
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.tailrec
import com.google.inject.{Injector, Key}
import eu.pmsoft.sam.injection.{ExternalInstanceProvider, DependenciesBindingContext, ExternalBindingSwitch}
import scala.collection.mutable
import eu.pmsoft.sam.model
import org.slf4j.LoggerFactory

object CanonicalRecordingLayer {

  def apply(transportProvider: Seq[ExternalServiceBind] => Seq[SlotExecutionPipe]) = new CanonicalRecordingLayer(transportProvider)

}


trait CanonicalProtocolInstanceRef[T] {
  def getInstance: T
}

trait InstanceProvider {
  def getInstance[T](key: Key[T]): T
}


trait RecordEmbroider {

  def haft(call: ReflectionAbstractCall): CanonicalProtocolInstanceRef[_ <: AnyRef]

  def key[T](bindingKey: Key[T]): CanonicalProtocolInstanceRef[T]

}


class CanonicalRecordingLayer(transportProvider: Seq[ExternalServiceBind] => Seq[SlotExecutionPipe]) {

  def createContext(injectionConfiguration: InjectionConfigurationElement) = {
    new InjectionTransactionContext(injectionConfiguration, transportProvider: Seq[ExternalServiceBind] => Seq[SlotExecutionPipe])
  }

}


class InjectionTransactionContext(injectionConfiguration: InjectionConfigurationElement, transportProvider: Seq[ExternalServiceBind] => Seq[SlotExecutionPipe]) extends InjectionTransactionAccessApi {

  private val recorder = new InjectionTransactionRecordManager(injectionConfiguration, transportProvider)
  val logger = LoggerFactory.getLogger(this.getClass)

  private val headInjector = InjectionTransaction.glueInjector(recorder.injectionConfiguration)
  require(headInjector.isDefined)
  private val executor = new TransactionExecutionManager(headInjector.get)

  private val transaction: InjectionTransaction = {
    val rootNode = InjectionTransaction.createNode(new InjectionTransactionWrappingContext(recorder.externalInstanceProviders), recorder.injectionConfiguration)
    new InjectionTransaction(headInjector.get, rootNode)
  }

  def getTransactionInjector: Injector = transaction.transactionInjector

  def bindTransaction {
    recorder.externalExecutionManager.bindTransaction
    transaction.rootNode.bindSwitchingScope
  }

  def unBindTransaction {
    transaction.rootNode.unbindSwitchingScope
    recorder.externalExecutionManager.unbindTransaction
  }

  def protocolExecution(message: ThreadMessage, clientTransport: TransportAbstraction): ThreadMessage = {
    message.data.map {
      proto => {
        executor.mergeInstances(0, proto.instances)
        executor.executionManager.executeCalls(0, proto.calls)

      }
    }

    val finishData = CanonicalRequest(executor.getInstancetoMerge(0), Seq(), true)
    val res = ThreadMessage(Some(finishData))
    logger.debug("response {}", res)
    res

  }
}

private class TransactionExecutionManager(headInjector: Injector) extends InstanceRegistry {
  private var transactionStatus: InstanceRegistryStatus = InstanceRegistryStatus(Vector(InstanceRegistrySlotStatus()))

  val executionManager = new ExecutionStackManager(Vector(), this)

  def getInstanceReferenceToTransfer(slotNr: Int): Seq[CanonicalProtocolInstance] = {
    val startPosition = transactionStatus.slots(slotNr).transferMark
    val slotInstance = transactionStatus.slots(slotNr).instances.drop(startPosition).map(_.instance)
    val toSend = slotInstance.takeWhile(_ match {
      case _: BindingKeyInstance => true
      case _: ReturnBindingKeyInstance => true
      case _: ExternalInstanceBinding => true
      case _: PendingDataInstance => true
      case _: FilledDataInstance => true
      case _: DataInstance => true
    })
    transactionStatus = transactionStatus.updated(UpdateTransferMark(startPosition + toSend.size))(slotNr)
    toSend.toSeq
  }

  def mergeInstances(slotNr: Int, instances: Seq[CanonicalProtocolInstance]) {
    val instanceRef: Seq[InstanceExecutionData] = instances.map {
      _ match {
        case i@BindingKeyInstance(nr, key) => InstanceExecutionData(i, headInjector.getInstance(key).asInstanceOf[AnyRef])
        case i@ReturnBindingKeyInstance(nr, key) => InstanceExecutionData(i, null)
        case i@ExternalInstanceBinding(nr, key) => InstanceExecutionData(i, ???)
        case i@PendingDataInstance(nr, key) => InstanceExecutionData(i, null)
        case i@FilledDataInstance(nr, key, data) => ???
        case i@DataInstance(nr, key, data) => InstanceExecutionData(i, data.asInstanceOf[AnyRef])
      }
    }
    val updateOperations = instanceRef.map(AddInstance(_))
    transactionStatus = transactionStatus.updated(updateOperations)(slotNr)
  }


  def saveReturnValue(slotNr: Int, instanceNr: Int, returnValue: AnyRef) {
    val currentInstance = transactionStatus.slots(slotNr).instances(instanceNr)
    val updated = currentInstance.instance match {
      case PendingDataInstance(nr, key) => currentInstance.copy(instance = FilledDataInstance(nr, key, returnValue.asInstanceOf[java.io.Serializable]))
      case ReturnBindingKeyInstance(nr, key) => currentInstance.copy(ref = returnValue)
      case _ => ???
    }
    val operation = UpdateInstance(updated)
    transactionStatus = transactionStatus.updated(operation)(slotNr)
  }

  def getInstanceRef(slotNr: Int, instanceNr: Int): AnyRef = {
    transactionStatus.slots(slotNr).instances(instanceNr).ref
  }

  def getInstancetoMerge(slotNr: Int): Seq[CanonicalProtocolInstance] = {
    val startPosition = transactionStatus.slots(slotNr).transferMark
    val slotInstance = transactionStatus.slots(slotNr).instances.drop(startPosition).map(_.instance)
    val toSend = slotInstance.filter(_ match {
      case _: BindingKeyInstance => false
      case _: ReturnBindingKeyInstance => true
      case _: ExternalInstanceBinding => ???
      case _: PendingDataInstance => false
      case _: FilledDataInstance => true
      case _: DataInstance => false
    })
    transactionStatus = transactionStatus.updated(UpdateTransferMark(startPosition + toSend.size))(slotNr)
    toSend.toSeq
  }
}


trait TransactionStatusFlow {
  def recordCall(slotNr: Int, call: CanonicalProtocolMethodCall): Unit

  def bind(slotNr: Int, instance: CanonicalProtocolInstance, ref: AnyRef): Unit
}

private class SlotRecordEmbroider(slotNr: Int, transactionFlow: TransactionStatusFlow) extends RecordEmbroider {
  private val instanceCounter = new AtomicInteger(0)

  private def nextInstanceNr = instanceCounter.getAndAdd(1)

  @tailrec
  private def extractInternalInstanceNumber(argType: Class[_], argument: AnyRef): Int = {
    argument match {
      case null => ???
      case serializable: java.io.Serializable => {
        val instance = DataInstance(nextInstanceNr, Key.get(argType), serializable)
        transactionFlow.bind(slotNr, instance, serializable)
        instance.instanceNr
      }
      case recorder: CanonicalProtocolRecorderRef[_] if recorder.embroider == this => recorder.instance.instanceNr
      case recorder: CanonicalProtocolRecorderRef[_] => ???
      case proxy: ExternalBindingSwitch[_] => extractInternalInstanceNumber(argType, proxy.getInternalInstance.asInstanceOf[AnyRef])
      case _ => throw new RuntimeException("can not handle this argument type")
    }
  }

  def parseArguments(types: Array[Class[_]], arguments: Array[AnyRef]): Array[Int] = {
    arguments zip types map {
      case (arg, argType) => extractInternalInstanceNumber(argType, arg)
    }
  }

  def haft(call: ReflectionAbstractCall): CanonicalProtocolInstanceRef[_ <: AnyRef] = {
    val argsNr = call.args.map {
      arguments => parseArguments(call.method.getParameterTypes, arguments)
    }.getOrElse(Array[Int]())

    call match {
      case ReflectionVoidMethodCall(instance, method, args) => {
        val ccall = VoidMethodCall(instance.instanceNr, method, argsNr)
        val ref = VoidCanonicalProtocolInstanceRef
        transactionFlow.recordCall(slotNr, ccall)
        ref
      }
      case ReflectionInterfaceMethodCall(instance, method, args, returnType) => {
        val binding = ReturnBindingKeyInstance(nextInstanceNr, Key.get(returnType))
        val ref = new CanonicalProtocolRecorderRef[AnyRef](binding, this)
        transactionFlow.bind(slotNr, binding, ref)
        val ccall = InterfaceMethodCall(instance.instanceNr, method, argsNr, binding.instanceNr)
        transactionFlow.recordCall(slotNr, ccall)
        ref
      }
      case ReflectionDataMethodCall(instance, method, args, returnType) => {
        val pending = PendingDataInstance(nextInstanceNr, Key.get(returnType))
        val ref = new PromiseCanonicalProtocolInstanceRef()
        transactionFlow.bind(slotNr, pending, ref)
        val ccall = ReturnMethodCall(instance.instanceNr, method, argsNr, pending.instanceNr)
        transactionFlow.recordCall(slotNr, ccall)
        ref
      }
    }
  }

  def key[T](bindingKey: Key[T]): CanonicalProtocolInstanceRef[T] = {
    val canonicalInstance = BindingKeyInstance(nextInstanceNr, bindingKey)
    val ref = new CanonicalProtocolRecorderRef[T](canonicalInstance, this)
    transactionFlow.bind(slotNr, canonicalInstance, ref)
    ref
  }
}


private class InjectionTransactionWrappingContext(externalInstanceProviders: Map[ExternalServiceBind, InstanceProvider]) extends model.InjectionTransactionContext {

  def instanceProvider(externalBind: ExternalServiceBind): InstanceProvider = externalInstanceProviders(externalBind)

  def instanceProvider(injector: Injector): InstanceProvider = new DirectServiceInstanceProvider(injector)

  def dependenciesBindContextCreator(bindings: Seq[InstanceProvider]): DependenciesBindingContext = new TransactionExternalInstanceProvider(bindings)
}

class ExternalRecorderInstanceProvider(embroider: RecordEmbroider) extends InstanceProvider {
  def getInstance[T](key: Key[T]): T = embroider.key(key).getInstance
}

class DirectServiceInstanceProvider(val injector: Injector) extends InstanceProvider {
  def getInstance[T](key: Key[T]): T = injector.getExistingBinding(key).getProvider.get()
}


class TransactionExternalInstanceProvider(val instanceProviders: Seq[InstanceProvider]) extends ExternalInstanceProvider with DependenciesBindingContext {

  val withMemory = instanceProviders.map(new MemoizedInstanceProvider(_))

  def getReference[T](slotNr: Int, key: Key[T], instanceReferenceNr: Int): T = {
    withMemory(slotNr).getReference(key, instanceReferenceNr)
  }

  def getExternalInstanceProvider: ExternalInstanceProvider = this
}

class MemoizedInstanceProvider(val instanceProvider: InstanceProvider) {

  private val instanceMap: mutable.Map[Key[_], mutable.HashMap[Int, _]] = mutable.Map.empty

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


object VoidCanonicalProtocolInstanceRef extends CanonicalProtocolInstanceRef[AnyRef] {
  def getInstance: AnyRef = throw new IllegalAccessException("You are trying to call on void")
}

class PromiseCanonicalProtocolInstanceRef[T] extends CanonicalProtocolInstanceRef[T] {
  val promise: Promise[AnyRef] = Promise()

  def getInstance: T = {
    Await.result(promise.future, 100 nanos).asInstanceOf[T]
  }
}

class CanonicalProtocolRecorderRef[T](val instance: CanonicalProtocolInstance, val embroider: RecordEmbroider) extends InvocationHandler with CanonicalProtocolInstanceRef[T] {
  lazy val instanceRef: T = {
    val returnedType = instance.key.getTypeLiteral.getRawType.asInstanceOf[Class[_]]
    java.lang.reflect.Proxy.newProxyInstance(returnedType.getClassLoader(), Array(returnedType), this).asInstanceOf[T]
  }

  def getInstance: T = instanceRef

  def invoke(proxy: Any, method: Method, args: Array[AnyRef]): AnyRef = {
    val returnType = method.getReturnType
    if (method.getName.compare("toString") == 0) return this.toString
    if (returnType.equals(Void.TYPE)) {
      embroider.haft(ReflectionVoidMethodCall(instance, method, Option(args)))
      return null
    }
    if (returnType.isInterface()) {
      return embroider.haft(ReflectionInterfaceMethodCall(instance, method, Option(args), returnType)).getInstance
    }
    return embroider.haft(ReflectionDataMethodCall(instance, method, Option(args), returnType)).getInstance
  }
}
