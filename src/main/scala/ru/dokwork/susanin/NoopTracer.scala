package ru.dokwork.susanin

import java.util.Collections
import java.{ lang, util }

import io.opentracing.SpanContext
import io.opentracing.propagation.Format

import scala.language.higherKinds
import scala.util.{ Success, Try }

final class NoopTracer[F[_]] extends Tracer[F] {
  override val activeSpan: Option[Span] = Some(Span(None))

  override def newSpan[A](spanName: String, buildSpan: SpanBuilder ⇒ Span)(
      f: Span ⇒ F[A]
  ): F[A] = f(activeSpan.get)

  override def transit[A](f: ⇒ F[A]): F[A] = f

  override def inject[C](spanContext: SpanContext, format: Format[C], carrier: C): Try[Unit] =
    Success({})

  override def extract[C](format: Format[C], carrier: C): Try[SpanContext] = Success {
    new SpanContext {
      override def toTraceId: String = ""
      override def toSpanId: String  = ""
      override def baggageItems(): lang.Iterable[util.Map.Entry[String, String]] =
        Collections.emptyList[util.Map.Entry[String, String]]
    }
  }
}
