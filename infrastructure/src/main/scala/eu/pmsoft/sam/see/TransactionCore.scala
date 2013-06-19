package eu.pmsoft.sam.see

import eu.pmsoft.sam.model._
import org.slf4j.LoggerFactory
import eu.pmsoft.sam.model.ExternalServiceBind

class TransactionCore {

}


private case class InstanceExecutionData(instance: CanonicalProtocolInstance, ref: AnyRef)


private abstract sealed class InstanceRegistryOperation

private case class AddInstance(data: InstanceExecutionData) extends InstanceRegistryOperation
private case class UpdateInstance(data: InstanceExecutionData) extends InstanceRegistryOperation
private case class UpdateTransferMark(mark : Int) extends InstanceRegistryOperation

private case class InstanceRegistrySlotStatus(instances: Vector[InstanceExecutionData] = Vector(), transferMark: Int = 0) {
  def updated(operation : InstanceRegistryOperation) : InstanceRegistrySlotStatus = operation match {
    case AddInstance(data) => this.copy(instances = this.instances :+ data)
    case UpdateInstance(data) => this.copy(instances = this.instances.updated(data.instance.instanceNr,data))
    case UpdateTransferMark(mark) => this.copy( transferMark = mark)
  }
}

private case class InstanceRegistryStatus(slots : Vector[InstanceRegistrySlotStatus]) {

  def updated(operation : InstanceRegistryOperation)(slotNr: Int) : InstanceRegistryStatus = this.copy(slots = this.slots.updated(slotNr,this.slots(slotNr).updated(operation)))

  def updated(operations : Iterable[InstanceRegistryOperation])(slotNr : Int): InstanceRegistryStatus = (this /: operations)( (status,operation) => status.updated(operation)(slotNr))

}



private class InjectionTransactionRecordManager(val injectionConfiguration: InjectionConfigurationElement, transportProvider: Seq[ExternalServiceBind] => Seq[SlotExecutionPipe]) extends TransactionStatusFlow with InstanceRegistry {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val externalBind = InjectionTransaction.getExternalBind(injectionConfiguration)
  val nrOfSlots = externalBind.size
  val externalInstanceProviders: Map[ExternalServiceBind, InstanceProvider] = externalBind.view.zipWithIndex.map {
    case (b, i) => b -> new ExternalRecorderInstanceProvider(new SlotRecordEmbroider(i, this))
  }.toMap

  private var transactionRecordStatus: InstanceRegistryStatus = InstanceRegistryStatus(Range(0, nrOfSlots).map(i => InstanceRegistrySlotStatus()).toVector)

  val externalExecutionManager = new ExecutionStackManager(transportProvider(externalBind).map(_.openPipe()).toVector, this)

  def recordCall(slotNr: Int, call: CanonicalProtocolMethodCall) = {
    externalExecutionManager.pushMethodCall(ProtocolMethodCall(slotNr, call))
    call match {
      case _: ReturnMethodCall => externalExecutionManager.flushExecution
      case _ =>
    }
  }

  def bind(slotNr: Int, instance: CanonicalProtocolInstance, ref: AnyRef) {
    val operation = AddInstance(InstanceExecutionData(instance, ref))
    transactionRecordStatus = transactionRecordStatus.updated(operation)(slotNr)
  }


  def getInstanceReferenceToTransfer(slotNr: Int): Seq[CanonicalProtocolInstance] = {
    val startPosition = transactionRecordStatus.slots(slotNr).transferMark
    val slotInstance = transactionRecordStatus.slots(slotNr).instances.drop(startPosition).map(_.instance)
    val toSend = slotInstance.takeWhile(_ match {
      case _: BindingKeyInstance => true
      case _: ReturnBindingKeyInstance => true
      case _: ExternalInstanceBinding => true
      case _: PendingDataInstance => true
      case _: FilledDataInstance => true
      case _: DataInstance => true
    })
    transactionRecordStatus = transactionRecordStatus.updated(UpdateTransferMark(startPosition + toSend.size))(slotNr)
    toSend.toSeq
  }

