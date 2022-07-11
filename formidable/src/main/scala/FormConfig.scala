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

trait FormConfig  {
  def withRemoveButton(subForm: VModifier, removeButton: VModifier) = div(display.flex, subForm, removeButton)
  def withCheckbox(subForm: VModifier, checkbox: VModifier)         = div(display.flex, checkbox, subForm)
  def withLabel(subForm: VModifier, label: VModifier)               = div(display.flex, label, subForm)

  def unlabeledFormGroup(subForms: Seq[VModifier])         = div(display.flex, VModifier.style("gap") := "0.5rem", subForms)
  def labeledFormGroup(subForms: Seq[(String, VModifier)]) =
    table(
      subForms.map { case (label, subForm) => tr(td(b(label, ": "), verticalAlign := "top"), td(subForm)) },
    )

  def formSequence(subForms: Seq[VModifier], addButton: VModifier) =
    div(
      display.flex,
      flexDirection.column,
      VModifier.style("gap") := "0.5rem",
      subForms,
    )

  def addButton(action: () => Unit) =
    button(
      "add",
      onClick.stopPropagation.doAction {
        action()
      },
    )

  def removeButton(action: () => Unit) =
    button(
      "remove",
      onClick.stopPropagation.doAction {
        action()
      },
    )

  def checkbox(state: Var[Boolean])(using Owner) =
    input(
      tpe := "checkbox",
      checked <-- state,
      onClick.stopPropagation.checked --> state,
    )

  def textInput(
    state: Var[String],
    inputPlaceholder: String = "",
    validationMessage: Rx[Option[String]] = Rx.const(None),
  )(using
    Owner,
  ): VNode = {
    div(
      input(
        tpe         := "text",
        placeholder := inputPlaceholder,
        value <-- state,
        onInput.stopPropagation.value --> state,
      ),
      validationMessage.map(_.map(msg => div(msg, color.red))),
    )
  }
}
object FormConfig {
  val default = new FormConfig {}
}
