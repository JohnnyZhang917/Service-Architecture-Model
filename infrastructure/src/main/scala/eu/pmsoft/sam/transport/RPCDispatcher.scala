package eu.pmsoft.sam.transport

import scala.concurrent._
import ExecutionContext.Implicits.global

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import scala.util.{Failure, Success}
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, Channel}
import java.util.concurrent.ConcurrentLinkedQueue


object RPCDispatcher {

  def apply[Action, Result](transport: Transport[Action, Result] ): RPCDispatcher[Action, Result] =
    new SerialClientDispatcher(transport)

}

trait RPCDispatcher[Action,Result]{

  def dispatch(action: Action): Future[Result]

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

object SerialServerDispatcher {
  private val EoF = Future.failed(new Exception("EoF"))
  private val Idle = Promise[Unit].future
  private val Draining = Promise[Unit].future
  private val Closed = Promise[Unit].future
}

class SerialServerDispatcher[Req, Res](transport: Transport[Res, Req], handler: Req => Future[Res]) {

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


class ChannelTransport[In, Out](ch: Channel) extends ChannelInboundHandlerAdapter with Transport[In, Out] {

  import NettyFutureTranslation._

  private[this] val readq = new AsyncQueue[Out]
  private[this] val writeq = new ConcurrentLinkedQueue[(In, Promise[Unit])]()

  def write(msg: In): Future[Unit] = {
    val p = Promise[Unit]()
    writeq.offer((msg, p))
    rollWrite()
    p.future
  }

  private[this] val counter = new AtomicInteger(0)

  def rollWrite() {
    if (counter.getAndIncrement() == 0) {
      do {
        val toSend = writeq.poll()
        // TODO closed
        val fw: Future[Unit] = ch.writeAndFlush(toSend._1)
        fw.onComplete {
          case Success(()) => toSend._2.success(())
          case Failure(t) => toSend._2.failure(t)
        }
      } while (counter.decrementAndGet() > 0)
    }
  }

  def read(): Future[Out] = readq.poll()

  def isOpen: Boolean = ch.isOpen

  private[this] val closep = Promise[Unit]

  def onClose: Future[Unit] = closep.future

  override def channelRead(ctx: ChannelHandlerContext, msg: Any) {
    val response = msg.asInstanceOf[Out]
    readq.offer(response)
    // check resource clean
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    readq.fail(cause)
    ctx.close()
    //TODO closable and close
  }
}