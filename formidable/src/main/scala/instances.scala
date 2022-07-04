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

// TODO: List, Vector instead of only Seq

given Form[String] with {
  def default                                                    = ""
  def apply(state: Var[String], config: FormConfig)(using Owner) = config.textInput(state)
}

given Form[Int] with {
  def default                                                 = 0
  def apply(state: Var[Int], config: FormConfig)(using Owner) =
    encodedTextInput[Int](
      state,
      encode = _.toString,
      decode = str =>
        str.toIntOption match {
          case Some(int) => Right(int)
          case None      => Left(s"'$str' could not be parsed as Int")
        },
      config,
    )
}

given Form[Boolean] with {
  def default                                                     = false
  def apply(state: Var[Boolean], config: FormConfig)(using Owner) = config.checkbox(state)
}

given optionForm[T: Form]: Form[Option[T]] with {
  def default                                                       = None
  def apply(state: Var[Option[T]], config: FormConfig)(using Owner) = {
    var innerBackup: T = summon[Form[T]].default

    val checkboxState = state.transformVar[Boolean](_.contramap {
      case true  => Some(innerBackup)
      case false => None
    })(_.map(_.isDefined))

    config.withCheckbox(
      subForm = state.sequence.map(
        _.map { innerState =>
          innerState.foreach(innerBackup = _)
          Form[T](innerState, config),
        },
      ),
      checkbox = config.checkbox(checkboxState),
    )
  }
}

given seqForm[T: Form]: Form[Seq[T]] with {
  def default                                                    = Seq.empty
  def apply(state: Var[Seq[T]], config: FormConfig)(using Owner) = {
    config.withAddButton(
      subForm = state.sequence.map(
        _.zipWithIndex.map { case (innerState, i) =>
          config.withRemoveButton(
            subForm = Form[T](innerState, config),
            removeButton = config.removeButton(() => state.update(_.patch(i, Nil, 1))),
          )
        },
      ),
      addButton = config.addButton(() => state.update(_ :+ Form[T].default)),
    )
  }
}

private def encodedTextInput[T](
  state: Var[T],
  encode: T => String,
  decode: String => Either[String, T],
  config: FormConfig,
)(using Owner) = {
  val fieldState                             = Var(encode(state.now()))
  val validationMessage: Var[Option[String]] = Var(None)

  state.foreach { value => fieldState.set(encode(value)) }
  fieldState.map(decode).foreach {
    case Left(msg)    =>
      validationMessage.set(Some(msg))
    case Right(value) =>
      validationMessage.set(None)
      state.set(value)
  }

  config.textInput(fieldState, validationMessage)
}
