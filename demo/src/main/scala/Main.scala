package webapp

import outwatch._
import outwatch.dsl._
import cats.effect.SyncIO
import colibri.reactive._
import formidable._
import formidable.instances._
import formidable.Form._

case class Person(name: String, age: Int = 5)

sealed trait Pet
object Pet {
  case class Dog(name: String, hungry: Boolean) extends Pet
  case class Cat(name: String, legs: Int = 4)   extends Pet
}

sealed trait BinaryTree
object BinaryTree {
  @formidable.Default
  case class Leaf(value: String)                         extends BinaryTree
  case class Branch(left: BinaryTree, right: BinaryTree) extends BinaryTree
}

sealed trait Tree
object Tree {
  @formidable.Default
  case object Leaf                                    extends Tree
  case class Node(value: String, children: Seq[Tree]) extends Tree
}

sealed trait GenericList[+T]
object GenericTree {
  @formidable.Default
  case object Nil                                    extends GenericList[Nothing]
  case class Cons[+T](head: T, tail: GenericList[T]) extends GenericList[T]
}

object Main {

  def main(args: Array[String]): Unit =
    Outwatch.renderInto[SyncIO]("#app", app).unsafeRunSync()

  def app: VNode = {
    // needed for recursion with Scala 3 (https://github.com/softwaremill/magnolia#limitations)
//    implicit def binaryTreeInstance: Form[BinaryTree]           = Form.derived
//    implicit def treeInstance: Form[Tree]                       = Form.derived
//    implicit def genericListInstance: Form[GenericList[String]] = Form.derived

    div(
      formFrame[String]("string"),
      formFrame[Int]("int"),
      formFrame[Double]("double"),
      formFrame[Long]("long"),
      formFrame[Boolean]("boolean"),
      formFrame[Option[Int]]("option-int"),
      formFrame[Seq[Int]]("seq-int"),
      formFrame[Person]("person"),
      formFrame[Pet]("pet"),
      formFrame[BinaryTree]("binarytree"),
      formFrame[Tree]("tree"),
      formFrame[GenericList[String]]("generic-list"),
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
      Form[T].apply(state),
      div(cls := "value", "value: ", state.map(_.toString)),
    ): VModifier
  }
}
