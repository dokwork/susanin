package ru.dokwork.susanin.concurrent

import java.util.concurrent.{ Callable, ScheduledExecutorService, ScheduledFuture, TimeUnit }

import ru.dokwork.susanin._

class TraceableScheduledExecutorService private[concurrent](
  wrapped: ScheduledExecutorService,
  stack: ThreadLocalStack[Span]
) extends TraceableExecutorService(wrapped, stack) with ScheduledExecutorService {

  override def schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture[_] =
    wrapped.schedule(new TraceableRunnable(stack, command), delay, unit)

  override def schedule[V](callable: Callable[V], delay: Long, unit: TimeUnit): ScheduledFuture[V] =
    wrapped.schedule(new TraceableCallable[V](stack, callable), delay, unit)

  override def scheduleAtFixedRate(command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): ScheduledFuture[_] =
    wrapped.scheduleAtFixedRate(new TraceableRunnable(stack, command), initialDelay, period, unit)

  override def scheduleWithFixedDelay(command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit): ScheduledFuture[_] =
    wrapped.scheduleWithFixedDelay(new TraceableRunnable(stack, command), initialDelay, delay, unit)
}

object TraceableScheduledExecutorService {
  def wrap(wrapped: ScheduledExecutorService): TraceableScheduledExecutorService =
    new TraceableScheduledExecutorService(wrapped, ThreadLocalStack.globalSpansStack)
}
