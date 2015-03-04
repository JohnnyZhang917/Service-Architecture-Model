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

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.net.InetSocketAddress
import org.slf4j.LoggerFactory
import eu.pmsoft.sam.model._
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap, LinkedBlockingQueue}

import eu.pmsoft.sam.transport._
import eu.pmsoft.sam.idgenerator.LongLongID
import eu.pmsoft.sam.model.CanonicalProtocolMethodCallProto.CallType
import eu.pmsoft.sam.model.CanonicalProtocolInstanceProto.InstanceType
import com.google.inject.Key
import java.io._
import com.google.protobuf.ByteString
import java.lang.annotation.Annotation
import eu.pmsoft.sam.model.ClientFilledDataInstance
import eu.pmsoft.sam.model.ClientDataInstance
import eu.pmsoft.sam.model.InterfaceMethodCall
import scala.Some
import eu.pmsoft.sam.model.ExposedServiceTransaction
import eu.pmsoft.sam.model.ServerReturnBindingKeyInstance
import eu.pmsoft.sam.model.ServiceInstanceURL
import eu.pmsoft.sam.model.ClientPendingDataInstance
import eu.pmsoft.sam.model.ServerExternalInstanceBinding
import eu.pmsoft.sam.model.VoidMethodCall
import eu.pmsoft.sam.model.ServerBindingKeyInstance
import eu.pmsoft.sam.model.ServerDataInstance
import eu.pmsoft.sam.model.ClientExternalInstanceBinding
import eu.pmsoft.sam.model.ServerPendingDataInstance
import eu.pmsoft.sam.model.ServerFilledDataInstance
import eu.pmsoft.sam.model.ReturnMethodCall
import eu.pmsoft.sam.model.ClientBindingKeyInstance
import eu.pmsoft.sam.model.ClientReturnBindingKeyInstance
import java.util.concurrent.atomic.AtomicBoolean

object CanonicalTransportLayer {

  private val EnvProtoCodec = TransportCodecBuilder.clientProtobuff[SamEnvironmentAction, SamEnvironmentResult](SamEnvironmentResult.getDefaultInstance)

  def createClientDispatcher(address: InetSocketAddress): Future[RPCDispatcher[SamEnvironmentAction, SamEnvironmentResult]] = {
    val client = ClientConnector[SamEnvironmentAction, SamEnvironmentResult](EnvProtoCodec)
    client.connect(address) map {
      transport => RPCDispatcher.clientDispatcher[SamEnvironmentAction, SamEnvironmentResult](transport)
    }
  }

}

case class CanonicalProtocolMessage(instances: Seq[CanonicalProtocolInstance], calls: Seq[CanonicalProtocolMethodCall], closeHomotopy: Boolean)

private[see] class CanonicalTransportServer(val logic: EnvironmentExternalApiLogic, val port: Int) extends SamEnvironmentExternalConnector {
  val logger = LoggerFactory.getLogger(this.getClass)
  val serverAddress = new InetSocketAddress(port)

  private val server = ServerConnector[SamEnvironmentAction, SamEnvironmentResult](
    serverAddress,
    TransportCodecBuilder.serverProtobuff[SamEnvironmentAction, SamEnvironmentResult](SamEnvironmentAction.getDefaultInstance),
    onConnection
  )

  val channel = server.bind

  def onConnection(transport: Transport[SamEnvironmentResult, SamEnvironmentAction]) {
    RPCDispatcher.serverDispatcher[SamEnvironmentAction, SamEnvironmentResult](transport, logic.handle _)
  }

  def createUrl(configurationId: ServiceConfigurationID): ServiceInstanceURL = {
    logger.trace("createUrl {}", configurationId)
    ServiceInstanceURL(new java.net.URL("http", serverAddress.getHostName, serverAddress.getPort, s"/service/${configurationId.id}"))
  }

  val dispatchers: ConcurrentMap[InetSocketAddress, Future[RPCDispatcher[SamEnvironmentAction, SamEnvironmentResult]]] = new ConcurrentHashMap()

  private def getClientDispatcher(address: InetSocketAddress) = {
    dispatchers.putIfAbsent(address, CanonicalTransportLayer.createClientDispatcher(address))
    dispatchers.get(address)
  }

  def onEnvironment(address: InetSocketAddress): SamEnvironmentExternalApi = new EnvironmentConnection(logic, getClientDispatcher(address))

  def getConnectionFromServiceURL(surl: ServiceInstanceURL): SamEnvironmentExternalApi = {
    onEnvironment(surl)
  }

  private implicit def addressFromURL(surl: ServiceInstanceURL): InetSocketAddress = new InetSocketAddress(surl.url.getHost, surl.url.getPort)

  def openExecutionPipe(pipeID: PipeIdentifier, remoteEndpointRef: PipeReference): TransportPipe[ThreadMessage] = new RPCBasedTransportPipe(pipeID, remoteEndpointRef, getClientDispatcher(remoteEndpointRef.address))

  def openLoopbackPipe(pipeID: PipeIdentifier, remoteEndpointRef: PipeReference): LoopTransportPipe[ThreadMessage] = new RPCBasedLoopTransportPipe(pipeID, remoteEndpointRef, getClientDispatcher(remoteEndpointRef.address))
}

