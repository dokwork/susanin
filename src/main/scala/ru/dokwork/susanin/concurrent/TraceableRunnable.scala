package ru.dokwork.susanin.concurrent

import ru.dokwork.susanin.Span

private[susanin] class TraceableRunnable(
    stack: ThreadLocalStack[Span],
    task: Runnable
) extends Runnable {
  val spans: List[Span]   = stack.get
  override def run(): Unit = {
    stack.withValues(spans)(task.run())
  }
}
