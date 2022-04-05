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
  def default                        = ""
  def form(subject: Subject[String]) =
    inputField(subject, inputTpe = "text", parse = str => str, toValue = value => value)
}

given Form[Int] with {
  def default                     = 0
  def form(subject: Subject[Int]) =
    inputField(
      subject,
      inputTpe = "number",
      parse = { case str if str.toIntOption.isDefined => str.toInt },
      toValue = value => value.toString,
    )
}

given Form[Boolean] with {
  def default                         = false
  def form(subject: Subject[Boolean]) =
    input(
      tpe := "checkbox",
      checked <-- subject,
      onClick.checked --> subject,
    ),
}

given optionForm[T: Form]: Form[Option[T]] with {
  def default                           = None
  def form(subject: Subject[Option[T]]) = {
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
      ),
      seqSubject.sequence.map(
        _.map(innerSub =>
          VDomModifier(summon[Form[T]].form(innerSub), managedFunction(() => innerSub.foreach(innerBackup = _))),
        ),
      ),
    )
  }
}

given seqForm[T: Form]: Form[Seq[T]] with {
  def default                        = Seq.empty
  def form(subject: Subject[Seq[T]]) = {
    div(
      subject.sequence.map(
        _.zipWithIndex.map { case (innerSub, i) =>
          div(
            summon[Form[T]].form(innerSub),
            button(
              "remove",
              onClick.stopPropagation(subject).foreach { nowValue =>
                subject.onNext(nowValue.patch(i, Nil, 1))
              },
            ),
          )
        },
      ),
      button(
        "add",
        onClick(subject).foreach { nowValue =>
          subject.onNext(nowValue :+ summon[Form[T]].default)
        },
      ),
    )
  }
}

given mapForm[A: Form, B: Form]: Form[Map[A, B]] with {
  def default                           = Map.empty
  def form(subject: Subject[Map[A, B]]) = {
    val seqSubject = subject
      .imapSubject[Seq[(A, B)]](_.toMap)(_.toSeq)
    div(
      seqSubject.sequence
        .map(
          _.zipWithIndex.map { case (innerSub, i) =>
            val keySub: Subject[A]   = innerSub.lens[A](_._1)((kv, newK) => (newK, kv._2))
            val valueSub: Subject[B] = innerSub.lens[B](_._2)((kv, newV) => (kv._1, newV))
            div(
              display.flex,
              summon[Form[A]].form(keySub),
              "->",
              summon[Form[B]].form(valueSub),
              button(
                "remove",
                onClick.stopPropagation(seqSubject).foreach { nowValue =>
                  seqSubject.onNext(nowValue.patch(i, Nil, 1))
                },
              ),
            )
          },
        ),
      button(
        "add",
        onClick(seqSubject).foreach { nowValue =>
          seqSubject.onNext(nowValue :+ summon[Form[(A, B)]].default)
        },
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
