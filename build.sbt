import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Paths }

import sbt.{ Credentials, Path }
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.Vcs

import scala.io.Source
import scala.sys.process.ProcessLogger

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
    licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
  )
  .settings(
    coverageMinimum := 45,
    coverageFailOnMinimum := true
  )

lazy val updateReadme = ReleaseStep(action = st ⇒ {
  val version = Project.extract(st).get(Keys.version)
  val readme =
    Source.fromFile("README.md").mkString.replaceAll("(\\d+\\.?)+(\\-SNAPSHOT)?", version)
  Files.write(Paths.get("README.md"), readme.getBytes(StandardCharsets.UTF_8))
  vcs(st).add()
  st
})
lazy val commitReadme = ReleaseStep(action =  st ⇒ {
  val sign = Project.extract(st).get(releaseVcsSign)
  val signOff = Project.extract(st).get(releaseVcsSignOff)
  vcs(st).add("./README.md") !! logger(st)
  vcs(st).commit("Version in the README updated.", sign, signOff) ! logger(st)
  st
})

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  updateReadme,
  commitReadme,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

def vcs(st: State): Vcs = {
  Project.extract(st).get(releaseVcs)
    .getOrElse(sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))
}

def logger(st: State): ProcessLogger = new ProcessLogger {
  override def err(s: => String): Unit = st.log.info(s)
  override def out(s: => String): Unit = st.log.info(s)
  override def buffer[T](f: => T): T = st.log.buffer(f)
}