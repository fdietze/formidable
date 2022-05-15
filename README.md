# Formidable

A typeclass for HTML forms, with automatic derivation in [Outwatch](github.com/outwatch/outwatch).

```scala
libraryDependencies += "com.github.fdietze" %%% "formidable" % "0.1.0-SNAPSHOT"
```


```scala
import formidable.{given, *}

case class Person(name: String, age: Option[Int])

val subject = Form.subject[Person]

div(
  Form[Person](subject),
  div(subject.map(_.toString)),
)
```


Automatically derive outwatch forms for:
- Primitives: `Int`, `String`, `Boolean`
- `Option[T]`
- `Seq[T]`
- sealed traits
- case classes
