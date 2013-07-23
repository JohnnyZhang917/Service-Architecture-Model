package com.twitter.concurrent

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.concurrent.{Future, Promise}

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