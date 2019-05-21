package ru.dokwork.susanin.concurrent

import java.util
import java.util.concurrent._

import ru.dokwork.susanin.Span

class TraceableExecutorService private[concurrent](
  wrapped: ExecutorService,
  stack: ThreadLocalStack[Span]
) extends ExecutorService {
  override def shutdown(): Unit =
    wrapped.shutdown()

  override def shutdownNow(): util.List[Runnable] =
    wrapped.shutdownNow()

  override def isShutdown: Boolean =
    wrapped.isShutdown

  override def isTerminated: Boolean =
    wrapped.isTerminated

  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean =
    wrapped.awaitTermination(timeout, unit)

  override def submit[T](task: Callable[T]): Future[T] =
    wrapped.submit(new TraceableCallable[T](stack, task))

  override def submit[T](task: Runnable, result: T): Future[T] =
    wrapped.submit(new TraceableRunnable(stack, task), result)

  override def submit(task: Runnable): Future[_] =
    wrapped.submit(new TraceableRunnable(stack, task))

  override def invokeAll[T](tasks: util.Collection[_ <: Callable[T]]): util.List[Future[T]] =
    wrapped.invokeAll(tasks)

  override def invokeAll[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): util.List[Future[T]] =
    wrapped.invokeAll(tasks, timeout, unit)

  override def invokeAny[T](tasks: util.Collection[_ <: Callable[T]]): T =
    wrapped.invokeAny(tasks)

  override def invokeAny[T](tasks: util.Collection[_ <: Callable[T]], timeout: Long, unit: TimeUnit): T =
    wrapped.invokeAny(tasks, timeout, unit)

  override def execute(command: Runnable): Unit =
    wrapped.execute(new TraceableRunnable(stack, command))
}

object TraceableExecutorService {
  def wrap(wrapped: ExecutorService): TraceableExecutorService =
    new TraceableExecutorService(wrapped, ThreadLocalStack.globalSpansStack)
}
