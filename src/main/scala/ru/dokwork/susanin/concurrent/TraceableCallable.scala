package ru.dokwork.susanin.concurrent

import java.util.concurrent.Callable

import ru.dokwork.susanin.Span

class TraceableCallable[A](
  stack: ThreadLocalStack[Span],
  task: Callable[A]
) extends Callable[A] {
  val spans: List[Span] = stack.get
  override def call(): A = stack.withValues(spans)(task.call())
}