  def mergeInstances(slotNr: Int, instances: Seq[CanonicalProtocolInstance]) {
    val instanceRef: Seq[InstanceExecutionData] = instances.map {
      _ match {
        case i@BindingKeyInstance(nr, key) => InstanceExecutionData(i, ???)
        case i@ReturnBindingKeyInstance(nr, key) => InstanceExecutionData(i, ???)
        case i@ExternalInstanceBinding(nr, key) => InstanceExecutionData(i, ???)
        case i@PendingDataInstance(nr, key) => InstanceExecutionData(i, ???)
        case i@FilledDataInstance(nr, key, data) => {
          val projection = data.asInstanceOf[AnyRef]
          val delayed = transactionRecordStatus.slots(slotNr).instances(nr).ref.asInstanceOf[PromiseCanonicalProtocolInstanceRef[_]]
          delayed.promise success projection
          InstanceExecutionData(i,projection)
        }
        case i@DataInstance(nr, key, data) => InstanceExecutionData(i, ???)
      }
    }
    val updateOperations = instanceRef.map( UpdateInstance(_))
    transactionRecordStatus = transactionRecordStatus.updated(updateOperations)(slotNr)
  }


  def saveReturnValue(slotNr: Int, instanceNr: Int, returnValue: AnyRef) {
    ???
  }

  def getInstanceRef(slotNr: Int,instanceNr: Int): AnyRef = {
    transactionRecordStatus.slots(slotNr).instances(instanceNr).ref
  }

  def getInstancetoMerge(slotNr: Int): Seq[CanonicalProtocolInstance] = ???
}

case class ThreadMessage(data: Option[CanonicalRequest], exception: Option[Throwable] = None)

trait InstanceRegistry {

  def saveReturnValue(slotNr : Int ,instanceNr: Int, returnValue : AnyRef)

  def getInstanceRef(slotNr: Int,instanceNr: Int): AnyRef

  def getInstanceReferenceToTransfer(slotNr: Int): Seq[CanonicalProtocolInstance]

  def mergeInstances(slotNr: Int, instances: Seq[CanonicalProtocolInstance])

  def getInstancetoMerge(slotNr: Int): Seq[CanonicalProtocolInstance]
}


private class ExecutionStackManager(val pipes: Vector[ExecutionPipe], val instanceRegistry: InstanceRegistry) extends StackOfStack {
  private val logger = LoggerFactory.getLogger(this.getClass)
  //  val stack = new StackOfStack

  def bindTransaction {
    assert(threadExecutionStack.isEmpty)
    bindStack(-1)
  }


  def unbindTransaction {
    assert(threadExecutionStack.size == 1)
    unBindStack
  }

  def pushMethodCall(call: ProtocolMethodCall) {
    val currentStack = getCurrentStack
    logger.debug("recording call {}", call)
    val slotChange = currentStack.addCall(call)
    if (slotChange) {
      logger.debug("slotChange")
      pushProtocolExecution(false, currentStack)
    }
  }

  def flushExecution {
    val executionStackCall = getCurrentStack
    val expectedExecutionMark = executionStackCall.recordMark
    while (executionStackCall.executionMark < expectedExecutionMark) {
      logger.trace("force execution. CurrentMark/Expected [{},{}]", executionStackCall.executionMark, expectedExecutionMark)
      pushProtocolExecution(true, executionStackCall)
    }
    logger.trace("flush execution done")
  }

  def getPipe(slotNr: Int): ExecutionPipe = pipes(slotNr)