private class EnvironmentConnection(val logic: EnvironmentExternalApiLogic, val dispatcher: Future[RPCDispatcher[SamEnvironmentAction, SamEnvironmentResult]]) extends SamEnvironmentExternalApi {

  import ProtocolTransformations._

  def getArchitectureSignature(): Future[String] = dispatcher flatMap {
    disp =>
      val action = SamEnvironmentAction.getDefaultInstance.copy(SamEnvironmentCommandType.ARCHITECTURE_INFO)
      disp.dispatch(action)
  } map {
    _.`architectureInfoSignature`.get
  }

  def ping(): Future[Boolean] = dispatcher flatMap {
    disp =>
      val action = SamEnvironmentAction.getDefaultInstance.copy(SamEnvironmentCommandType.PING)
      disp.dispatch(action)
  } map {
    res => true
  }

  def getExposedServices: Future[Seq[ExposedServiceTransaction]] = dispatcher flatMap {
    disp =>
      val action = SamEnvironmentAction.getDefaultInstance.copy(SamEnvironmentCommandType.SERVICE_LIST)
      disp.dispatch(action)
  } map {
    res =>
      res.`serviceList` map {
        ref =>
          ExposedServiceTransaction(logic.createUrl(ref.`url`), logic.findServiceContract(ref.`contract`))
      }
  }

  def registerTransactionRemote(globalTransactionID: GlobalTransactionIdentifier, headConnectionPipe: PipeReference, service: ExposedServiceTransaction): Future[TransactionBindRegistration] = dispatcher flatMap {
    disp =>
      val registerData = SamTransactionRegistration.getDefaultInstance.copy(globalTransactionID.globalID: LongLongIDProto, mapPipeRef(headConnectionPipe), ProtocolTransformations.serviceReference(service))
      val action = SamEnvironmentAction.getDefaultInstance.copy(SamEnvironmentCommandType.REGISTER_TRANSACTION, Some(registerData))
      disp.dispatch(action)
  } map {
    res => {
      val data = res.`registrationConfirmation`.get
      val p = data.`headPipeRef`
      val pipeRef = PipeReference(ThreadExecutionIdentifier(data.`transactionBindId`), PipeIdentifier(p.`pipeRefId`), logic.createUrl(p.`address`))
      TransactionBindRegistration(globalTransactionID, service.url, ThreadExecutionIdentifier(data.`transactionBindId`), pipeRef)
    }
  }


  def unRegisterTransactionRemote(tid: GlobalTransactionIdentifier): Future[Boolean] = dispatcher flatMap {
    disp =>
      val action = SamEnvironmentAction.getDefaultInstance.copy(SamEnvironmentCommandType.UNREGISTER_TRANSACTION, None, Some(tid.globalID))
      disp.dispatch(action)
  } map {
    res => true
  }

}

