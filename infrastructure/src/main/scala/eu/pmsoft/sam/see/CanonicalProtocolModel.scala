package eu.pmsoft.sam.see

import org.slf4j.{Logger, LoggerFactory}
import scala._
import scala.AnyRef
import scala.Some
import eu.pmsoft.sam.model._
import com.google.inject.Injector
import scala.concurrent.Future


object CanonicalProtocolModel {
}






//trait InstanceMethodCallRecordingActions {
//
//  def recordExternalMethodCall(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef]): Unit
//
//  def recordExternalMethodCallWithInterfaceReturnType(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef], returnType: Class[_]): CanonicalProtocolInstance
//
//  def recordExternalMethodCallWithReturnType(instance: CanonicalProtocolInstance, method: Method, args: Array[AnyRef], returnType: Class[_]): AnyRef
//}


//trait CanonicalProtocolRecordRegistry {
//  def pushInstanceCreation(instance: CanonicalProtocolInstance): CanonicalProtocolInstance
//
//  def pushMethodExecution(call: CanonicalProtocolMethodCall)
//
//  def realizeMethodExecution(returnCall: ReturnMethodCall): AnyRef
//}


//trait ProcotolCalls {
//  def pushInstanceCreation(instance: CanonicalProtocolInstance)(slotNr: Int): CanonicalProtocolInstance
//
//  def pushMethodExecution(call: CanonicalProtocolMethodCall)(slotNr: Int): Unit
//
//  def realizeMethodExecution(returnCall: ReturnMethodCall)(slotNr: Int): AnyRef
//
//}


