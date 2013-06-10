package eu.pmsoft.sam.see


import com.google.inject.{Injector, Key}
import java.lang.reflect.{InvocationHandler, Method}
import eu.pmsoft.sam.injection.{DependenciesBindingContext, ExternalInstanceProvider, ExternalBindingSwitch}
import scala.annotation.tailrec
import eu.pmsoft.sam.model._
import scala.collection.mutable
import java.util.concurrent.atomic.AtomicInteger
import scala._
import scala.AnyRef
import scala.Some
import eu.pmsoft.sam.model.ExternalServiceBind

object CanonicalRecordingLayer {

  def apply(transportProvider: Seq[ExternalServiceBind] => Seq[SlotExecutionPipe]) = new CanonicalRecordingLayer(transportProvider)

}

class CanonicalRecordingLayer(transportProvider: Seq[ExternalServiceBind] => Seq[SlotExecutionPipe]) {

  def createContext(injectionConfiguration: InjectionConfigurationElement) = {
    new InjectionTransactionExecutionContext(injectionConfiguration, transportProvider)
  }

}

class InjectionTransactionExecutionContext(injectionConfiguration: InjectionConfigurationElement, transportProvider: Seq[ExternalServiceBind] => Seq[SlotExecutionPipe]) extends InjectionTransactionContext {

  private val externalBind = InjectionTransaction.getExternalBind(injectionConfiguration)

  private val executionPipes = transportProvider(externalBind)

  private val headInjector = InjectionTransaction.glueInjector(injectionConfiguration)
  require(headInjector.isDefined)

  val executionContext = new CanonicalProtocolExecutionContext(headInjector.get, externalBind, executionPipes)

  private val methodCallRecord = externalBind.map {
    b => b -> new CanonicalProtocolServiceRecordContext(executionContext.clientContext.recordRegistries(b))
  }.toMap

  private val bindMap = externalBind.map(b => b -> new ClientRecordingInstanceRegistry(methodCallRecord(b), methodCallRecord(b).createKeyBinding _)).toMap

  def instanceProvider(externalBind: ExternalServiceBind): InstanceProvider = bindMap(externalBind)

  def dependenciesBindContextCreator(bindings: Seq[InstanceProvider]): DependenciesBindingContext = new InstanceProviderTransactionContext(bindings.map(s => new MemoizedInstanceProvider(s)))

  def instanceProvider(injector: Injector): InstanceProvider = new DirectServiceInstanceProvider(injector)

  val transaction: InjectionTransaction = {

    val rootNode = InjectionTransaction.createNode(this,injectionConfiguration)
    new InjectionTransaction(headInjector.get, rootNode)
  }

}

class InstanceProviderTransactionContext(val bindServices: Seq[MemoizedInstanceProvider]) extends ExternalInstanceProvider with DependenciesBindingContext {

  def getReference[T](slotNr: Int, key: Key[T], instanceReferenceNr: Int): T = bindServices(slotNr).getReference(key, instanceReferenceNr)

  def getExternalInstanceProvider: ExternalInstanceProvider = this

}


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


case class SlotMethodCallBuffer(slotNr: Int, calls: Seq[CanonicalProtocolMethodCall])


//private object TransactionRecordContext {
//  def apply(externalBindSet: Seq[ExternalServiceBind], slotPipes: Seq[SlotExecutionPipe] ) = new TransactionRecordContext(externalBindSet,slotPipes)
//}

