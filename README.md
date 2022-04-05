# Formidable

A typeclass for HTML forms, with automatic derivation in [Outwatch](github.com/outwatch/outwatch).

```scala
libraryDependencies += "com.github.fdietze" %%% "formidable" % "0.1.0-SNAPSHOT"
```


```scala
import formidable.{given, *}

case class Person(name: String, age: Option[Int])

val subject = Subject.behavior(summon[Form[Person]].default)

div(
  summon[Form[Person]].form(subject),
  div(subject.map(_.toString)),
)
```


Automatically derive outwatch forms for:
- Primitives: `Int`, `String`, `Boolean`
- `Option[T]`
- `Seq[T]`
- `Map[A,B]`
- sealed traits
- case classes