private abstract class QueuedTransportPipe[T] extends TransportPipe[T] {
  private val queue = new LinkedBlockingQueue[T]()

  def waitResponse: T = queue.take()

  def pollResponse: Option[T] = Option(queue.poll())

  def receiveMessage(message: T) {
    queue.add(message)
  }
}

private class RPCBasedLoopTransportPipe(val pipeID: PipeIdentifier, val remoteEndpointRef: PipeReference, val dispatcher: Future[RPCDispatcher[SamEnvironmentAction, SamEnvironmentResult]]) extends LoopTransportPipe[ThreadMessage] {
  private val inputPipe = new RPCBasedTransportPipe(pipeID, remoteEndpointRef, dispatcher)
  private val loopPipe = new RPCBasedTransportPipe(pipeID, remoteEndpointRef, dispatcher)

  def getInputPipe: TransportPipe[ThreadMessage] = inputPipe

  def getLoopBackPipe: TransportPipe[ThreadMessage] = loopPipe


}

private class RPCBasedTransportPipe(val pipeID: PipeIdentifier, val remoteEndpointRef: PipeReference, val dispatcher: Future[RPCDispatcher[SamEnvironmentAction, SamEnvironmentResult]]) extends QueuedTransportPipe[ThreadMessage] {
  val logger = LoggerFactory.getLogger(this.getClass)
  private val waitingForResponse = new AtomicBoolean(false)


  override def receiveMessage(message: ThreadMessage) {
    waitingForResponse.set(false)
    super.receiveMessage(message)
  }

  def sendMessage(message: ThreadMessage): Unit = {
    waitingForResponse.set(true)
    transport(message)
  }

  import ThreadMessageToProtoMapper._

  def transport(message: ThreadMessage): Future[Unit] = {
    val sended = dispatcher flatMap {
      disp =>
        logger.debug("sending message to direction {}", remoteEndpointRef.address)
        val action = SamEnvironmentAction.getDefaultInstance.copy(SamEnvironmentCommandType.PROTOCOL_EXECUTION_MESSAGE).setMessage(toProto(pipeID, remoteEndpointRef, message))
        disp.dispatch(action)
    } map {
      res => ()
    }
    sended onFailure {
      case t => logger.error("error on send: {}", t)
    }

    sended
  }


  def isWaitingForMessage: Boolean = waitingForResponse.get()

  def getPipeID: PipeIdentifier = pipeID
}

object ThreadMessageToProtoMapper {

  import ProtocolTransformations._

  def toProto(sourcePipeID: PipeIdentifier, remotePipeRef: PipeReference, message: ThreadMessage): CanonicalRequestProto = {
    val data = message.data.get
    val calls = data.calls.map {
      mapCall _
    }
    val instances = data.instances.map {
      mapInstance
    }
    CanonicalRequestProto.getDefaultInstance.copy(sourcePipeID.pid, remotePipeRef, calls.toVector, instances.toVector, data.closeThread)
  }

  implicit def mapCall(call: CanonicalProtocolMethodCall): CanonicalProtocolMethodCallProto = {
    val ctype = call match {
      case VoidMethodCall(instanceNr, methodSignature, arguments) => CallType.VOID_CALL
      case InterfaceMethodCall(instanceNr, methodSignature, arguments, returnNr) => CallType.INTERFACE_CALL
      case ReturnMethodCall(instanceNr, methodSignature, arguments, returnNr) => CallType.RETURN_CALL
    }
    CanonicalProtocolMethodCallProto.getDefaultInstance.copy(call.instanceNr, call.methodSignature, call.returnInstance, call.arguments.toVector, ctype)
  }

  implicit def mapCallProto(callp: CanonicalProtocolMethodCallProto): CanonicalProtocolMethodCall = {
    callp.`callType` match {
      case CallType.VOID_CALL => VoidMethodCall(callp.`instanceNr`, callp.`methodSignature`, callp.`argumentsReference`.toArray)
      case CallType.INTERFACE_CALL => InterfaceMethodCall(callp.`instanceNr`, callp.`methodSignature`, callp.`argumentsReference`.toArray, callp.`returnInstanceId`.get)
      case CallType.RETURN_CALL => ReturnMethodCall(callp.`instanceNr`, callp.`methodSignature`, callp.`argumentsReference`.toArray, callp.`returnInstanceId`.get)
      case _ => ??? // this should not happen
    }
  }

