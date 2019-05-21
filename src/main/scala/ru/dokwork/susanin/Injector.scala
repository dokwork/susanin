package ru.dokwork.susanin

import io.{ opentracing ⇒ ot }
import scala.util.Try

trait Injector {

  /**
    * Inject a SpanContext into a `carrier` of a given type.
    *
    * @example
    * {{{
    *   val adapter: io.opentracing.propagation.TextMap = ???
    *   inject(span.context(), Format.Builtin.TEXT_MAP, adapter)
    *   val tracingKey = adapter.toString
    * }}}
    *
    * @param spanContext the SpanContext instance to inject into the carrier
    * @param format the Format of the carrier
    * @param carrier the carrier for the SpanContext state.
    *
    * @see [[io.opentracing.Tracer#inject(io.opentracing.SpanContext, io.opentracing.propagation.Format, java.lang.Object)]]
    */
  def inject[C](spanContext: ot.SpanContext, format: ot.propagation.Format[C], carrier: C): Try[Unit]
}
