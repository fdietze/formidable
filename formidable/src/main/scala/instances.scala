import colibri.reactive._
import outwatch._

// TODO: List, Vector instead of only Seq

package object formidable {

  implicit val stringForm: Form[String] = new Form[String] {
    def default = ""
    def render(state: Var[String], config: FormConfig) =
      config.textInput(state)
  }

  implicit val intForm: Form[Int] = new Form[Int] {
    def default = 0
    def render(state: Var[Int], config: FormConfig) =
      encodedTextInput[Int](
        state,
        encode = _.toString,
        decode = str => str.toIntOption.toRight(s"'$str' could not be parsed as Int"),
        config,
      )
  }

  implicit val longForm: Form[Long] = new Form[Long] {
    def default = 0
    def render(state: Var[Long], config: FormConfig) =
      encodedTextInput[Long](
        state,
        encode = _.toString,
        decode = str => str.toLongOption.toRight(s"'$str' could not be parsed as Long"),
        config,
      )
  }

  implicit val doubleForm: Form[Double] = new Form[Double] {
    def default = 0.0
    def render(state: Var[Double], config: FormConfig) =
      encodedTextInput[Double](
        state,
        encode = _.toString,
        decode = str => str.toDoubleOption.toRight(s"'$str' could not be parsed as Double"),
        config,
      )
  }

  implicit val booleanForm: Form[Boolean] = new Form[Boolean] {
    def default = false
    def render(state: Var[Boolean], config: FormConfig) =
      config.checkbox(state)
  }

  implicit def optionForm[T: Form]: Form[Option[T]] = new Form[Option[T]] {
    def default = None
    def render(state: Var[Option[T]], config: FormConfig) = {
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
    def render(state: Var[Seq[T]], config: FormConfig) =
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
      )
  }

  implicit def vectorForm[T](implicit seqForm: Form[Seq[T]]): Form[Vector[T]] = seqForm.imap(_.toVector)(_.toSeq)
  implicit def listForm[T](implicit seqForm: Form[Seq[T]]): Form[List[T]]     = seqForm.imap(_.toList)(_.toSeq)

  private def encodedTextInput[T](
    state: Var[T],
    encode: T => String,
    decode: String => Either[String, T],
    config: FormConfig,
  ): VModifier = {
    val validatedFieldState: Var[(String, Either[String, T])] = Var.createStateful(
      state.contramapIterable { case (_, decoded) => decoded.toOption },
      state.map(t => (encode(t), Right(t))),
    )

    val fieldState        = validatedFieldState.imap[String](str => (str, decode(str)))(_._1)
    val validationMessage = validatedFieldState.map(_._2.left.toOption)

    config.textInput(fieldState, validationMessage = validationMessage)
  }
}
