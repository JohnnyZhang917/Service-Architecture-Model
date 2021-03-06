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


import java.lang.reflect.{InvocationHandler, Method}
import java.util.concurrent.atomic.AtomicBoolean
import eu.pmsoft.sam.model._
import scala._
import scala.AnyRef
import eu.pmsoft.sam.model.ExternalServiceBind
import scala.concurrent._
import scala.concurrent.duration._
import scala.annotation.tailrec
import com.google.inject.{Guice, Injector, Key}
import eu.pmsoft.sam.injection.{ExternalInstanceProvider, DependenciesBindingContext, ExternalBindingSwitch}
import scala.collection.mutable
import org.slf4j.LoggerFactory


object CanonicalRecordingLayer {

  def apply(registry: SamArchitectureManagementApi,
            injectionConfiguration: InjectionConfigurationElement,
            transportContext: TransactionTransportContext): InjectionTransactionAccessApi = new CanonicalRecordingLayer(registry, injectionConfiguration, transportContext)

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

class TransactionTransportContext(
                                   val pipeBindMapping: Map[PipeIdentifier, TransportPipe[ThreadMessage]],
                                   val externalBind: Seq[(ExternalServiceBind, TransportPipe[ThreadMessage])],
                                   val loopPipe: LoopTransportPipe[ThreadMessage]
                                   )

private class CanonicalRecordingLayer(registry: SamArchitectureManagementApi, injectionConfiguration: InjectionConfigurationElement, transportContext: TransactionTransportContext) extends InjectionTransactionAccessApi {

  private val recorder = new InjectionTransactionRecordManager(injectionConfiguration, transportContext)
  val logger = LoggerFactory.getLogger(this.getClass)

  private val headInjector = Guice.createInjector(InjectionTransaction.glueInjector(registry, recorder.injectionConfiguration).get)

  private val executor = new TransactionExecutionManager(headInjector, transportContext)

  private val transaction: InjectionTransaction = {
    val rootNode = InjectionTransaction.createNode(new InjectionTransactionWrappingContext(recorder.externalInstanceProviders), recorder.injectionConfiguration)
    new InjectionTransaction(headInjector, rootNode)
  }

  def getTransactionInjector: Injector = transaction.transactionInjector

  def bindTransaction {
    recorder.recordingExecutionManager.bindTransaction
    executor.executionManager.bindTransaction
    transaction.rootNode.bindSwitchingScope
  }

  def unBindTransaction {
    transaction.rootNode.unbindSwitchingScope
    executor.executionManager.unbindTransaction
    recorder.recordingExecutionManager.unbindTransaction
  }

  def getTransactionTransportContext: TransactionTransportContext = transportContext

  private val closed = new AtomicBoolean(false)

  def enterPendingMode() {
    logger.debug("enter pending mode")
    while (!closed.get()) {
      protocolExecution()
    }
    logger.debug("exit pending mode")
  }


  def exitPendingMode() {
    closed.set(true)
    transportContext.loopPipe.getInputPipe.receiveMessage( ThreadMessage(None,None))
  }

  def protocolExecution(): Unit = {
    val inputPipe = transportContext.loopPipe.getInputPipe
    val message = inputPipe.waitResponse
    message.data.map {
      proto => {
        executor.mergeInstances(0, proto.instances)
        executor.executionManager.executeCalls(0, proto.calls)
      }
    }

    val finishData = CanonicalRequest(executor.getInstanceReferenceToTransfer(0), Seq(), true)
    val res = ThreadMessage(Some(finishData))
    logger.debug("response {}", res)
    inputPipe.sendMessage(res)
  }
}


trait TransactionStatusFlow {
  def recordCall(slotNr: Int, call: CanonicalProtocolMethodCall): Unit

  def bind(slotNr: Int, instance: CanonicalProtocolInstance, ref: AnyRef): Unit

}

trait CanonicalInstanceCreationSchema {

  def keyBinding(key: Key[_]): CanonicalProtocolInstance

  def externalBinding(key: Key[_]): CanonicalProtocolInstance

  def dataBinding(key: Key[_], ref: java.io.Serializable): CanonicalProtocolInstance

  def returnBind(key: Key[_]): CanonicalProtocolInstance

  def pendingBind(key: Key[_]): CanonicalProtocolInstance

}

class ClientCanonicalInstanceCreationSchema(val next: () => Int) extends CanonicalInstanceCreationSchema {

