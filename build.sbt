Global / onChangedBuildSource := IgnoreSourceChanges // not working well with webpack devserver

name                     := "Formidable"
ThisBuild / organization := "com.github.fdietze"
ThisBuild / scalaVersion := "3.1.3"

val versions = new {
  val outwatch  = "1.0.0-RC8"
  val colibri   = "0.6.0+3-ff10e1ed+20220702-2309-SNAPSHOT" // https://github.com/cornerman/colibri/pull/203
  val funPack   = "0.2.0"
  val scalaTest = "3.2.12"
}

ThisBuild / resolvers ++= Seq(
  "jitpack" at "https://jitpack.io",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Snapshots S01" at "https://s01.oss.sonatype.org/content/repositories/snapshots", // https://central.sonatype.org/news/20210223_new-users-on-s01/
)

lazy val scalaJsMacrotaskExecutor = Seq(
  // https://github.com/scala-js/scala-js-macrotask-executor
  libraryDependencies       += "org.scala-js" %%% "scala-js-macrotask-executor" % "1.0.0",
  Compile / npmDependencies += "setimmediate"  -> "1.0.5", // polyfill
)

def readJsDependencies(baseDirectory: File, field: String): Seq[(String, String)] = {
  val packageJson = ujson.read(IO.read(new File(s"$baseDirectory/package.json")))
  packageJson(field).obj.mapValues(_.str.toString).toSeq
}

val enableFatalWarnings =
  sys.env.get("ENABLE_FATAL_WARNINGS").flatMap(value => scala.util.Try(value.toBoolean).toOption).getOrElse(false)

lazy val commonSettings = Seq(
  // overwrite scalacOptions "-Xfatal-warnings" from https://github.com/DavidGregory084/sbt-tpolecat
  if (enableFatalWarnings) scalacOptions += "-Xfatal-warnings" else scalacOptions -= "-Xfatal-warnings",
  scalacOptions ++= Seq("-Vimplicits", "-Vtype-diffs"),
)

lazy val formidable = project
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin,
  )
  .dependsOn(ProjectRef(file("../colibri"), "colibri"))  // TODO: specific commit
  .dependsOn(ProjectRef(file("../colibri"), "reactive")) // TODO: specific commit
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.github.outwatch" %%% "outwatch" % versions.outwatch,
      /* "com.github.cornerman" %%% "colibri"          % versions.colibri, */
      /* "com.github.cornerman" %%% "colibri-reactive" % versions.colibri, */
      "org.scalatest" %%% "scalatest" % versions.scalaTest % Test,
    ),
    Compile / npmDependencies    ++= readJsDependencies(baseDirectory.value, "dependencies"),
    Compile / npmDevDependencies ++= readJsDependencies(baseDirectory.value, "devDependencies"),
    useYarn                       := true, // Makes scalajs-bundler use yarn instead of npm
    Test / requireJsDomEnv        := true,
  )

lazy val demo = project
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin,
  )
  .dependsOn(formidable)
  .settings(commonSettings, scalaJsMacrotaskExecutor)
  .settings(
    libraryDependencies ++= Seq(
      "io.github.outwatch" %%% "outwatch" % versions.outwatch,
    ),
    Compile / npmDevDependencies ++= Seq(
      "@fun-stack/fun-pack" -> versions.funPack, // sane defaults for webpack development and production, see webpack.config.*.js
    ),
    scalacOptions --= Seq(
      "-Xfatal-warnings",
    ), // overwrite option from https://github.com/DavidGregory084/sbt-tpolecat

    useYarn := true, // Makes scalajs-bundler use yarn instead of npm
    scalaJSLinkerConfig ~= (_.withModuleKind(
      ModuleKind.CommonJSModule,
    )), // configure Scala.js to emit a JavaScript module instead of a top-level script
    scalaJSUseMainModuleInitializer   := true, // On Startup, call the main function
    webpackDevServerPort              := 12345,
    webpack / version                 := "4.46.0",
    startWebpackDevServer / version   := "3.11.3",
    webpackDevServerExtraArgs         := Seq("--color"),
    fullOptJS / webpackEmitSourceMaps := true,
    fastOptJS / webpackBundlingMode := BundlingMode
      .LibraryOnly(), // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance
    fastOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack.config.dev.js"),
    fullOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack.config.prod.js"),
    Test / requireJsDomEnv        := true,
  )

addCommandAlias("prod", "demo/fullOptJS/webpack")
addCommandAlias("dev", "devInit; devWatchAll; devDestroy")
addCommandAlias("devInit", "; demo/fastOptJS/startWebpackDevServer")
addCommandAlias("devWatchAll", "~; demo/fastOptJS/webpack")
addCommandAlias("devDestroy", "demo/fastOptJS/stopWebpackDevServer")
