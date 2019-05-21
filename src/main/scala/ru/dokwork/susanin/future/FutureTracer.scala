package ru.dokwork.susanin.future

import ru.dokwork.susanin.{ SpanBuilder, _ }
import ru.dokwork.susanin.concurrent.{ ThreadLocalStack, TraceableExecutionContext }
import com.typesafe.scalalogging.StrictLogging
import io.opentracing.propagation.Format
import io.opentracing.util.GlobalTracer
import io.{ opentracing ⇒ ot }

import scala.concurrent.Future
import scala.util.Try

/**
  * Implementation of the [[Tracer]] with [[Future]] as effect.
  *
  * @param wrapped tracer which used to prepare span and extract/inject [[io.opentracing.SpanContext]].
  * @param stack thread-local stack with current spans.
  * @param tec traceable execution context which will be used to finish span.
  */
final class FutureTracer(
    val wrapped: ot.Tracer,
    private val stack: ThreadLocalStack[Span]
)(implicit tec: TraceableExecutionContext)
    extends Tracer[Future]
    with StrictLogging {

  override def inject[C](spanContext: ot.SpanContext, format: Format[C], carrier: C): Try[Unit] =
    Try(wrapped.inject(spanContext, format, carrier))

  override def extract[C](format: Format[C], carrier: C): Try[ot.SpanContext] =
    Try(wrapped.extract(format, carrier))

  /**
    * Looks for the first unfinished span on stack and removes from stack every finished spans.
    *
    * @return some first unfinished span from the top of the stack, or [[None]] if stack is empty
    *         or doesn't contains any unfinished spans.
    */
  override def activeSpan: Option[Span] = {
    val span = stack.peek()
    if (span.exists(_.isFinished)) {
      stack.pop()
      activeSpan
    } else
      span
  }

  /**
    * Run `f` in the context of the new span. Invokes `buildSpan` to build new span and applies it
    * to the `f`. If the span was not finished by `f`, result of the `f` will be
    * [[scala.concurrent.Future#transform(scala.Function1, scala.concurrent.ExecutionContext) transformed]]
    * without any changes just for finish span.
    *
    * @param spanName name of the new span.
    * @param buildSpan function to prepare new span.
    * @param f function wrapped in new span.
    * @tparam A type of the result of the `f`.
    * @return result of the `f`.
    */
  override def newSpan[A](
      spanName: String,
      buildSpan: SpanBuilder ⇒ Span = _.start()
  )(f: Span ⇒ Future[A]): Future[A] = {
    val span = buildSpan(SpanBuilder(wrapped.buildSpan(spanName), activeSpan))
    stack.push(span)
    logger.debug(s"New span $span")
    f(span).transform(result ⇒ {
      if (!span.isFinished) {
        logger.debug(s"Finish span $span")
        span.finish()
      }
      result
    })
  }

  /**
    * Saves spans graph before invoke `f` and recoveries it after result of the `f` will be completed.
    * Any spans inside `f` will be ignored.
    *
    * This function useful to wrap computation with not traceable execution context.
    *
    * @example {{{
    *       implicit val tec = TraceableExecutionContext.global
    *       val p = Promise[Unit]()
    *       for {
    *           _ ← tracer.newSpan("outer") { _ ⇒
    *             for {
    *               _ ← tracer.newSpan("first")(_ => Future.unit)
    *               _ ← tracer.transit(p.future) // fix graph
    *               _ ← tracer.newSpan("second")(_ => Future.unit)
    *             } yield ()
    *           }
    *        } yield {
    *           span("first").parentId() shouldBe span("outer").context().spanId()
    *           span("second").parentId() shouldBe span("outer").context().spanId()
    *        }
    *       // resolving promise on not traceable thread breaks the graph:
    *       ExecutionContext.global.execute(() ⇒ p.success({}))
    * }}}
    *
    * @param f function which run on not traceable execution context.
    * @tparam A type of the result of the `f`.
    * @return result of the `f`.
    */
  def transit[A](f: ⇒ Future[A]): Future[A] = {
    val stack = ThreadLocalStack.globalSpansStack.get
    f.transform(tr ⇒ { ThreadLocalStack.globalSpansStack.set(stack); tr })
  }
}

object FutureTracer {

  /**
    * @return new [[FutureTracer]] around `tracer`.
    */
  def fromOpenTracing(tracer: ot.Tracer)(implicit tec: TraceableExecutionContext): FutureTracer =
    new FutureTracer(tracer, ThreadLocalStack.globalSpansStack)

  /**
    * [[FutureTracer]] with [[io.opentracing.util.GlobalTracer]] inside.
    */
  lazy val global: FutureTracer = fromOpenTracing(GlobalTracer.get())

  lazy val noop: NoopTracer[Future] = new NoopTracer[Future]
}
