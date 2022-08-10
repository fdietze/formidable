# Formidable

Generates reactive HTML forms directly from type definitions. These forms can be used with ScalaJS and [Outwatch](github.com/outwatch/outwatch) and the generated HTML/CSS is fully customizable. Scala 2.13 and 3 are supported. Here's a [Demo](https://fdietze.github.io/formidable) and it's [Code](demo/src/main/scala-3/Main.scala).

Formidable defines a typeclass [`Form[T]`](formidable/src/main/scala/Form.scala) with many default instances:
- Primitives: `Int`, `Double`, `Long`, `String`, `Boolean`
- `Option[T]`
- `Seq[T]`
- sealed traits
- tuples
- case classes
- recursive types
- generic types

Automatic derivation is done using [Magnolia](https://github.com/softwaremill/magnolia).

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


