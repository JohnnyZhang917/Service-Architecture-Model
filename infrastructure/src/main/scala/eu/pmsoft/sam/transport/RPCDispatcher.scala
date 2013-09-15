package eu.pmsoft.sam.transport

import scala.concurrent._
import ExecutionContext.Implicits.global

import java.util.concurrent.atomic.AtomicReference
import scala.util.{Failure, Success}


object RPCDispatcher {

  def clientDispatcher[Action, Result](transportClient: Transport[Action, Result]): RPCDispatcher[Action, Result] =
    new SerialClientDispatcher(transportClient)

  def serverDispatcher[Action, Result](transportServer: Transport[Result, Action], handler: Action => Future[Result]): RPCServerHandler[Action, Result] = new SerialServerDispatcher[Action, Result](transportServer, handler)
}

trait RPCDispatcher[Action, Result] {

  def dispatch(action: Action): Future[Result]

}

trait RPCServerHandler[Action, Result] {

}

private class SerialClientDispatcher[Action, Result](transport: Transport[Action, Result]) extends RPCDispatcher[Action, Result] {

  private[this] val semaphor = new AsyncSemaphore(1)

  private def syncDispatch(action: Action, promise: Promise[Result]) {
    transport.write(action)
    val rf = transport.read()
    rf map {
      res => promise success res
    } recover {
      case t => promise failure t
    }
  }

  def dispatch(action: Action): Future[Result] = {
    val p = Promise[Result]
    semaphor.acquire().onComplete {
      case Success(permit) => try {
        syncDispatch(action, p)
      } finally {
        permit.release()
      }
      case Failure(e) => p failure e
    }
    p.future
  }
}

private object SerialServerDispatcher {
  private val EoF = Future.failed(new Exception("EoF"))
  private val Idle = Promise[Unit].future
  private val Draining = Promise[Unit].future
  private val Closed = Promise[Unit].future
}

private class SerialServerDispatcher[Req, Res](transport: Transport[Res, Req], handler: Req => Future[Res]) extends RPCServerHandler[Req, Res] {

  import SerialServerDispatcher._

  private[this] val state = new AtomicReference[Future[_]](Idle)

  private[this] def loop(): Unit = {
    state.set(Idle)
    transport.read() flatMap {
      req =>
        val res = handler(req)
        if (state.compareAndSet(Idle, res)) res
        else {
          EoF
        }
    } flatMap {
      res =>
        transport.write(res)
    } map {
      done =>
        loop()
    }
  }

  loop()

}


