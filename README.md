# Formidable

A typeclass for HTML forms, with automatic derivation in [Outwatch](github.com/outwatch/outwatch).

Currently only working with `Scala 3` and not released yet.

Publish the library locally:

```bash
sbt publishLocal
```

Then include it in the `build.sbt` of a local project:
```scala
libraryDependencies += "com.github.fdietze" %%% "formidable" % "0.1.0-SNAPSHOT"
```


```scala
import formidable.{given, *}

case class Person(name: String, age: Option[Int])

val state = Form.state[Person]

div(
  Form[Person](state),
  div(state.map(_.toString)),
)
```


Automatically derive outwatch forms for:
- Primitives: `Int`, `String`, `Boolean`
- `Option[T]`
- `Seq[T]`
- sealed traits
- case classes

TODO:
- recursive case classes (Leaf/Node tree)
- Any `Seq[T]`, like `Vector[T]`
