import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

def Scala213 = "2.13.16"

crossScalaVersions := Seq(
  "2.12.20",
  Scala213,
  "3.3.5",
)

scalaVersion := Scala213

scalacOptions ++= {
  scalaBinaryVersion.value match {
    case "2.12" =>
      Seq(
        "-Xsource:3",
      )
    case "2.13" =>
      Seq(
        "-Xsource:3-cross",
      )
    case _ =>
      Nil
  }
}

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
)

scalacOptions ++= {
  scalaBinaryVersion.value match {
    case "2.13" =>
      Seq("-Ymacro-annotations")
    case _ =>
      Nil
  }
}

pomExtra := (
  <developers>
    <developer>
      <id>xuwei-k</id>
      <name>Kenji Yoshida</name>
      <url>https://github.com/xuwei-k</url>
    </developer>
  </developers>
  <scm>
    <url>git@github.com:xuwei-k/shapeless-annotation.git</url>
    <connection>scm:git:git@github.com:xuwei-k/shapeless-annotation.git</connection>
  </scm>
)

name := "shapeless-annotation"

description := "shapeless macro annotation"

organization := "com.github.xuwei-k"

homepage := Some(url("https://github.com/xuwei-k/shapeless-annotation"))

licenses := List(
  "MIT License" -> url("https://opensource.org/licenses/mit-license")
)

libraryDependencies ++= {
  scalaBinaryVersion.value match {
    case "2.12" | "2.13" =>
      Seq(
        scalaOrganization.value % "scala-reflect" % scalaVersion.value,
        "com.chuusai" %% "shapeless" % "2.3.13" % Test
      )
    case _ =>
      Nil
  }
}

libraryDependencies ++= {
  scalaBinaryVersion.value match {
    case "2.12" =>
      Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch))
    case _ =>
      // https://github.com/scala/scala/pull/6606
      Nil
  }
}

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.6.4"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+ publishSigned"),
  releaseStepCommandAndRemaining("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

publishTo := sonatypePublishToBundle.value
