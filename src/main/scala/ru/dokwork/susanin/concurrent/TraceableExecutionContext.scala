package ru.dokwork.susanin.concurrent

import ru.dokwork.susanin.Span

import scala.concurrent.ExecutionContext

/**
  * Traceable execution context which can transmits `stack` of the spans between threads
  * in the wrapped `ec`.
  *
  * '''You should wrap all execution context in your project to use tracing!'''
  *
  * @param ec wrapped execution context which will be used to invoke routines.
  * @param stack thread-local stack with spans.
  */
class TraceableExecutionContext(
    ec: ExecutionContext,
    private val stack: ThreadLocalStack[Span]
) extends ExecutionContext {

  override def execute(runnable: Runnable): Unit = {
    ec.execute(new TraceableRunnable(stack, runnable))
  }

  override def reportFailure(cause: Throwable): Unit =
    ec.reportFailure(cause)
}

object TraceableExecutionContext {

  /**
    * Wraps [[ExecutionContext.global]] context with [[ThreadLocalStack.globalSpansStack]].
    */
  implicit lazy val global: TraceableExecutionContext =
    wrap(ExecutionContext.global)

  /**
    * Builds new [[TraceableExecutionContext]] wrapping `ec` with [[ThreadLocalStack.globalSpansStack]].
    */
  def wrap(ec: ExecutionContext): TraceableExecutionContext =
    new TraceableExecutionContext(ec, ThreadLocalStack.globalSpansStack)
}
