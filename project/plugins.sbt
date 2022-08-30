addSbtPlugin("org.scala-js"  % "sbt-scalajs"         % "1.10.1")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")

addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.1")
addSbtPlugin("org.scalameta"             % "sbt-scalafmt" % "2.4.6")

// for reading npmDependencies from package.json
libraryDependencies ++= Seq("com.lihaoyi" %% "upickle" % "1.6.0")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")