  implicit def mapInstance(ins: CanonicalProtocolInstance): CanonicalProtocolInstanceProto = {
    val data = ins match {
      case ClientBindingKeyInstance(instanceNr, key) => (ins, InstanceType.CLIENT_BINDING_KEY, None)
      case ClientReturnBindingKeyInstance(instanceNr, key) => (ins, InstanceType.CLIENT_RETURN_BINDING_KEY, None)
      case ClientExternalInstanceBinding(instanceNr, key) => (ins, InstanceType.CLIENT_EXTERNAL_INSTANCE_BINDING, None)
      case ClientPendingDataInstance(instanceNr, key) => (ins, InstanceType.CLIENT_PENDING_DATA_INSTANCE, None)
      case ClientFilledDataInstance(instanceNr, key, data) => (ins, InstanceType.CLIENT_FILLED_DATA_INSTANCE, Some(data))
      case ClientDataInstance(instanceNr, key, data) => (ins, InstanceType.CLIENT_DATA_INSTANCE, Some(data))
      case ServerBindingKeyInstance(instanceNr, key) => (ins, InstanceType.SERVER_BINDING_KEY, None)
      case ServerReturnBindingKeyInstance(instanceNr, key) => (ins, InstanceType.SERVER_RETURN_BINDING_KEY, None)
      case ServerExternalInstanceBinding(instanceNr, key) => (ins, InstanceType.SERVER_EXTERNAL_INSTANCE_BINDING, None)
      case ServerPendingDataInstance(instanceNr, key) => (ins, InstanceType.SERVER_PENDING_DATA_INSTANCE, None)
      case ServerFilledDataInstance(instanceNr, key, data) => (ins, InstanceType.SERVER_FILLED_DATA_INSTANCE, Some(data))
      case ServerDataInstance(instanceNr, key, data) => (ins, InstanceType.SERVER_DATA_INSTANCE, Some(data))
    }
    CanonicalProtocolInstanceProto.getDefaultInstance.copy(ins.instanceNr, data._2, ins.key, data._3 map dataMapper _)

  }

  implicit def mapInstanceProto(insp: CanonicalProtocolInstanceProto): CanonicalProtocolInstance = {
    insp.`instanceType` match {
      case InstanceType.CLIENT_BINDING_KEY => ClientBindingKeyInstance(insp.`instanceNr`, insp.`key`)
      case InstanceType.CLIENT_RETURN_BINDING_KEY => ClientReturnBindingKeyInstance(insp.`instanceNr`, insp.`key`)
      case InstanceType.CLIENT_EXTERNAL_INSTANCE_BINDING => ClientExternalInstanceBinding(insp.`instanceNr`, insp.`key`)
      case InstanceType.CLIENT_PENDING_DATA_INSTANCE => ClientPendingDataInstance(insp.`instanceNr`, insp.`key`)
      case InstanceType.CLIENT_FILLED_DATA_INSTANCE => ClientFilledDataInstance(insp.`instanceNr`, insp.`key`, insp.`dataObjectSerialization`.get)
      case InstanceType.CLIENT_DATA_INSTANCE => ClientDataInstance(insp.`instanceNr`, insp.`key`, insp.`dataObjectSerialization`.get)
      case InstanceType.SERVER_BINDING_KEY => ServerBindingKeyInstance(insp.`instanceNr`, insp.`key`)
      case InstanceType.SERVER_RETURN_BINDING_KEY => ServerReturnBindingKeyInstance(insp.`instanceNr`, insp.`key`)
      case InstanceType.SERVER_EXTERNAL_INSTANCE_BINDING => ServerExternalInstanceBinding(insp.`instanceNr`, insp.`key`)
      case InstanceType.SERVER_PENDING_DATA_INSTANCE => ServerPendingDataInstance(insp.`instanceNr`, insp.`key`)
      case InstanceType.SERVER_FILLED_DATA_INSTANCE => ServerFilledDataInstance(insp.`instanceNr`, insp.`key`, insp.`dataObjectSerialization`.get)
      case InstanceType.SERVER_DATA_INSTANCE => ServerDataInstance(insp.`instanceNr`, insp.`key`, insp.`dataObjectSerialization`.get)
      case _ => ??? // This should not happen
    }
  }

