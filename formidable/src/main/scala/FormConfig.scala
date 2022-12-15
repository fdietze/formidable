package formidable

import outwatch._
import outwatch.dsl._
import colibri.reactive._

trait FormConfig {
  def withRemoveButton(subForm: VModifier, removeButton: VModifier): VModifier =
    div(display.flex, alignItems.flexStart, removeButton, subForm)
  def withCheckbox(subForm: VModifier, checkbox: VModifier): VModifier =
    div(display.flex, alignItems.flexStart, checkbox, subForm)
  def withLabel(subForm: VModifier, label: VModifier): VModifier = div(display.flex, label, subForm)

  def unlabeledFormGroup(subForms: Seq[VModifier]): VModifier =
    div(display.flex, VModifier.style("gap") := "0.5rem", subForms)
  def labeledFormGroup(subForms: Seq[(String, VModifier)]): VModifier =
    table(
      subForms.map { case (label, subForm) => tr(td(b(label, ": "), verticalAlign := "top"), td(subForm)) }
    )

  def formSequence(subForms: Seq[VModifier], addButton: VModifier): VModifier =
    div(
      display.flex,
      flexDirection.column,
      VModifier.style("gap") := "0.5rem",
      subForms,
      addButton,
    )

  def addButton(action: () => Unit): VModifier =
    button(
      "add",
      onClick.stopPropagation.doAction {
        action()
      },
    )

  def removeButton(action: () => Unit): VModifier =
    button(
      "remove",
      onClick.stopPropagation.doAction {
        action()
      },
    )

  def checkbox(state: Var[Boolean]): VModifier =
    input(
      tpe := "checkbox",
      checked <-- state,
      onClick.stopPropagation.checked --> state,
    )

  def textInput(
    state: Var[String],
    inputPlaceholder: String = "",
    validationMessage: Rx[Option[String]] = Rx.const(None),
  ): VModifier = Owned {
    div(
      input(
        tpe         := "text",
        placeholder := inputPlaceholder,
        value <-- state,
        onInput.stopPropagation.value --> state,
      ),
      validationMessage.map(_.map(msg => div(msg, color.red))),
    ): VModifier
  }

  def selectInput[T](options: Seq[T], selectedValue: Var[T], show: T => String): VModifier = {
    select(
      Owned.function { implicit owner =>
        options.zipWithIndex.map { case (opt, ind) =>
          option(dsl.value := ind.toString)(
            show(opt),
            selectedValue.map(sel => selected := opt == sel),
          )
        }
      },
      onChange.value.map(index => options(index.toInt)) --> selectedValue,
    )
  }

  def unionSubform(selectForm: VModifier, subForm: VModifier) = div(selectForm, subForm)
}
object FormConfig {
  val default: FormConfig = new FormConfig {}
}
