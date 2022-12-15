package webapp

import outwatch._
import outwatch.dsl._
import cats.effect.SyncIO
import colibri.reactive._

import formidable._

case class Person(name: String, age: Int = 5)

sealed trait Pet
object Pet {
  case class Dog(name: String, hungry: Boolean = true) extends Pet
  @formidable.Default
  case class Cat(name: String, legs: Int = 4) extends Pet
}

case class Address(city: String, street: String)
case class Company(name: String, address: Option[Address])

case class Tree(value: Int = 2, children: Seq[Tree])

sealed trait BinaryTree
object BinaryTree {
  @formidable.Default
  case class Leaf(value: Int)                            extends BinaryTree
  case class Branch(left: BinaryTree, right: BinaryTree) extends BinaryTree
}

sealed trait GenericLinkedList[+T]
object GenericLinkedList {
  case class Cons[+T](head: T, tail: GenericLinkedList[T]) extends GenericLinkedList[T]
  @formidable.Default
  case object Nil extends GenericLinkedList[Nothing]
}

object Main extends Extras {

  def main(args: Array[String]): Unit =
    Outwatch.renderInto[SyncIO]("#app", app).unsafeRunSync()

  def app: VNode = {
    div(
      formFrame[String]("String"),
      formFrame[Int]("Int"),
      formFrame[Double]("Double"),
      formFrame[Long]("Long"),
      formFrame[Boolean]("Boolean"),
      formFrame[Option[Int]]("Option[Int]"),
      formFrame[Seq[Int]]("Seq[Int]"),
      formFrame[List[Int]]("List[Int]"),
      formFrame[Vector[Int]]("Vector[Int]"),
      formFrame[(Int, String, Option[Long])]("Tuple"),
      formFrame[Person]("Person"),
      formFrame[Pet]("Pet"),
      formFrame[Company]("Company"),
      formFrame[Tree]("Tree"),
      formFrame[BinaryTree]("BinaryTree"),
      formFrame[GenericLinkedList[Pet]]("GenericLinkedList[Pet]"),
    )
  }

  def formFrame[T: Form](name: String): VModifier = Owned {
    val state = Form.state[T]
    div(
      cls          := name,
      marginBottom := "5px",
      padding      := "5px",
      border       := "1px solid black",
      div(i(name)),
      Form[T].render(state),
      div("value: ", span(cls := "value", state.map(_.toString))),
    ): VModifier
  }
}