  def executeCalls(slotNr: Int, calls: Seq[CanonicalProtocolMethodCall]) = {
    calls foreach {
      call => {
        logger.debug("execution call {}", call)
        val instanceRef: AnyRef = instanceRegistry.getInstanceRef(slotNr,call.instanceNr)
        val argumentsRefs: Array[AnyRef] = call.arguments.map( instanceRegistry.getInstanceRef(slotNr, _ ) )

        val method = instanceRef.getClass.getInterfaces.flatMap(_.getMethods).find(_.hashCode() == call.methodSignature).getOrElse(???)
        try {
          val resultValue = argumentsRefs.size match {
            case 0 => method.invoke(instanceRef)
            case _ => method.invoke(instanceRef, argumentsRefs: _*)
          }
          call.returnInstance.map(
            instanceRegistry.saveReturnValue(slotNr,_,resultValue)
          )
        } catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
  }

  def pushProtocolExecution(forceWait: Boolean, currentStack: MethodExecutionStack) {
    if (currentStack.waitingStatus) {
      val pipe = getPipe(currentStack.waitingSlotNr)
      val response = if (forceWait) {
        Some(pipe.waitResponse)
      } else {
        pipe.pollResponse
      }
      response.map {
        res =>
          res.exception.map(throw _)
          res.data match {
            case None => {
              logger.trace("returning thread execution")
              currentStack.markDone
            }
            case Some(data) => {
              instanceRegistry.mergeInstances(currentStack.waitingSlotNr, data.instances)
              if (data.closeThread) {
                logger.trace("handle returning message call - merge only for closing execution thread")
                assert(data.calls.isEmpty)
                currentStack.markDone
              } else {
                logger.trace("handle returning message call")
                if (!data.calls.isEmpty) {
                  bindStack(currentStack.waitingSlotNr)
                  executeCalls(currentStack.waitingSlotNr, data.calls)
                  unBindStack
                }
                val finishData = CanonicalRequest(instanceRegistry.getInstancetoMerge(currentStack.waitingSlotNr), Seq(), true)
                val finishMessage = ThreadMessage(Some(finishData))
                pipe.sendMessage(finishMessage)
              }
            }
          }
      }
    } else {
      val request = currentStack.createStackRequest
      val pipe = getPipe(currentStack.waitingSlotNr)
      pipe.sendMessage(ThreadMessage(Some(request)))
      assert(currentStack.waitingStatus)
    }
  }
}


private trait StackOfStack {
  private val logger = LoggerFactory.getLogger(this.getClass)
  var threadExecutionStack = Vector[MethodExecutionStack]()

  val instanceRegistry: InstanceRegistry

  def bindStack(mainTargetSlot: Int) {
    logger.trace("Stack Execution PUSH for slot {}", mainTargetSlot)
    threadExecutionStack = new MethodExecutionStack(mainTargetSlot, instanceRegistry) +: threadExecutionStack
  }

  def unBindStack {
    logger.trace("Stack Execution POP")
    assert(!threadExecutionStack.isEmpty)
    assert(threadExecutionStack.head.serviceCallStack.isEmpty)
    threadExecutionStack = threadExecutionStack.tail
  }

  def getCurrentStack = threadExecutionStack.head
}

case class CanonicalRequest(instances: Seq[CanonicalProtocolInstance], calls: Seq[CanonicalProtocolMethodCall], closeThread: Boolean)

case class ProtocolMethodCall(slot: Int, call: CanonicalProtocolMethodCall)

private class MethodExecutionStack(val slotReference: Int, val instanceRegistries: InstanceRegistry) {
  var serviceCallStack = Vector[ProtocolMethodCall]()
  var waitingStatus = false
  var waitingSlotNr: Int = _
  var executionMark = 0
  var sendMark = 0
  var recordMark = 0

  def markDone {
    executionMark = sendMark
    waitingStatus = false
  }

  def addCall(call: ProtocolMethodCall) = {
    val f = serviceCallStack.headOption
    serviceCallStack = serviceCallStack :+ call
    recordMark += 1
    f.map {
      firstCall => firstCall.slot != call.slot
    }.getOrElse(false)
  }

  def prepareMergeInstanceRequest: CanonicalRequest = {
    val instanceReference = instanceRegistries.getInstanceReferenceToTransfer(waitingSlotNr)
    CanonicalRequest(instanceReference, Seq(), false)
  }

  def createStackRequest: CanonicalRequest = {
    assert(!waitingStatus, "Trying to send request on waiting status")
    waitingStatus = true
    if (serviceCallStack.isEmpty) {
      return prepareMergeInstanceRequest
    }
    val methodCalls = getAccesibleCalls
    assert(!methodCalls.isEmpty, "the serviceCallStack is not empty and list of method calls is empty???, critical exceptions")
    sendMark += methodCalls.size
    val targetSlot = methodCalls.head.slot
    val instanceReference = instanceRegistries.getInstanceReferenceToTransfer(targetSlot)
    waitingSlotNr = targetSlot
    CanonicalRequest(instanceReference, methodCalls.map(_.call).toSeq, false)
  }

  private def getAccesibleCalls: Vector[ProtocolMethodCall] = {
    serviceCallStack.headOption.map {
      first => {
        val toSend = serviceCallStack.takeWhile(_.slot == first.slot)
        serviceCallStack = serviceCallStack.drop(toSend.size)
        toSend
      }
    }.getOrElse(Vector())
  }
}


