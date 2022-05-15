package formidable

import outwatch._
import outwatch.dsl._
import colibri._
import org.scalajs.dom.console
import org.scalajs.dom.HTMLInputElement

import scala.deriving.Mirror
import scala.deriving._
import scala.compiletime.{constValueTuple, erasedValue, summonInline}

// TODO: List instead of only Seq

given Form[String] with {
  def default                                                       = ""
  def apply(subject: Subject[String], formModifiers: FormModifiers) =
    inputField(subject, inputTpe = "text", parse = str => str, toValue = value => value)(formModifiers.inputModifiers)
}

given Form[Int] with {
  def default                                                    = 0
  def apply(subject: Subject[Int], formModifiers: FormModifiers) =
    inputField(
      subject,
      inputTpe = "number",
      parse = { case str if str.toIntOption.isDefined => str.toInt },
      toValue = value => value.toString,
    )
}

given Form[Boolean] with {
  def default                                                        = false
  def apply(subject: Subject[Boolean], formModifiers: FormModifiers) =
    input(
      tpe := "checkbox",
      checked <-- subject,
      onClick.checked --> subject,
      formModifiers.checkboxModifiers,
    ),
}

given optionForm[T: Form]: Form[Option[T]] with {
  def default                                                          = None
  def apply(subject: Subject[Option[T]], formModifiers: FormModifiers) = {
    val seqSubject: Subject[Seq[T]] = subject.imapSubject[Seq[T]](_.headOption)(_.toSeq)
    var innerBackup: T              = summon[Form[T]].default

    div(
      display.flex,
      alignItems.center,
      input(
        tpe := "checkbox",
        checked <-- subject.map(_.isDefined),
        onClick.checked.map {
          case true  => Some(innerBackup)
          case false => None
        } --> subject,
        formModifiers.checkboxModifiers,
      ),
      seqSubject.sequence.map(
        _.map(innerSub =>
          VDomModifier(Form[T](innerSub, formModifiers), managedFunction(() => innerSub.foreach(innerBackup = _))),
        ),
      ),
    )
  }
}

given seqForm[T: Form]: Form[Seq[T]] with {
  def default                                                       = Seq.empty
  def apply(subject: Subject[Seq[T]], formModifiers: FormModifiers) = {
    div(
      subject.sequence.map(
        _.zipWithIndex.map { case (innerSub, i) =>
          div(
            Form[T](innerSub, formModifiers),
            button(
              "remove",
              onClick.stopPropagation(subject).foreach { nowValue =>
                subject.onNext(nowValue.patch(i, Nil, 1))
              },
              formModifiers.buttonModifiers,
            ),
          )
        },
      ),
      button(
        "add",
        onClick(subject).foreach { nowValue =>
          subject.onNext(nowValue :+ Form[T].default)
        },
        formModifiers.buttonModifiers,
      ),
    )
  }
}

private def inputField[T](
  subject: Subject[T],
  inputTpe: String,
  parse: PartialFunction[String, T],
  toValue: T => String,
) = input(
  tpe := inputTpe,
  value <-- subject.map(toValue),
  onInput.value.collect(parse) --> subject,
)
