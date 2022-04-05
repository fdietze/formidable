package formidable

import outwatch._
import outwatch.dsl._
import colibri._
import org.scalajs.dom.console
import org.scalajs.dom.HTMLInputElement

import scala.deriving.Mirror
import scala.deriving._
import scala.compiletime.{constValueTuple, erasedValue, summonInline}

def submittableForm[T: Form](onSubmit: T => Unit)(using f: Form[T]) = {
  val subject = Subject.behavior(f.default)
  div(
    f.form(subject),
    button("submit", onClick(subject).foreach(onSubmit)),
  )
}
