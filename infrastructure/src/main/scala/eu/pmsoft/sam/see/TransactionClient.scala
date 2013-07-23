package eu.pmsoft.sam.see

import org.slf4j.LoggerFactory

import eu.pmsoft.sam.model._

object TransactionClient {

}

private class InjectionTransactionRecordManager(val injectionConfiguration: InjectionConfigurationElement, transportProvider: Seq[ExternalServiceBind] => Seq[ExecutionPipe]) extends TransactionStatusFlow with InstanceRegistry {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val externalBind = InjectionTransaction.getExternalBind(injectionConfiguration)
  val nrOfSlots = externalBind.size
  val externalInstanceProviders: Map[ExternalServiceBind, InstanceProvider] = externalBind.zipWithIndex.map {
    case (b, i) => {
      val instanceCreator = new SlotRecordEmbroider(i, new ClientCanonicalInstanceCreationSchema({
        () => nextInstanceNr(i)
      }), this)
      logger.debug("bind {} to {}", i, b)
      b -> new ExternalRecorderInstanceProvider(instanceCreator)
    }
  }.toMap

  private var transactionRecordStatus: InstanceRegistryStatus = InstanceRegistryStatus(Range(0, nrOfSlots).map(i => InstanceRegistrySlotStatus()).toVector)

  def nextInstanceNr(slotNr: Int): Int = transactionRecordStatus.slots(slotNr).instances.size

  val recordingExecutionManager = new ExecutionStackManager(transportProvider(externalBind).toVector, this)

  def recordCall(slotNr: Int, call: CanonicalProtocolMethodCall) {
    recordingExecutionManager.pushMethodCall(ProtocolMethodCall(slotNr, call))
    call match {
      case _: ReturnMethodCall => recordingExecutionManager.flushExecution
      case _ =>
    }
  }

  def bind(slotNr: Int, instance: CanonicalProtocolInstance, ref: AnyRef) {
    logger.debug("on slot {} bind instance {} ", slotNr, instance)
    val operation = AddInstance(InstanceExecutionData(instance, ref))
    transactionRecordStatus = transactionRecordStatus.updated(operation)(slotNr)
  }


  def getInstanceReferenceToTransfer(slotNr: Int): Seq[CanonicalProtocolInstance] = {
    val startPosition = transactionRecordStatus.slots(slotNr).transferMark
    val slotInstance = transactionRecordStatus.slots(slotNr).instances.drop(startPosition).map(_.instance)
    val toSend = slotInstance.filter(CanonicalProtocol.isClientInstance _)
    transactionRecordStatus = transactionRecordStatus.updated(UpdateTransferMark(startPosition + toSend.size))(slotNr)
    val toMerge = getInstancetoMerge(slotNr)
    toSend ++ toMerge
  }

  def getInstancetoMerge(slotNr: Int): Seq[CanonicalProtocolInstance] = {
    val startPosition = transactionRecordStatus.slots(slotNr).mergeMark
    val slotInstance = transactionRecordStatus.slots(slotNr).instances.drop(startPosition).map(_.instance)
    val merged = slotInstance.takeWhile(_ match {
      case _: ServerPendingDataInstance => false
      case _ => true
    })
    transactionRecordStatus = transactionRecordStatus.updated(UpdateMergeMark(startPosition + merged.size))(slotNr)
    merged.filter(_ match {
      case _: ServerFilledDataInstance => true
      case _ => false
    }).toSeq
  }


  def mergeInstances(slotNr: Int, instances: Seq[CanonicalProtocolInstance]) {
    logger.debug("merge of instances on record context {}", instances)

    val updateOperations = instances.map {
      _ match {
        case i@ServerBindingKeyInstance(instanceNr, key) => ???
        case i@ServerReturnBindingKeyInstance(instanceNr, key) => ???
        case i@ServerExternalInstanceBinding(instanceNr, key) => ???
        case i@ServerPendingDataInstance(instanceNr, key) => AddInstance(InstanceExecutionData(i, null))
        case i@ServerFilledDataInstance(instanceNr, key, data) => ???
        case i@ServerDataInstance(instanceNr, key, data) => AddInstance(InstanceExecutionData(i, data.asInstanceOf[AnyRef]))
        case i@ClientFilledDataInstance(instanceNr, key, data) => {
          val projection = data.asInstanceOf[AnyRef]
          val delayed = transactionRecordStatus.slots(slotNr).instances(instanceNr).ref.asInstanceOf[PromiseCanonicalProtocolInstanceRef[_]]
          delayed.promise success projection
          UpdateInstance(InstanceExecutionData(i, projection))
        }
        case _ => ???
      }
    }
    //    val updateOperations = instanceRef.map(UpdateInstance(_))
    transactionRecordStatus = transactionRecordStatus.updated(updateOperations)(slotNr)
  }


  def saveReturnValue(slotNr: Int, instanceNr: Int, returnValue: AnyRef) {
    val currentInstance = transactionRecordStatus.slots(slotNr).instances(instanceNr)
    val updated = currentInstance.instance match {
      case ServerPendingDataInstance(nr, key) => currentInstance.copy(instance = ServerFilledDataInstance(nr, key, returnValue.asInstanceOf[java.io.Serializable]))
      case _ => ???
    }
    val operation = UpdateInstance(updated)
    transactionRecordStatus = transactionRecordStatus.updated(operation)(slotNr)

  }

  def getInstanceRef(slotNr: Int, instanceNr: Int): AnyRef = {
    transactionRecordStatus.slots(slotNr).instances(instanceNr).ref
  }


}
