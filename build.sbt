Global / onChangedBuildSource := IgnoreSourceChanges // not working well with webpack devserver

name                           := "Formidable"
ThisBuild / organization       := "com.github.fdietze"
ThisBuild / crossScalaVersions := Seq("2.13.8", "3.3.1")
ThisBuild / scalaVersion       := "2.13.8"

inThisBuild(
  List(
    organization := "com.github.fdietze",
    homepage     := Some(url("https://github.com/fdietze/formidable")),
    licenses     := Seq("MIT License" -> url("https://opensource.org/licenses/MIT")),
    developers := List(
      Developer(
        "fdietze",
        "Felix Dietze",
        "mail@felx.me",
        url("https://github.com/fdietze"),
      )
    ),
  )
)

val versions = new {
  val outwatch = "1.0.0-RC17+7-3d737d72-SNAPSHOT"
  val colibri  = "0.7.8+98-29351ce7-SNAPSHOT"

  val funPack   = "0.3.2"
  val scalaTest = "3.2.12"
}

ThisBuild / resolvers ++= Seq(
  "jitpack" at "https://jitpack.io",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Snapshots S01" at "https://s01.oss.sonatype.org/content/repositories/snapshots", // https://central.sonatype.org/news/20210223_new-users-on-s01/
)

lazy val scalaJsMacrotaskExecutor = Seq(
  // https://github.com/scala-js/scala-js-macrotask-executor
  libraryDependencies += "org.scala-js" %%% "scala-js-macrotask-executor" % "1.1.1"
)

def readJsDependencies(baseDirectory: File, field: String): Seq[(String, String)] = {
  val packageJson = ujson.read(IO.read(new File(s"$baseDirectory/package.json")))
  packageJson(field).obj.mapValues(_.str).toSeq
}

val enableFatalWarnings =
  sys.env.get("ENABLE_FATAL_WARNINGS").flatMap(value => scala.util.Try(value.toBoolean).toOption).getOrElse(false)

val isScala3 = Def.setting(CrossVersion.partialVersion(scalaVersion.value).exists(_._1 == 3))

lazy val commonSettings = Seq(
  // overwrite scalacOptions "-Xfatal-warnings" from https://github.com/DavidGregory084/sbt-tpolecat
  scalacOptions --= (if (enableFatalWarnings) Nil else Seq("-Xfatal-warnings")),
  scalacOptions ++= (if (isScala3.value) Nil
                     else Seq("-Vimplicits", "-Vtype-diffs")), // better error messages for implicit resolution
  scalacOptions ++= (if (isScala3.value) Seq("-Yretain-trees") else Nil), // recursive data structures with Scala 3
  scalacOptions ++= (if (isScala3.value) Seq("-scalajs") else Nil),       // needed for Scala3 + ScalaJS
)

lazy val formidable = project
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin,
  )
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.github.outwatch"   %%% "outwatch"         % versions.outwatch,
      "com.github.cornerman" %%% "colibri"          % versions.colibri,
      "com.github.cornerman" %%% "colibri-reactive" % versions.colibri,
    ) ++
      (if (isScala3.value) Seq("com.softwaremill.magnolia1_3" %%% "magnolia" % "1.3.0")
       else
         Seq("com.softwaremill.magnolia1_2" %%% "magnolia" % "1.1.6", "org.scala-lang" % "scala-reflect" % "2.13.12")),
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
    publish / skip := true,
    libraryDependencies ++= Seq(
      "io.github.outwatch" %%% "outwatch" % versions.outwatch
    ),
    Compile / npmDevDependencies ++= Seq(
      "@fun-stack/fun-pack" -> versions.funPack // sane defaults for webpack development and production, see webpack.config.*.js
    ),
    scalacOptions --= Seq(
      "-Xfatal-warnings"
    ), // overwrite option from https://github.com/DavidGregory084/sbt-tpolecat

    useYarn := true, // Makes scalajs-bundler use yarn instead of npm
    scalaJSLinkerConfig ~= (_.withModuleKind(
      ModuleKind.CommonJSModule
    )), // configure Scala.js to emit a JavaScript module instead of a top-level script
    scalaJSUseMainModuleInitializer   := true, // On Startup, call the main function
    webpack / version                 := "5.75.0",
    webpackCliVersion                 := "5.0.0",
    startWebpackDevServer / version   := "4.11.1",
    webpackDevServerPort              := 12345,
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
