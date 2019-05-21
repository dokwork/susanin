package ru.dokwork.susanin

import ru.dokwork.susanin.future._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }

class FutureTracerSpec extends Spec {

  "FutureTracer" - {
    "should begin span before and finish it after function" in test { f ⇒
      import f._
      val start = System.currentTimeMillis()
      for {
        _ ← tracer.newSpan("test")(unit)
      } yield {
        assert(finishedSpans().loneElement.finishMicros() > (start + 300))
      }
    }
    "should mark inner span as child of the outer" in test { f ⇒
      import f._
      for {
        _ ← tracer.newSpan("outer") { _ ⇒
          tracer.newSpan("inner")(unit)
        }
      } yield {
        span("inner").parentId() shouldBe span("outer").context().spanId()
      }
    }
    "should mark both inner spans as children of the outer" in test { f ⇒
      import f._
      for {
        _ ← tracer.newSpan("outer") { _ ⇒
          tracer.newSpan("inner1")(unit) >>
            tracer.newSpan("inner2")(unit)
        }
      } yield {
        span("inner1").parentId() shouldBe span("outer").context().spanId()
        span("inner2").parentId() shouldBe span("outer").context().spanId()
      }
    }
    "should broke graph with nested spans when used not traceable ec" in test { f ⇒
      import f._
      for {
        _ ← tracer.newSpan("outer") { _ ⇒
          Future.unit // you must use TraceableExecutionContext in every places in your project!
            .flatMap(_ ⇒ tracer.newSpan("inner")(unit))(ExecutionContext.global)
        }
      } yield {
        span("inner").parentId() shouldBe 0
        span("outer").parentId() shouldBe 0
      }
    }
    "should broke graph with sequence spans when used not traceable ec" in test { f ⇒
      import f._
      for {
        _ ← tracer.newSpan("outer") { _ ⇒
          tracer
            .newSpan("first")(unit)
            .flatMap(_ ⇒ tracer.newSpan("second")(unit))(ExecutionContext.global)
        }
      } yield {
        span("first").parentId() shouldBe span("outer").context().spanId()
        span("second").parentId() should not be span("outer").context().spanId()
      }
    }
  }

  "Method sleep" - {
    "should not lose tracing context" in test { f ⇒
      import f._
      for {
        _ ← tracer.newSpan("outer") { _ ⇒
          sleep(10.millis) >> tracer.newSpan("inner")(_ ⇒ sleep(10.millis))
        }
      } yield {
        span("inner").parentId() shouldBe span("outer").context().spanId()
      }
    }
  }

  "Method transit" - {
    "should fix graph with sequence spans when used not traceable ec" in {
      val p = Promise[Unit]()
      val result = test { f ⇒
        import f._
        for {
          _ ← tracer.newSpan("outer") { _ ⇒
            for {
              _ ← tracer.newSpan("first")(unit)
              _ ← tracer.transit(p.future) // fix graph
              _ ← tracer.newSpan("second")(unit)
            } yield ()
          }
        } yield {
          span("first").parentId() shouldBe span("outer").context().spanId()
          span("second").parentId() shouldBe span("outer").context().spanId()
        }
      }
      // when:
      // resolving promise on not traceable thread breaks the graph:
      ExecutionContext.global.execute(() ⇒ p.success({}))
      result
    }
  }
}
