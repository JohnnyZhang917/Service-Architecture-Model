package eu.pmsoft.sam.see

import com.google.inject.{Key, Injector}
import org.slf4j.LoggerFactory
import eu.pmsoft.sam.model._

object TransactionProvider {

}

private class TransactionExecutionManager(val headInjector: Injector, val transportContext : TransactionTransportContext) extends TransactionStatusFlow with InstanceRegistry {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private var transactionStatus: InstanceRegistryStatus = InstanceRegistryStatus(Vector(InstanceRegistrySlotStatus()))

  val returnLoopInstanceProvider: RecordEmbroider = new SlotRecordEmbroider(0, new ServerCanonicalInstanceCreationSchema({
    () => nextInstanceNr(0)
  }), this)

  def nextInstanceNr(slotNr: Int): Int = transactionStatus.slots(slotNr).instances.size

  val executionManager = new ExecutionStackManager(Vector(transportContext.loopBackPipe), this)

  def recordCall(slotNr: Int, call: CanonicalProtocolMethodCall) {
    executionManager.pushMethodCall(ProtocolMethodCall(slotNr, call))
    call match {
      case _: ReturnMethodCall => executionManager.flushExecution
      case _ =>
    }
  }

  def bind(slotNr: Int, instance: CanonicalProtocolInstance, ref: AnyRef) {
    logger.debug("on slot {} bind instance {} ", slotNr, instance)
    val operation = AddInstance(InstanceExecutionData(instance, ref))
    transactionStatus = transactionStatus.updated(operation)(0)

  }

  def getInstanceReferenceToTransfer(slotNr: Int): Seq[CanonicalProtocolInstance] = {
    val startPosition = transactionStatus.slots(slotNr).transferMark
    val slotInstance = transactionStatus.slots(slotNr).instances.drop(startPosition).map(_.instance)
    val toSend = slotInstance.filter(CanonicalProtocol.isServerInstance _)
    transactionStatus = transactionStatus.updated(UpdateTransferMark(startPosition + slotInstance.size))(slotNr)
    val total = toSend ++ getInstancetoMerge(slotNr)
    total
  }

  def getInstancetoMerge(slotNr: Int): Seq[CanonicalProtocolInstance] = {
    val startPosition = transactionStatus.slots(slotNr).mergeMark
    val slotInstance = transactionStatus.slots(slotNr).instances.drop(startPosition).map(_.instance)
    val merged = slotInstance.takeWhile(_ match {
      case _: ClientPendingDataInstance => false
      case _ => true
    })
    transactionStatus = transactionStatus.updated(UpdateMergeMark(startPosition + merged.size))(slotNr)
    merged.filter(_ match {
      case _: ClientFilledDataInstance => true
      case _ => false
    }).toSeq
  }

  def mergeInstances(slotNr: Int, instances: Seq[CanonicalProtocolInstance]) {
    logger.debug("merge instances {}", instances)
    val updateOperations = instances.map {
      _ match {
        case i@ClientBindingKeyInstance(nr, key) => AddInstance(InstanceExecutionData(i, headInjector.getInstance(key).asInstanceOf[AnyRef]))
        case i@ClientReturnBindingKeyInstance(nr, key) => AddInstance(InstanceExecutionData(i, null))
        case i@ClientExternalInstanceBinding(nr, key) => AddInstance(InstanceExecutionData(i, new CanonicalProtocolRecorderRef[AnyRef](i, returnLoopInstanceProvider).getInstance))
        case i@ClientPendingDataInstance(nr, key) => AddInstance(InstanceExecutionData(i, null))
        case i@ClientFilledDataInstance(nr, key, data) => throw new IllegalStateException()
        case i@ClientDataInstance(nr, key, data) => AddInstance(InstanceExecutionData(i, data.asInstanceOf[AnyRef]))
        case i@ServerFilledDataInstance(instanceNr, key, data) => {
          val projection = data.asInstanceOf[AnyRef]
          val delayed = transactionStatus.slots(slotNr).instances(instanceNr).ref.asInstanceOf[PromiseCanonicalProtocolInstanceRef[_]]
          delayed.promise success projection
          UpdateInstance(InstanceExecutionData(i, projection))
        }

        case _ => throw new IllegalStateException()
      }
    }
    transactionStatus = transactionStatus.updated(updateOperations)(slotNr)
  }


  def saveReturnValue(slotNr: Int, instanceNr: Int, returnValue: AnyRef) {
    val currentInstance = transactionStatus.slots(slotNr).instances(instanceNr)
    val updated = currentInstance.instance match {
      case ClientPendingDataInstance(nr, key) => currentInstance.copy(instance = ClientFilledDataInstance(nr, key, returnValue.asInstanceOf[java.io.Serializable]))
      case ClientReturnBindingKeyInstance(nr, key) => currentInstance.copy(ref = returnValue)
      case _ => ???
    }
    val operation = UpdateInstance(updated)
    transactionStatus = transactionStatus.updated(operation)(slotNr)
  }

  def getInstanceRef(slotNr: Int, instanceNr: Int): AnyRef = {
    transactionStatus.slots(slotNr).instances(instanceNr).ref
  }


}

private class ServerCanonicalInstanceCreationSchema(val next: () => Int) extends CanonicalInstanceCreationSchema {


  def keyBinding(key: Key[_]): CanonicalProtocolInstance = ServerBindingKeyInstance(next(), key)

  def externalBinding(key: Key[_]): CanonicalProtocolInstance = ServerExternalInstanceBinding(next(), key)

  def dataBinding(key: Key[_], ref: java.io.Serializable): CanonicalProtocolInstance = ServerDataInstance(next(), key, ref)

  def returnBind(key: Key[_]): CanonicalProtocolInstance = ServerReturnBindingKeyInstance(next(), key)

  def pendingBind(key: Key[_]): CanonicalProtocolInstance = ServerPendingDataInstance(next(), key)
}
