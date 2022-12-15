import colibri.reactive._
import outwatch._

// TODO: List, Vector instead of only Seq

package object formidable {

  implicit val stringForm: Form[String] = new Form[String] {
    def default = ""
    def render(state: Var[String], config: FormConfig) = Owned {
      config.textInput(state)
    }
  }

  implicit val intForm: Form[Int] = new Form[Int] {
    def default = 0
    def render(state: Var[Int], config: FormConfig) = Owned {
      encodedTextInput[Int](
        state,
        encode = _.toString,
        decode = str => str.toIntOption.toRight(s"'$str' could not be parsed as Int"),
        config,
      )
    }
  }

  implicit val longForm: Form[Long] = new Form[Long] {
    def default = 0
    def render(state: Var[Long], config: FormConfig) = Owned {
      encodedTextInput[Long](
        state,
        encode = _.toString,
        decode = str => str.toLongOption.toRight(s"'$str' could not be parsed as Long"),
        config,
      )
    }
  }

  implicit val doubleForm: Form[Double] = new Form[Double] {
    def default = 0.0
    def render(state: Var[Double], config: FormConfig) = Owned {
      encodedTextInput[Double](
        state,
        encode = _.toString,
        decode = str => str.toDoubleOption.toRight(s"'$str' could not be parsed as Double"),
        config,
      )
    }
  }

  implicit val booleanForm: Form[Boolean] = new Form[Boolean] {
    def default = false
    def render(state: Var[Boolean], config: FormConfig) = Owned {
      config.checkbox(state)
    }
  }

  implicit def optionForm[T: Form]: Form[Option[T]] = new Form[Option[T]] {
    def default = None
    def render(state: Var[Option[T]], config: FormConfig) = Owned {
      val checkboxState = state.transformVar[Boolean](_.contramap {
        case true  => Some(Form[T].default)
        case false => None
      })(_.map(_.isDefined))

      config.withCheckbox(
        subForm = state.sequence.map(
          _.map { innerState =>
            Form[T].render(innerState, config)
          }
        ),
        checkbox = config.checkbox(checkboxState),
      )
    }
  }

  implicit def seqForm[T: Form]: Form[Seq[T]] = new Form[Seq[T]] {
    def default = Seq.empty
    def render(state: Var[Seq[T]], config: FormConfig) = Owned {
      state.sequence.map(seq =>
        config.formSequence(
          seq.zipWithIndex.map { case (innerState, i) =>
            config.withRemoveButton(
              subForm = Form[T].render(innerState, config),
              removeButton = config.removeButton(() => state.update(_.patch(i, Nil, 1))),
            )
          },
          addButton = config.addButton(() => state.update(_ :+ Form[T].default)),
        )
      ): VModifier
    }
  }

  implicit def vectorForm[T](implicit seqForm: Form[Seq[T]]): Form[Vector[T]] = seqForm.imap(_.toVector)(_.toSeq)
  implicit def listForm[T](implicit seqForm: Form[Seq[T]]): Form[List[T]]     = seqForm.imap(_.toList)(_.toSeq)

  private def encodedTextInput[T](
    state: Var[T],
    encode: T => String,
    decode: String => Either[String, T],
    config: FormConfig,
  )(implicit owner: Owner): VModifier = {
    val fieldState                             = Var(encode(state.now()))
    val validationMessage: Var[Option[String]] = Var(None)

    state.foreach { value => fieldState.set(encode(value)) }
    fieldState.map(decode).foreach {
      case Left(msg) =>
        validationMessage.set(Some(msg))
      case Right(value) =>
        validationMessage.set(None)
        state.set(value)
    }

    config.textInput(fieldState, validationMessage = validationMessage)
  }
}
