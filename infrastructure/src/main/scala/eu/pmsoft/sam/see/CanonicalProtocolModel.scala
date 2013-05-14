package eu.pmsoft.sam.see

import com.google.inject._
import eu.pmsoft.sam.injection.ExternalInstanceProvider
import scala.collection.mutable
import scala.Some
import eu.pmsoft.sam.model.ExternalServiceBind
import java.lang.reflect.{Method, InvocationHandler}

object CanonicalProtocolModel {

}

object TransactionRecordContext {
  def apply(externalBindSet: Set[ExternalServiceBind]) = new TransactionRecordContext(externalBindSet)
}

class TransactionRecordContext(val instanceRegistries: Set[ExternalServiceBind] = Set.empty) {

  lazy val recordMap = instanceRegistries.map( b => b -> new CanonicalProtocolServiceRecordContext(this)).toMap

  lazy val bindMap = instanceRegistries.map(b => b -> new ClientRecordingInstanceRegistry(b, getRecordContext(b))).toMap

  def get(bind: ExternalServiceBind): ClientRecordingInstanceRegistry = bindMap.get(bind).get

  private def getRecordContext(bind: ExternalServiceBind): CanonicalProtocolServiceRecordContext = recordMap.get(bind).get

  def forceContextExecution(slotContext : CanonicalProtocolServiceRecordContext) = ???

}

