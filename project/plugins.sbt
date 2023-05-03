addSbtPlugin("org.scala-js"  % "sbt-scalajs"         % "1.13.1")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1")

addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.1")
addSbtPlugin("org.scalameta"             % "sbt-scalafmt" % "2.5.0")

// for reading npmDependencies from package.json
libraryDependencies ++= Seq("com.lihaoyi" %% "upickle" % "3.1.0")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")
