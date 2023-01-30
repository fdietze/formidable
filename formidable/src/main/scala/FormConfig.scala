package formidable

import outwatch._
import outwatch.dsl._
import colibri.reactive._

trait FormConfig {
  def withRemoveButton(subForm: VMod, removeButton: VMod): VMod =
    div(display.flex, alignItems.flexStart, removeButton, subForm)
  def withCheckbox(subForm: VMod, checkbox: VMod): VMod =
    div(display.flex, alignItems.flexStart, checkbox, subForm)
  def withLabel(subForm: VMod, label: VMod): VMod = div(display.flex, label, subForm)

  def unlabeledFormGroup(subForms: Seq[VMod]): VMod =
    div(display.flex, VMod.style("gap") := "0.5rem", subForms)
  def labeledFormGroup(subForms: Seq[(String, VMod)]): VMod =
    table(
      subForms.map { case (label, subForm) => tr(td(b(label), verticalAlign := "top"), td(subForm)) }
    )

  def formSequence(subForms: Seq[VMod], addButton: VMod): VMod =
    div(
      display.flex,
      flexDirection.column,
      VMod.style("gap") := "0.5rem",
      subForms,
      addButton,
    )

  def addButton(action: () => Unit): VMod =
    button(
      "add",
      onClick.stopPropagation.doAction {
        action()
      },
    )

  def removeButton(action: () => Unit): VMod =
    button(
      "remove",
      onClick.stopPropagation.doAction {
        action()
      },
    )

  def checkbox(state: Var[Boolean]): VMod =
    input(
      tpe := "checkbox",
      checked <-- state,
      onClick.stopPropagation.checked --> state,
    )

  def textInput(
    state: Var[String],
    inputPlaceholder: String = "",
    validationMessage: Rx[Option[String]] = Rx.const(None),
  ): VMod = Owned {
    div(
      input(
        tpe         := "text",
        placeholder := inputPlaceholder,
        value <-- state,
        onInput.stopPropagation.value --> state,
      ),
      validationMessage.map(_.map(msg => div(msg, color.red))),
    ): VMod
  }

  def selectInput[T](options: Seq[T], selectedValue: Var[T], show: T => String): VMod = {
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

  def unionSubform(selectForm: VMod, subForm: VMod) = div(selectForm, subForm)
}
object FormConfig {
  val default: FormConfig = new FormConfig {}
}
