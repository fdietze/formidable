package formidable

import outwatch._
import outwatch.dsl._
import colibri._
import colibri.reactive._
import org.scalajs.dom.console
import org.scalajs.dom.HTMLInputElement

import scala.deriving.Mirror
import scala.deriving._
import scala.compiletime.{constValueTuple, erasedValue, summonInline}

// TODO: List instead of only Seq

given Form[String] with {
  def default                                                              = ""
  def apply(state: Var[String], formModifiers: FormModifiers)(using Owner) =
    inputField(state, inputTpe = "text", parse = str => str, toValue = value => value)(formModifiers.inputModifiers)
}

given Form[Int] with {
  def default                                                           = 0
  def apply(state: Var[Int], formModifiers: FormModifiers)(using Owner) =
    inputField(
      state,
      inputTpe = "number",
      parse = { case str if str.toIntOption.isDefined => str.toInt },
      toValue = value => value.toString,
    )
}

given Form[Boolean] with {
  def default                                                               = false
  def apply(state: Var[Boolean], formModifiers: FormModifiers)(using Owner) =
    input(
      tpe := "checkbox",
      checked <-- state,
      onClick.checked --> state,
      formModifiers.checkboxModifiers,
    ),
}

given optionForm[T: Form]: Form[Option[T]] with {
  def default                                                                 = None
  def apply(state: Var[Option[T]], formModifiers: FormModifiers)(using Owner) = {
    val seqState: Var[Seq[T]] = state.imap[Seq[T]](_.headOption)(_.toSeq)
    var innerBackup: T        = summon[Form[T]].default

    div(
      display.flex,
      alignItems.center,
      input(
        tpe := "checkbox",
        checked <-- state.map(_.isDefined),
        onClick.checked.map {
          case true  => Some(innerBackup)
          case false => None
        } --> state,
        formModifiers.checkboxModifiers,
      ),
      seqState.sequence.map(
        _.map { innerState =>
          innerState.foreach(innerBackup = _)
          Form[T](innerState, formModifiers),
        },
      ),
    )
  }
}

given seqForm[T: Form]: Form[Seq[T]] with {
  def default                                                              = Seq.empty
  def apply(state: Var[Seq[T]], formModifiers: FormModifiers)(using Owner) = {
    div(
      state.sequence.map(
        _.zipWithIndex.map { case (innerState, i) =>
          div(
            Form[T](innerState, formModifiers),
            button(
              "remove",
              onClick.stopPropagation(state).foreach { nowValue =>
                state.set(nowValue.patch(i, Nil, 1))
              },
              formModifiers.buttonModifiers,
            ),
          )
        },
      ),
      button(
        "add",
        onClick(state).foreach { nowValue =>
          state.set(nowValue :+ Form[T].default)
        },
        formModifiers.buttonModifiers,
      ),
    )
  }
}

private def inputField[T](
  state: Var[T],
  inputTpe: String,
  parse: PartialFunction[String, T],
  toValue: T => String,
)(using Owner) = input(
  tpe := inputTpe,
  value <-- state.map(toValue),
  onInput.value.collect(parse) --> state,
)