//private class TransactionRecordContext(val instanceRegistries: Seq[ExternalServiceBind], val slotPipes: Seq[SlotExecutionPipe] ) extends SimpleLazyProtocol(instanceRegistries.size) {
//
//  private lazy val pipesVector = slotPipes.toVector
//
//  def getPipeForSlot(slotNr: Int): SlotExecutionPipe = pipesVector(slotNr)
//
//  private lazy val recordMap = instanceRegistries.view.zipWithIndex.map {
//    case (b, i) => b -> new CanonicalProtocolServiceRecordContext(i, this)
//  }.toMap
//
//  private lazy val bindMap = instanceRegistries.map(b => b -> new ClientRecordingInstanceRegistry(b, getRecordContext(b))).toMap
//
//  def get(bind: ExternalServiceBind): ClientRecordingInstanceRegistry = bindMap.get(bind).get
//
//  private def getRecordContext(bind: ExternalServiceBind): CanonicalProtocolServiceRecordContext = recordMap.get(bind).get
//
//  def execute(message: CanonicalProtocolMessage): CanonicalProtocolMessage = ???
//}
//
//
//
//abstract class SimpleLazyProtocol(nrOfSlots: Int) extends CanonicalProtocol  {
//
////  val logger = LoggerFactory.getLogger(getClass)
////
////  var slotInstances: Vector[Vector[CanonicalProtocolInstance]] = Range(0, nrOfSlots).map(i => Vector[CanonicalProtocolInstance]()).toVector
////
////  var methodCalls: Vector[SlotMethodCallBuffer] = Vector()
////
////  def pushInstanceCreation(slotNr: Int, instance: CanonicalProtocolInstance): CanonicalProtocolInstance = {
////    slotInstances = slotInstances.updated(slotNr, instance +: slotInstances(slotNr))
////    logger.debug("instance creation. Slot {} , Instance {}", slotNr, instance)
////    instance
////
////  }
////
////  def pushMethodExecution(slotNr: Int, call: CanonicalProtocolMethodCall) {
////    logger.debug("method call. Slot {} , call {}", slotNr, call.toCallString)
////    methodCalls.headOption match {
////      case None => {
////        methodCalls = Vector(SlotMethodCallBuffer(slotNr, Seq(call)))
////      }
////      case Some(buff) if buff.slotNr == slotNr => methodCalls = buff.copy(calls = call +: buff.calls) +: methodCalls.tail
////      case Some(buff) => {
////        methodCalls = SlotMethodCallBuffer(slotNr, Seq(call)) +: methodCalls
////        triggerExecutionProcess
////      }
////    }
////  }
//
//  var sendPipeLine: Vector[SlotMethodCallBuffer] = Vector()
//
//  def instanceTransfer(slotNr: Int): Seq[CanonicalProtocolInstance] = {
//    // TODO
//    Seq()
//  }
//
//
//  def mergeInstance(slotNr: Int, instances: Seq[CanonicalProtocolInstance]) {
//
//  }
//
//  def saveInstance(slotNr: Int, instanceNr: Int, value: AnyRef) {
//
//  }
//
//  def executeCalls(slotNr: Int, calls: Seq[CanonicalProtocolMethodCall]) {
//    calls foreach {
//      call => {
//        val instance = slotInstances(slotNr)(call.instanceNr)
//        val instanceRef: Object = null
//        val arguments: Array[AnyRef] = Array()
//
//        //        val api = instanceRef.getClass.getInterfaces
//        //          api.flatMap(_.getDeclaredMethods()).filter(_.hashCode() == call.method).head
//        val method = call.method
//
//        val returnValue = if (arguments.size == 0) {
//          method.invoke(instanceRef)
//        } else {
//          method.invoke(instanceRef, arguments)
//        }
//        call.returnInstance.map(saveInstance(slotNr, _, returnValue))
//      }
//    }
//  }
//
//  private var executionLine: Option[Future[Unit]] = None
//
//  def triggerExecutionProcess {
//    executionLine match {
//      case None => executionLine = Some(startSlotThreadExecution)
//      case Some(executing) => {
//        val nextStep = executing flatMap {
//          case () => startSlotThreadExecution
//        }
//        executionLine = Some(nextStep)
//      }
//    }
//  }
//
//  def getPipeForSlot(slotNr: Int) : SlotExecutionPipe
//
//  private def startSlotThreadExecution: Future[Unit] = {
//    val toSend = methodCalls.last
//    methodCalls = methodCalls.dropRight(1)
//    val targetSlot = toSend.slotNr
//    val pipe = getPipeForSlot(targetSlot)
//    val execution = pipe.sendProtocolExecution(CanonicalProtocolMessage(instanceTransfer(targetSlot), toSend.calls, false))
//    execution map {
//      response => {
//        mergeInstance(targetSlot, response.instances)
//        executeCalls(targetSlot, response.calls)
//      }
//    }
//  }
//
////  def realizeMethodExecution(slotNr: Int, returnCall: ReturnMethodCall): Future[AnyRef] = {
////    pushMethodExecution(slotNr, returnCall)
////    triggerExecutionProcess
////    val p = Promise[AnyRef]()
////    //TODO bind to execution
////    p.future
////  }
//}
//
//
//
//
class CanonicalProtocolServiceRecordContext(val protocolRegistry: CanonicalProtocolRecordRegistry) extends InstanceMethodCallRecordingActions {

  private val instanceCounter = new AtomicInteger(0)

  private def nextInstanceNr = instanceCounter.addAndGet(1)

