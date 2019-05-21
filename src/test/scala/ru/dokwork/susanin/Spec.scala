package ru.dokwork.susanin
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import ru.dokwork.susanin.concurrent.{ TraceableExecutionContext, TraceableScheduledExecutorService }
import ru.dokwork.susanin.future.FutureTracer
import io.opentracing.mock.{ MockSpan, MockTracer }
import org.scalatest.{ Assertion, AsyncFreeSpec, LoneElement, Matchers }

import scala.collection.JavaConverters._
import scala.concurrent.{ ExecutionContext, Future }

trait Spec extends AsyncFreeSpec with Matchers with LoneElement  {
  implicit val scheduler: TraceableScheduledExecutorService =
    TraceableScheduledExecutorService.wrap(
      Executors.newScheduledThreadPool(2)
    )

  override implicit val executionContext: TraceableExecutionContext =
    TraceableExecutionContext.wrap(TestExecutionContext())

  implicit class FutureSugar[A](val f: Future[A]) {
    def >>[B](other: ⇒ Future[B]): Future[B] =
      f.flatMap(_ ⇒ other)
  }

  case class Fixture(tracer: FutureTracer, finishedSpans: () ⇒ List[MockSpan]) {
    private def spans = finishedSpans().map(_.operationName()).zip(finishedSpans()).toMap
    def span(name: String): MockSpan =
      spans.getOrElse(name, throw new Exception(s"Span '$name' was not finished"))
  }

  def test(test: Fixture ⇒ Future[Assertion]): Future[Assertion] = {
    val mockTracer = new MockTracer()
    val fixture = Fixture(
      FutureTracer.fromOpenTracing(mockTracer),
      () ⇒ mockTracer.finishedSpans().asScala.toList
    )
    test(fixture)
  }

  /** Just returns `Future.unit` */
  def unit(ignore: Span): Future[Unit] = Future.unit
}

object TestExecutionContext {

  // We should run every task on new thread with empty spans stack to have clean and relevant tests.
  def apply(): ExecutionContext = new ExecutionContext {
    private val id = new AtomicInteger(0)
    override def execute(runnable: Runnable): Unit = {
      val t = new Thread(runnable)
      t.setName(s"test-execution-context-thread-${id.getAndIncrement()}")
      t.setDaemon(true)
      t.start()
    }
    override def reportFailure(cause: Throwable): Unit = throw cause
  }
}