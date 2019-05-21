package ru.dokwork.susanin

import java.util.concurrent.TimeUnit

import ru.dokwork.susanin.concurrent.TraceableScheduledExecutorService

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ Future, Promise }

package object future {

  /**
    * Creates an asynchronous task that on evaluation sleeps for the
    * specified duration, emitting a notification on completion.
    *
    * @param duration time to sleep.
    * @param scheduler traceable version of the [[java.util.concurrent.ScheduledExecutorService]] to
    *                  save trace context.
    * @return future which will be completed after `duration`.
    */
  def sleep(
      duration: FiniteDuration
  )(implicit scheduler: TraceableScheduledExecutorService): Future[Unit] = {
    val p = Promise[Unit]()
    scheduler.schedule(() ⇒ p.success({}), duration.toMillis, TimeUnit.MILLISECONDS)
    p.future
  }
}
