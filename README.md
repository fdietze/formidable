# Formidable

Generates reactive HTML forms directly from type definitions. These forms can used with ScalaJS and [Outwatch](github.com/outwatch/outwatch). Scala 2.13 and 3 are supported.

Formidable defines a typeclass [`Form[T]`](formidable/src/main/scala/Form.scala) with many default instances:
- Primitives: `Int`, `Double`, `Long`, `String`, `Boolean`
- `Option[T]`
- `Seq[T]`
- sealed traits
- case classes
- recursive types
- generic types

Automatic derivation is done using [Magnolia](https://github.com/softwaremill/magnolia).

To publish the library locally:

```bash
sbt publishLocal
```

Then include it in the `build.sbt` of a local project:
```scala
libraryDependencies += "com.github.fdietze" %%% "formidable" % "0.1.0-SNAPSHOT"
```


```scala
import formidable._
import formidable.instances._

case class Person(name: String, age: Option[Int])

val state = Form.state[Person]

div(
  Form[Person].apply(state),
  div(state.map(_.toString)),
)
```


