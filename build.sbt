import WebKeys._

// TODO Replace with your project's/module's name
name := "maalka-benchmark"

// TODO Set your organization here; ThisBuild means it will apply to all sub-modules
organization in ThisBuild := "com.maalka"

// TODO Set your version here
version := "1.0.3.2"

scalaVersion in ThisBuild := "2.11.6"

maintainer in Linux := "Clay Teeter <clay.teeter@maalka.com>"
maintainer in Docker := "Clay Teeter <clay.teeter@maalka.com>"

packageSummary in Linux := "Benchmark tool"
packageDescription := "Benchmark tool"

linuxPackageMappings += packageTemplateMapping(s"/var/run/${name.value}/")() withUser name.value withGroup name.value

lazy val squants = ProjectRef(uri("https://github.com/Maalka/squants.git"), "squantsJVM")
lazy val root = (project in file(".")).enablePlugins(SbtWeb, PlayScala, JavaAppPackaging).dependsOn(squants)


// Dependencies
libraryDependencies ++= Seq(
  filters,
  cache,
  // WebJars (i.e. client-side) dependencies
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test",
  "org.mockito" % "mockito-all" % "1.10.19",
  "org.webjars" % "requirejs" % "2.1.22",
  "org.webjars" % "jquery" % "2.1.3",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "angularjs" % "1.4.7" exclude("org.webjars", "jquery"),
  "org.webjars" % "highcharts" % "4.2.3",
  "org.webjars" % "highstock" % "4.2.3",
  "org.webjars" % "matchmedia-ng" % "1.0.5"
)

// Scala Compiler Options
scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Xcheckinit", // runtime error when a val is not initialized due to trait hierarchies (instead of NPE somewhere else)
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)

routesGenerator := InjectedRoutesGenerator

//
// sbt-web configuration
// https://github.com/sbt/sbt-web
//

// Configure the steps of the asset pipeline (used in stage and dist tasks)
// rjs = RequireJS, uglifies, shrinks to one file, replaces WebJars with CDN
// digest = Adds hash to filename
// gzip = Zips all assets, Asset controller serves them automatically when client accepts them
pipelineStages := Seq(rjs, digest, gzip)

// RequireJS with sbt-rjs (https://github.com/sbt/sbt-rjs#sbt-rjs)
// ~~~
RjsKeys.paths += ("jsRoutes" -> ("/jsroutes" -> "empty:"))

//RjsKeys.mainModule := "main"

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

// Asset hashing with sbt-digest (https://github.com/sbt/sbt-digest)
// ~~~
// md5 | sha1
//DigestKeys.algorithms := "md5"
//includeFilter in digest := "..."
//excludeFilter in digest := "..."

// HTTP compression with sbt-gzip (https://github.com/sbt/sbt-gzip)
// ~~~
// includeFilter in GzipKeys.compress := "*.html" || "*.css" || "*.js"
// excludeFilter in GzipKeys.compress := "..."

// JavaScript linting with sbt-jshint (https://github.com/sbt/sbt-jshint)
// ~~~
// JshintKeys.config := ".jshintrc"

// All work and no play...
emojiLogs
