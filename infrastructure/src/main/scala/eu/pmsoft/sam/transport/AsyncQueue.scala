package eu.pmsoft.sam.transport

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.concurrent.{Future, Promise}
import java.util.concurrent.RejectedExecutionException
import java.util.ArrayDeque

object AsyncQueue {

  private sealed trait State[+T]

  private case object Idle extends State[Nothing]

  private case class Offering[T](q: Queue[T]) extends State[T]

  private case class Polling[T](q: Queue[Promise[T]]) extends State[T]

  private case class Excepting(exc: Throwable) extends State[Nothing]

}

/**
 * An asynchronous FIFO queue. In addition to providing {{offer()}}
 * and {{poll()}}, the queue can be "failed", flushing current
 * pollers.
 *
 * Implementation taken from twitter.utils
 */
class AsyncQueue[T] {

  import AsyncQueue._

  private[this] val state = new AtomicReference[State[T]](Idle)

  def size: Int = state.get match {
    case Offering(q) => q.size
    case _ => 0
  }

  /**
   * Retrieves and removes the head of the queue, completing the
   * returned future when the element is available.
   */
  @tailrec
  final def poll(): Future[T] = state.get match {
    case s@Idle =>
      val p = Promise[T]
      if (state.compareAndSet(s, Polling(Queue(p)))) p.future else poll()

    case s@Polling(q) =>
      val p = Promise[T]
      if (state.compareAndSet(s, Polling(q.enqueue(p)))) p.future else poll()

    case s@Offering(q) =>
      val (elem, nextq) = q.dequeue
      val nextState = if (nextq.nonEmpty) Offering(nextq) else Idle
      if (state.compareAndSet(s, nextState)) Future.successful(elem) else poll()

    case Excepting(exc) =>
      Future.failed(exc)
  }

  /**
   * Insert the given element at the tail of the queue.
   */
  @tailrec
  final def offer(elem: T): Unit = state.get match {
    case s@Idle =>
      if (!state.compareAndSet(s, Offering(Queue(elem))))
        offer(elem)

    case s@Offering(q) =>
      if (!state.compareAndSet(s, Offering(q.enqueue(elem))))
        offer(elem)

    case s@Polling(q) =>
      val (waiter, nextq) = q.dequeue
      val nextState = if (nextq.nonEmpty) Polling(nextq) else Idle
      if (state.compareAndSet(s, nextState))
        waiter.success(elem)
      else
        offer(elem)

    case Excepting(_) =>
    // Drop.
  }

  /**
   * Fail the queue: current and subsequent pollers will be completed
   * with the given exception.
   */
  @tailrec
  final def fail(exc: Throwable): Unit = state.get match {
    case s@Idle =>
      if (!state.compareAndSet(s, Excepting(exc)))
        fail(exc)

    case s@Polling(q) =>
      if (!state.compareAndSet(s, Excepting(exc))) fail(exc)
      else
        q foreach (_.failure(exc))

    case s@Offering(_) =>
      if (!state.compareAndSet(s, Excepting(exc))) fail(exc)

    case Excepting(_) => // Just take the first one.
  }

  override def toString = "AsyncQueue<%s>".format(state.get)
}

trait Permit {
  def release()
}

/**
 * An AsyncSemaphore is a traditional semaphore but with asynchronous
 * execution. Grabbing a permit returns a Future[Permit]
 */
class AsyncSemaphore protected (initialPermits: Int, maxWaiters: Option[Int]) {
  import AsyncSemaphore._

  def this(initialPermits: Int = 0) = this(initialPermits, None)
  def this(initialPermits: Int, maxWaiters: Int) = this(initialPermits, Some(maxWaiters))
  require(maxWaiters.getOrElse(0) >= 0)
  private[this] val waitq = new ArrayDeque[Promise[Permit]]
  private[this] var availablePermits = initialPermits

  private[this] class SemaphorePermit extends Permit {
    /**
     * Indicate that you are done with your Permit.
     */
    override def release() {
      val run : Promise[Permit]= AsyncSemaphore.this.synchronized {
        val next = waitq.pollFirst()
        if (next == null) availablePermits += 1
        next
      }

      if (run != null) run.success(new SemaphorePermit)
    }
  }

  def numWaiters: Int = synchronized(waitq.size)
  def numPermitsAvailable: Int = synchronized(availablePermits)

  /**
   * Acquire a Permit, asynchronously. Be sure to permit.release() in a 'finally'
   * block of your onSuccess() callback.
   *
   * @return a Future[Permit] when the Future is satisfied, computation can proceed,
   * or a Future.Exception[RejectedExecutionException] if the configured maximum number of waitq
   * would be exceeded.
   */
  def acquire(): Future[Permit] = {
    synchronized {
      if (availablePermits > 0) {
        availablePermits -= 1
        Future.successful(new SemaphorePermit)
      } else {
        maxWaiters match {
          case Some(max) if (waitq.size >= max) =>
            MaxWaitersExceededException
          case _ =>
            val promise = Promise[Permit]
            waitq.addLast(promise)
            promise.future
        }
      }
    }
  }

}

object AsyncSemaphore {
  private val MaxWaitersExceededException =
    Future.failed(new RejectedExecutionException("Max waiters exceeded"))
}
