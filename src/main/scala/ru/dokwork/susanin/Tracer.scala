package ru.dokwork.susanin

import scala.language.higherKinds

/**
  * Invokes function in the new span.
  */
trait Tracer[F[_]] extends Injector with Extractor {

  /**
    * @return current active span if exists or [[None]].
    */
  def activeSpan: Option[Span]

  /**
    * Invokes `buildSpan` to build new span and applies it to the `f`. If the span was not finished
    * by `f`, it should be finished by this method when effect `F` with result from `f` will
    * be completed.
    *
    * @param spanName name of the new span.
    * @param buildSpan function to prepare new span.
    * @param f function wrapped in new span.
    * @tparam A type of the result of the `f`.
    * @return result of the `f`.
    */
  def newSpan[A](
      spanName: String,
      buildSpan: SpanBuilder ⇒ Span = _.start()
  )(f: Span ⇒ F[A]): F[A]

  /**
    * Saves spans graph before invoke `f` and recoveries it after result of the `f` will be completed.
    * Any spans inside `f` will be ignored.
    *
    * @param f function which will be ignored in the spans graph.
    * @tparam A type of the result of the `f`.
    * @return result of the `f`.
    */
  def transit[A](f: ⇒ F[A]): F[A]
}

object Tracer {

  implicit def apply[F[_]](implicit instance: Tracer[F]): Tracer[F] = instance

  def noop[F[_]] = new NoopTracer[F]
}
