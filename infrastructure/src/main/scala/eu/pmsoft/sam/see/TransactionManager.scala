package eu.pmsoft.sam.see


import scala.concurrent._
import ExecutionContext.Implicits.global

import java.util.concurrent.atomic.AtomicReference
import java.net.{URL, InetSocketAddress}
import scala.concurrent.{Promise, Future}
import scala.util.{Failure, Success}
import eu.pmsoft.sam.model._


private[see] case class TransactionCreationData(
                                                 tid: LiftedServiceConfiguration,
                                                 config: InjectionConfiguration)

object TransactionManager {

  private sealed trait TransactionStatus

  // after creation
  private case class Init() extends TransactionStatus
  // transaction information send to external environments
  private case class Starting() extends TransactionStatus
  // processID created and registered in all environments
  private case class Bind() extends TransactionStatus
  // in transaction execution
  private case class Running() extends TransactionStatus
  // execution done
  private case class UnBind() extends TransactionStatus
  // transaction termination announcing
  private case class Stopping() extends TransactionStatus
  // processID registration deleted
  private case class End() extends TransactionStatus

  private case class Error(t:Throwable) extends TransactionStatus
}

class TransactionManager(env: ServiceExecutionEnvironment, data: TransactionCreationData) {

  import TransactionManager._
  private[this] val state = new AtomicReference[TransactionStatus](Init())
  private[this] val workDonePromise = Promise[Unit]()

  def loopTransactionExecution() : Unit = {
    state.get() match {
      case s@Init() => if( state.compareAndSet(s,Starting())) innerLoop()
      case _ => ()
    }
    workDonePromise.future
  }

  private def innerLoop() : Unit = {
    val work : Option[Future[Unit]] = state.get() match {
      case Init() => throw new IllegalStateException("how is this possible???")
      case Starting() => Some(startTransaction())
      case End() => { workDonePromise.success(()); None }
      case Error(t) => { workDonePromise failure t; None }
      case _ => ???
    }
    work match {
      case Some(f) => f.onComplete {
        case Success(u) => innerLoop()
        case Failure(t) => workDonePromise failure t
      }
      case None => ()
    }
  }

  private def startTransaction() : Future[Unit] = Future {
    def getEnvironmentAddress(serviceUrl: URL) : InetSocketAddress = ???
    val externalBind = InjectionTransaction.getExternalBind(data.config.configurationRoot)
    val externalEnvAddress = externalBind map { e => getEnvironmentAddress( e.url.url ) }
    val externalEnvConnections = externalEnvAddress map { env.externalConnector.onEnvironment _ }
//    externalEnvConnections.foreach( _.)
    ???
  }


}
