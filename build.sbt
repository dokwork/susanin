lazy val susanin = (project in file("."))
  .settings(
    organization := "ru.dokwork",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq(
      "-encoding",
      "utf-8",
      "-target:jvm-1.8",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xexperimental",
      "-Xlint",
      "-Ywarn-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-nullary-override"
    ),
    libraryDependencies ++= Seq(
      "io.opentracing"             % "opentracing-api"  % "0.32.0-RC1",
      "io.opentracing"             % "opentracing-util" % "0.32.0-RC1",
      "com.typesafe.scala-logging" %% "scala-logging"   % "3.7.2",
      // tests:
      "org.scalatest"  %% "scalatest"       % "3.0.0"      % "test",
      "ch.qos.logback" % "logback-classic"  % "1.1.7"      % "test",
      "io.opentracing" % "opentracing-mock" % "0.32.0-RC1" % "test"
    ),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    releaseReadmeFile := Some(baseDirectory.value / "README.md")
  )
  .settings(
    coverageMinimum := 42,
    coverageFailOnMinimum := true
  )
