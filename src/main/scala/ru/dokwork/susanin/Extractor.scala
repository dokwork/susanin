package ru.dokwork.susanin

import io.{ opentracing ⇒ ot }
import scala.util.Try

trait Extractor {

  /**
    * Extract a [[io.opentracing.SpanContext]] from a `carrier` of a given type.
    *
    * @example
    * {{{
    *   val adapter: io.opentracing.propagation.TextMap = ???
    *   val spanContext = extract(Format.Builtin.TEXT_MAP, adapter)
   *      .getOrElse(throw new Exception("Exception on extract span context.", e))
    * }}}
    *
    * @param format the Format of the carrier
    * @param carrier the carrier for the SpanContext state.
    *
    * @return the SpanContext instance holding context to create a Span in case of success, or
   *         [[scala.util.Failure]] with reason.
    *
    * @see io.opentracing.Tracer#extract(io.opentracing.propagation.Format, java.lang.Object)
    */
  def extract[C](format: ot.propagation.Format[C], carrier: C): Try[ot.SpanContext]
}