  def createKeyBinding(key: Key[_]): CanonicalProtocolInstance = protocolRegistry.pushInstanceCreation(BindingKeyInstance(nextInstanceNr, key))

  private def createExternalInstanceBinding(externalSlotInstance: CanonicalInstanceRecorder[_]): CanonicalProtocolInstance = {
    require(false, "TODO - map between slots")
    protocolRegistry.pushInstanceCreation(ExternalInstanceBinding(nextInstanceNr, externalSlotInstance.instance.key))
  }

  private def createDataBinding(key: Key[_], serializable: java.io.Serializable): CanonicalProtocolInstance = {
    protocolRegistry.pushInstanceCreation(DataInstance(nextInstanceNr, key, serializable))
  }

  def parseArguments(types: Array[Class[_]], arguments: Array[AnyRef]): Array[Int] = {
    arguments zip types map {
      case (arg, argType) => extractInternalInstanceNumber(argType, arg)
    }
  }

  @tailrec
  private def extractInternalInstanceNumber(argType: Class[_], argument: AnyRef): Int = {
    argument match {
      case null => -1
      case serializable: java.io.Serializable => createDataBinding(Key.get(argType), serializable).instanceNr
      case recorder: CanonicalInstanceRecorder[_] if recorder.context == this => recorder.instance.instanceNr
      case recorder: CanonicalInstanceRecorder[_] => createExternalInstanceBinding(recorder).instanceNr
      case proxy: ExternalBindingSwitch[_] => extractInternalInstanceNumber(argType, proxy.getInternalInstance.asInstanceOf[AnyRef])
      case _ => throw new RuntimeException("can not handle this argument type")
    }
  }

  def recordExternalMethodCall(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef]): Unit = {
    val arguments = parseArguments(method.getParameterTypes, args)
    protocolRegistry.pushMethodExecution(VoidMethodCall(instance.instanceNr, method, arguments))
  }

  def recordExternalMethodCallWithInterfaceReturnType(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef], returnType: Class[_]): CanonicalProtocolInstance = {
    val arguments = parseArguments(method.getParameterTypes, args)
    val binding = protocolRegistry.pushInstanceCreation(ReturnBindingKeyInstance(nextInstanceNr, Key.get(returnType)))
    protocolRegistry.pushMethodExecution(InterfaceMethodCall(instance.instanceNr, method, arguments, binding.instanceNr))
    binding
  }

  def recordExternalMethodCallWithReturnType(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef], returnType: Class[_]): AnyRef = {
    val finalArgs = if (args == null) Array[AnyRef]() else args
    val arguments = parseArguments(method.getParameterTypes, finalArgs)
    val binding = protocolRegistry.pushInstanceCreation(PendingDataInstance(nextInstanceNr, Key.get(returnType)))
    protocolRegistry.realizeMethodExecution(ReturnMethodCall(instance.instanceNr, method, arguments, binding.instanceNr))
  }
}

trait InstanceProvider {
  def getInstance[T](key: Key[T]): T
}

class ClientRecordingInstanceRegistry(val context: InstanceMethodCallRecordingActions,
                                      val createKeyBinding: Key[_] => CanonicalProtocolInstance
                                       ) extends InstanceProvider {

  def getInstance[T](key: Key[T]): T = {
    new CanonicalInstanceRecorder(context, createKeyBinding(key)).getInstance
  }
}

class DirectServiceInstanceProvider(val injector: Injector) extends InstanceProvider {
  def getInstance[T](key: Key[T]): T = injector.getExistingBinding(key).getProvider.get()
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

trait InstanceMethodCallRecordingActions {
  def recordExternalMethodCall(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef]): Unit

  def recordExternalMethodCallWithInterfaceReturnType(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef], returnType: Class[_]): CanonicalProtocolInstance

  def recordExternalMethodCallWithReturnType(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef], returnType: Class[_]): AnyRef
}

class CanonicalInstanceRecorder[T](val context: InstanceMethodCallRecordingActions, val instance: CanonicalProtocolInstance) extends InvocationHandler {

  lazy val instanceRef: T = {
    val returnedType = instance.key.getTypeLiteral.getRawType.asInstanceOf[Class[_]]
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
      val handler = new CanonicalInstanceRecorder(context, canonicalInstance)
      return handler.getInstance;
    }
    return context.recordExternalMethodCallWithReturnType(instance, method, args, returnType)
  }

  def getInstance: T = instanceRef
}

