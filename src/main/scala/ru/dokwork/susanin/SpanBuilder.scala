package ru.dokwork.susanin

import io.{ opentracing ⇒ ot }

trait SpanBuilder {

  def ignoreActiveSpan(): SpanBuilder

  def asChildOf(parent: ot.SpanContext): SpanBuilder

  def asChildOf(parent: Span): SpanBuilder

  def withTag(key: String, value: String): SpanBuilder

  def withTag(key: String, value: Boolean): SpanBuilder

  def withTag(key: String, value: Number): SpanBuilder

  def withStartTimestamp(microseconds: Long): SpanBuilder

  def start(): Span
}

object SpanBuilder {

  /**
   * Wraps specified [[io.opentracing.Tracer.SpanBuilder]] and uses `activeSpan` as parent for
   * building the span by default.
   */
  def apply(wrapped: ot.Tracer.SpanBuilder, activeSpan: ot.Span): SpanBuilder =
    SpanBuilderImpl(Span(Option(activeSpan)), wrapped.ignoreActiveSpan())

  def apply(wrapped: ot.Tracer.SpanBuilder, activeSpan: Option[ot.Span]): SpanBuilder =
    SpanBuilderImpl(Span(activeSpan), wrapped.ignoreActiveSpan())
}