  implicit def keyMapper(key: Key[_]): BindingKey = {
    BindingKey(
      key.getTypeLiteral.toString,
      Option(key.getAnnotationType()).map(_.toString),
      Option(key.getAnnotation).map(ano => dataMapper(ano.asInstanceOf[java.io.Serializable]))
    )
  }

  implicit def keyMapperProto(keyp: BindingKey): Key[_] = {
    val ctype = Class.forName(keyp.`type`)
    val annotationType = keyp.`annotationType` map {
      Class.forName(_).asSubclass(classOf[Annotation])
    }
    val annotation = keyp.`annotationInstance` map {
      data => dataMapperProto(data).asInstanceOf[Annotation]
    }
    annotation match {
      case Some(a) => Key.get(ctype, a)
      case None => annotationType match {
        case Some(at) => Key.get(ctype, at)
        case None => Key.get(ctype)
      }
    }
  }

  implicit def dataMapperProto(data: com.google.protobuf.ByteString): java.io.Serializable = {
    val s: InputStream = new ByteArrayInputStream(data.toByteArray)
    val instr = new ObjectInputStream(s)
    try {
      val res = instr.readObject()
      res.asInstanceOf[java.io.Serializable]
    } finally {
      instr.close()
    }
  }

  implicit def dataMapper(serializable: java.io.Serializable): com.google.protobuf.ByteString = {
    val bos = new ByteArrayOutputStream()
    val out = new ObjectOutputStream(bos)
    try {
      out.writeObject(serializable)
      ByteString.copyFrom(bos.toByteArray)
    } catch {
      case e: Throwable => ???
    } finally {
      out.close()
      bos.close()
    }
  }

  implicit def fromProto(proto: CanonicalRequestProto, urlMap: String => ServiceInstanceURL): MessageRoutingInformation = {
    val cs = proto.`calls` map {
      mapCallProto _
    }
    val ins = proto.`instances` map {
      mapInstanceProto _
    }
    val data = Some(CanonicalRequest(ins, cs, proto.`closeThread`))
    val p = proto.`remotePipeRef`
    val pipeRef = PipeReference(ThreadExecutionIdentifier(p.`transactionBindId`), PipeIdentifier(p.`pipeRefId`), urlMap(p.`address`))
    MessageRoutingInformation(PipeIdentifier(proto.`sourcePipeId`), pipeRef, ThreadMessage(data, None))
  }
}

// TODO make this class private
object ProtocolTransformations {

  implicit def mapPipeRef(p: PipeReference): PipeReferenceProto = PipeReferenceProto.getDefaultInstance.copy(p.transactionBindID.tid, p.pipeID.pid, p.address.url.toExternalForm)

  implicit def mapExposedTo(ref: ExposedServiceTransaction): SamExposedServiceReference = SamExposedServiceReference(ref.url.url.toExternalForm, ref.contract.signature.getCanonicalName)

  implicit def longLongIdProto(llid: LongLongID): LongLongIDProto = LongLongIDProto.getDefaultInstance.copy(llid.getMark, llid.getLinear)

  implicit def longLongId(pllid: LongLongIDProto): LongLongID = new LongLongID(pllid.`mask`, pllid.`linear`)

  // FIXME security, contract mapping is using strig<->class, change to generated ids from architecture
  implicit def serviceReference(ref: ExposedServiceTransaction): SamExposedServiceReference = SamExposedServiceReference.getDefaultInstance.copy(ref.url.url.toExternalForm, ref.contract.signature.getCanonicalName)
}