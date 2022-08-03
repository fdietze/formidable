package webapp

import outwatch._
import outwatch.dsl._
import cats.effect.SyncIO

import colibri.reactive._

import formidable._
import formidable.instances._
import formidable.Form._

case class Person(name: String = "hey", age: Option[Int] = Some(2), pets: Seq[Pet])
sealed trait Pet
case class Dog(name: String) extends Pet
case class Cat(name: String) extends Pet

object Main {

  def main(args: Array[String]): Unit =
    Outwatch.renderInto[SyncIO]("#app", div(app)).unsafeRunSync()

  def app: VModifier = Owned {

    val state: Var[Person] = Form.state[Person]

    div(
      Form[Person].apply(state),
//      div(state.map(_.toString)),
      div(state.observable.scanToList.map(_.take(4).map(e => div(e.toString)))),
    ): VModifier
  }

}
