package ru.dokwork.susanin

import io.{ opentracing ⇒ ot }

/**
 * Compatible with `opentracing-java` api builder of the [[Span]].
 *
 * @param activeSpan active span at current moment.
 * @param builder instance of the [[io.opentracing.Tracer.SpanBuilder]].
 * @param isReferenceSpecified `true` - when at least one reference added to the span.
 * @param isIgnoreActiveSpan if `true` then active span will be ignored and new span will be root.
 */
final case class SpanBuilderImpl private[susanin] (
  activeSpan: Span,
  private val builder: ot.Tracer.SpanBuilder,
  private val isReferenceSpecified: Boolean = false,
  private val isIgnoreActiveSpan: Boolean = false
) extends SpanBuilder {

  def asChildOf(parent: ot.SpanContext): SpanBuilderImpl =
    copy(builder = builder.asChildOf(parent), isReferenceSpecified = true)

  def asChildOf(parent: ot.Span): SpanBuilderImpl =
    copy(builder = builder.asChildOf(parent), isReferenceSpecified = true)

  def asChildOf(parent: Span): SpanBuilderImpl =
    copy(builder = builder.asChildOf(parent), isReferenceSpecified = true)

  override def ignoreActiveSpan(): SpanBuilderImpl =
    copy(isIgnoreActiveSpan = true)

  override def withTag(key: String, value: String): SpanBuilderImpl =
    copy(builder = builder.withTag(key, value))

  override def withTag(key: String, value: Boolean): SpanBuilderImpl =
    copy(builder = builder.withTag(key, value))

  override def withTag(key: String, value: Number): SpanBuilderImpl =
    copy(builder = builder.withTag(key, value))

  override def withStartTimestamp(microseconds: Long): SpanBuilderImpl =
    copy(builder = builder.withStartTimestamp(microseconds))

  override def start(): Span =
    if (isIgnoreActiveSpan || isReferenceSpecified)
      Span(Option(builder.start()))
    else
      asChildOf(activeSpan).start()
}