  def keyBinding(key: Key[_]): CanonicalProtocolInstance = ClientBindingKeyInstance(next(), key)

  def externalBinding(key: Key[_]): CanonicalProtocolInstance = ClientExternalInstanceBinding(next(), key)

  def dataBinding(key: Key[_], ref: java.io.Serializable): CanonicalProtocolInstance = ClientDataInstance(next(), key, ref)

  def returnBind(key: Key[_]): CanonicalProtocolInstance = ClientReturnBindingKeyInstance(next(), key)

  def pendingBind(key: Key[_]): CanonicalProtocolInstance = ClientPendingDataInstance(next(), key)
}


private class SlotRecordEmbroider(val slotNr: Int,
                                  val creationSchema: CanonicalInstanceCreationSchema,
                                  val transactionFlow: TransactionStatusFlow) extends RecordEmbroider {
  private val logger = LoggerFactory.getLogger(this.getClass)


  @tailrec
  private def extractInternalInstanceNumber(argType: Class[_], argument: AnyRef): Int = {
    argument match {
      case null => ???
      case proxy: java.lang.reflect.Proxy => extractInternalInstanceNumber(argType, java.lang.reflect.Proxy.getInvocationHandler(proxy))
      case proxy: ExternalBindingSwitch[_] => extractInternalInstanceNumber(argType, proxy.getInternalInstance.asInstanceOf[AnyRef])
      case recorder: CanonicalProtocolRecorderRef[_] if recorder.embroider == this => recorder.instance.instanceNr
      case recorder: CanonicalProtocolRecorderRef[_] => {
        val instance = creationSchema.externalBinding(Key.get(argType))
        transactionFlow.bind(slotNr, instance, recorder.getInstance.asInstanceOf[AnyRef])
        instance.instanceNr

      }
      case serializable: java.io.Serializable => {
        val instance = creationSchema.dataBinding(Key.get(argType), serializable)
        transactionFlow.bind(slotNr, instance, serializable)
        instance.instanceNr
      }
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
        val ccall = VoidMethodCall(instance.instanceNr, method.hashCode(), argsNr)
        val ref = VoidCanonicalProtocolInstanceRef
        transactionFlow.recordCall(slotNr, ccall)
        ref
      }
      case ReflectionInterfaceMethodCall(instance, method, args, returnType) => {
        val binding = creationSchema.returnBind(Key.get(returnType))
        val ref = new CanonicalProtocolRecorderRef[AnyRef](binding, this)
        transactionFlow.bind(slotNr, binding, ref.getInstance.asInstanceOf[AnyRef])
        val ccall = InterfaceMethodCall(instance.instanceNr, method.hashCode(), argsNr, binding.instanceNr)
        transactionFlow.recordCall(slotNr, ccall)
        ref
      }
      case ReflectionDataMethodCall(instance, method, args, returnType) => {
        val pending = creationSchema.pendingBind(Key.get(returnType))
        val ref = new PromiseCanonicalProtocolInstanceRef()
        transactionFlow.bind(slotNr, pending, ref)
        val ccall = ReturnMethodCall(instance.instanceNr, method.hashCode(), argsNr, pending.instanceNr)
        transactionFlow.recordCall(slotNr, ccall)
        ref
      }
    }
  }

  def key[T](bindingKey: Key[T]): CanonicalProtocolInstanceRef[T] = {
    logger.trace("apply bind key on slot {} for key {}", slotNr, bindingKey)
    val canonicalInstance = creationSchema.keyBinding(bindingKey)
    val ref = new CanonicalProtocolRecorderRef[T](canonicalInstance, this)
    transactionFlow.bind(slotNr, canonicalInstance, ref.getInstance.asInstanceOf[AnyRef])
    ref
  }
}


private class InjectionTransactionWrappingContext(externalInstanceProviders: Map[ExternalServiceBind, InstanceProvider]) extends InjectionTransactionContext {

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
  def getInstance: AnyRef = throw new IllegalAccessException("You are trying to call a instance reference on void")
}

class PromiseCanonicalProtocolInstanceRef[T] extends CanonicalProtocolInstanceRef[T] {
  val promise: Promise[AnyRef] = Promise()

  def getInstance: T = {
    Await.result(promise.future, 0 nanos).asInstanceOf[T]
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
