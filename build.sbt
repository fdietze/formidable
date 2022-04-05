Global / onChangedBuildSource := IgnoreSourceChanges // not working well with webpack devserver

name                     := "Formidable"
ThisBuild / organization := "com.github.fdietze"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.1.1"

val versions = new {
  val outwatch  = "1.0.0-RC6+2-6728c9c7-SNAPSHOT"
  val scalaTest = "3.2.11"
}

ThisBuild / resolvers ++= Seq(
  "jitpack" at "https://jitpack.io",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Snapshots S01" at "https://s01.oss.sonatype.org/content/repositories/snapshots", // https://central.sonatype.org/news/20210223_new-users-on-s01/
)

def readJsDependencies(baseDirectory: File, field: String): Seq[(String, String)] = {
  val packageJson = ujson.read(IO.read(new File(s"$baseDirectory/package.json")))
  packageJson(field).obj.mapValues(_.str.toString).toSeq
}

lazy val formidable = project
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin,
  )
  .settings(
    libraryDependencies          ++= Seq(
      "io.github.outwatch" %%% "outwatch"  % versions.outwatch,
      "org.scalatest"      %%% "scalatest" % versions.scalaTest % Test,
    ),
    Compile / npmDependencies    ++= readJsDependencies(baseDirectory.value, "dependencies"),
    Compile / npmDevDependencies ++= readJsDependencies(baseDirectory.value, "devDependencies"),
    scalacOptions --= Seq(
      "-Xfatal-warnings",
    ), // overwrite option from https://github.com/DavidGregory084/sbt-tpolecat

    useYarn                := true, // Makes scalajs-bundler use yarn instead of npm
    Test / requireJsDomEnv := true,
  )
