package ru.dokwork.susanin

import java.util

import io.opentracing.tag.Tag
import io.{ opentracing ⇒ ot }

final case class Span(wrapped: Option[ot.Span]) extends ot.Span {
  @volatile
  private var finished: Boolean = false

  def isFinished: Boolean                                = finished
  override def context(): ot.SpanContext                 = wrapped.map(_.context()).orNull
  override def setTag(key: String, value: String): Span  = change(_.setTag(key, value))
  override def setTag(key: String, value: Boolean): Span = change(_.setTag(key, value))
  override def setTag(key: String, value: Number): Span  = change(_.setTag(key, value))
  override def setTag[T](tag: Tag[T], value: T): Span    = change(_.setTag(tag, value))
  override def log(fields: util.Map[String, _]): Span    = change(_.log(fields))
  override def log(timestampMicroseconds: Long, fields: util.Map[String, _]): Span =
    change(_.log(timestampMicroseconds, fields))
  override def log(event: String): Span = change(_.log(event))
  override def log(timestampMicroseconds: Long, event: String): Span =
    change(_.log(timestampMicroseconds, event))
  override def setBaggageItem(key: String, value: String): Span =
    change(_.setBaggageItem(key, value))
  override def getBaggageItem(key: String): String = wrapped.map(_.getBaggageItem(key)).orNull
  override def setOperationName(operationName: String): Span =
    change(_.setOperationName(operationName))

  /**
    * Marks span as finished and invoke the same method from th wrapped span.
    */
  override def finish(): Unit = done(_.finish())

  /**
    * Marks span as finished and invoke the same method from th wrapped span.
    *
    * @param finishMicros time when span was finished.
    * @see [[io.opentracing.Span#finish(long)]]
    */
  override def finish(finishMicros: Long): Unit = done(_.finish(finishMicros))

  private def change(update: ot.Span ⇒ ot.Span): Span = copy(wrapped = wrapped.map(update))
  private def done(update: ot.Span ⇒ Unit): Unit = {
    wrapped.foreach(update)
    finished = true
  }

  override def toString: String =
    s"Span(wrapped=$wrapped, finished=$finished)"
}