//class CanonicalProtocolExecutionContext(injector: Injector, externalBind: Seq[ExternalServiceBind], executionPipes: Seq[SlotExecutionPipe]) {
//
//  val clientContext = new ClientRecordExecutionManager(externalBind,executionPipes)
//
//  val serverExecutionManager = new ServerExecutionManager(injector)
//
//}
//
//
//class CanonicalProtocolRecordRegistrySlot(val slotNr: Int, val transactionGlobalExecutionContext: ExecutionInstanceRegistry) extends CanonicalProtocolRecordRegistry {
//  def pushInstanceCreation(instance: CanonicalProtocolInstance): CanonicalProtocolInstance = transactionGlobalExecutionContext.pushInstanceCreation(slotNr, instance)
//
//  def pushMethodExecution(call: CanonicalProtocolMethodCall) = transactionGlobalExecutionContext.pushMethodExecution(slotNr, call)
//
//  def realizeMethodExecution(returnCall: ReturnMethodCall): AnyRef = transactionGlobalExecutionContext.realizeMethodExecution(slotNr, returnCall)
//}
//
//
//
//class ClientRecordExecutionManager(externalBind: Seq[ExternalServiceBind], executionPipes: Seq[SlotExecutionPipe]) extends ExecutionInstanceRegistry {
//  val recordRegistries = externalBind.view.zipWithIndex.map {
//    case (b, i) => b -> new CanonicalProtocolRecordRegistrySlot(i, this)
//  }.toMap
//
//  val status: InstanceRegistryStatus = new InstanceRegistryStatus(externalBind.size)
//
//  def pushCanonicalProtocolCall(slot: Int, message: CanonicalProtocolMessage) = executionPipes(slot).sendProtocolExecution(message)
//}
//
//class ServerExecutionManager(val serviceInjector : Injector) extends ExecutionInstanceRegistry with CanonicalProtocolExecutor {
//
//  val status: InstanceRegistryStatus = new InstanceRegistryStatus(2)
//
//  var objectReferences : Map[Int,AnyRef] = Map()
//
//  def pushCanonicalProtocolCall(slot: Int, message: CanonicalProtocolMessage): Future[CanonicalProtocolMessage] = ???
//
//  def bindThread {}
//
//  def unbindThread { }
//
//  private def createReferenceForInstance(instance : CanonicalProtocolInstance): AnyRef = {
//    instance match {
//      case BindingKeyInstance(_,key) => serviceInjector.getInstance(key).asInstanceOf[AnyRef]
//      case ReturnBindingKeyInstance(_,_) => ???
//      case ExternalInstanceBinding(_,key) => serviceInjector.getInstance(key).asInstanceOf[AnyRef]
//      case PendingDataInstance(_,_) => ???
//      case FilledDataInstance(_,_,data) => data.asInstanceOf[AnyRef]
//      case DataInstance(_,_,data) => data.asInstanceOf[AnyRef]
//    }
//  }
//
//  def getInstanceRef(instanceNr: Int): AnyRef = {
//    logger.debug("get instance ref {}", instanceNr)
//    val instance = status.slotInstances(0)(instanceNr-1)
//    objectReferences.get(instanceNr) match {
//      case Some(ref) => ref
//      case None => {
//        val ref = createReferenceForInstance(instance)
//        objectReferences = objectReferences + (instanceNr -> ref)
//        ref
//      }
//    }
//
//  }
//
//  def getArguments(instanceNr: Array[Int]): Array[AnyRef] = instanceNr map getInstanceRef _
//
//  def saveReturnValue(instanceNr: Int, value: java.io.Serializable) {
//    logger.debug("result instance for {} is {}", instanceNr, value)
//    val instance = status.slotInstances(0)(instanceNr-1)
//    instance match {
//      case PendingDataInstance(iNr,key) => {
//        status.slotInstances = status.slotInstances.updated(0,status.slotInstances(0).updated(instanceNr-1, FilledDataInstance(iNr,key,value)) )
//      }
//      case _ => ???
//    }
//  }
//
//  def mergeInstances(instances: Seq[CanonicalProtocolInstance]) {
//    logger.debug("merge instances {}", instances)
//    status.slotInstances = status.slotInstances.updated(0, status.slotInstances(0) ++ instances)
//  }
//
//  def createCloseHomotopyMessage: CanonicalProtocolMessage = {
//    // TODO
//    CanonicalProtocolMessage(Seq(),Seq(),false)
//  }
//}
//
//case class SlotMethodCallBuffer(slotNr: Int, calls: Seq[CanonicalProtocolMethodCall])
//
//class InstanceRegistryStatus(val nrOfSlots: Int) {
//  var slotInstances: Vector[Vector[CanonicalProtocolInstance]] = Range(0, nrOfSlots).map(i => Vector[CanonicalProtocolInstance]()).toVector
//
//  var instanceSendMark : Vector[Int] = (0 to nrOfSlots).map (i => 0).toVector
//
//  var methodCalls: Vector[SlotMethodCallBuffer] = Vector()
//}
//
//trait ExecutionInstanceRegistry {
//
//  def pushCanonicalProtocolCall(slot: Int, message : CanonicalProtocolMessage) : Future[CanonicalProtocolMessage]
//
//  var executionLine : Option[Future[CanonicalProtocolMessage]] = None
//
//  def triggerExecutionProcess: Unit = {
//    val callsToSend = status.methodCalls.last
//    status.methodCalls = status.methodCalls.dropRight(1)
//    val targetSlot = callsToSend.slotNr
//    val message = CanonicalProtocolMessage(instanceTransfer(targetSlot), callsToSend.calls, false)
//
//    executionLine match {
//      case None => {
//        executionLine = Some(pushCanonicalProtocolCall(targetSlot,message))
//      }
//      case Some(alreadyRunning) => {
//        ???
//      }
//    }
//
//
//  }
//
//  def instanceTransfer(slotNr: Int): Seq[CanonicalProtocolInstance] = {
//    val instance = status.slotInstances(slotNr)
//    val sendMark = status.instanceSendMark(slotNr)
//    val instanceToSend = instance.drop(sendMark)
//    status.instanceSendMark = status.instanceSendMark.updated(slotNr, sendMark + instanceToSend.size)
//    instanceToSend
//  }
//
//  val logger = LoggerFactory.getLogger(getClass)
//
//  val status: InstanceRegistryStatus
//
//  def pushInstanceCreation(slotNr: Int, instance: CanonicalProtocolInstance): CanonicalProtocolInstance = {
//    status.slotInstances = status.slotInstances.updated(slotNr, status.slotInstances(slotNr) :+ instance )
//    logger.debug("instance creation. Slot {} , Instance {}", slotNr, instance)
//    instance
//
//  }
//
//  def pushMethodExecution(slotNr: Int, call: CanonicalProtocolMethodCall) {
//    logger.debug("method call. Slot {} , call [{}]", slotNr, call.toCallString)
//    status.methodCalls.headOption match {
//      case None => {
//        status.methodCalls = Vector(SlotMethodCallBuffer(slotNr, Seq(call)))
//      }
//      case Some(buff) if buff.slotNr == slotNr => status.methodCalls = buff.copy(calls = call +: buff.calls) +: status.methodCalls.tail
//      case Some(buff) => {
//        status.methodCalls = SlotMethodCallBuffer(slotNr, Seq(call)) +: status.methodCalls
//        triggerExecutionProcess
//      }
//    }
//  }
//
//  def realizeMethodExecution(slotNr: Int, returnCall: ReturnMethodCall): AnyRef = {
//    pushMethodExecution(slotNr, returnCall)
//    triggerExecutionProcess
//    //    val p = Promise[AnyRef]()
//    Thread.sleep(100)
//    //TODO bind to execution
//    ???
//  }
//
//}
//
//
//trait CanonicalProtocolEnd {
//  def execute(message: CanonicalProtocolMessage): CanonicalProtocolMessage
//}
//
//
//trait CanonicalProtocolExecutor extends CanonicalProtocolEnd {
//  val executionLogger = LoggerFactory.getLogger(getClass)
//
//  def bindThread: Unit
//
//  def unbindThread: Unit
//
//  def getInstanceRef(instanceNr: Int): AnyRef
//
//  def getArguments(instanceNr: Array[Int]): Array[AnyRef]
//
//  def saveReturnValue(instanceNr: Int, value: java.io.Serializable): Unit
//
//  def mergeInstances(instances: Seq[CanonicalProtocolInstance]): Unit
//
//  def createCloseHomotopyMessage: CanonicalProtocolMessage
//
//  private def executeCalls(calls: Seq[CanonicalProtocolMethodCall]) {
//    calls foreach {
//      call => {
//        executionLogger.debug("execution call {}" , call)
//        val instanceRef: AnyRef = getInstanceRef(call.instanceNr)
//        val argumentsRefs: Array[AnyRef] = getArguments(call.arguments)
//
//        val method = instanceRef.getClass.getInterfaces.flatMap( _.getMethods).find( _.hashCode() == call.methodSignature).getOrElse(???)
//        try {
//          val resultValue = argumentsRefs.size match {
//            case 0 => method.invoke(instanceRef)
//            case _ => method.invoke(instanceRef, argumentsRefs : _*)
//          }
//          call.returnInstance.map(saveReturnValue(_, resultValue.asInstanceOf[java.io.Serializable]))
//        } catch {
//          case e: Exception => e.printStackTrace()
//        }
//      }
//    }
//  }
//
//  def execute(message: CanonicalProtocolMessage): CanonicalProtocolMessage = {
//    bindThread
//    mergeInstances(message.instances)
//    executeCalls(message.calls)
//    val responseMessage = createCloseHomotopyMessage
//    unbindThread
//    responseMessage
//  }
//}
//
//
