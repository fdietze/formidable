package formidable

import outwatch._
import colibri.reactive._

import magnolia1._

trait FormDerivation extends AutoDerivation[Form] {
  def apply[A](implicit instance: Form[A]): Form[A] = instance
  def state[A](implicit instance: Form[A]): Var[A]  = Var(instance.default)

  override def join[T](ctx: CaseClass[Typeclass, T]): Form[T] = new Form[T] {
    override def default: T = ctx.construct(param => param.default.getOrElse(param.typeclass.default))

    override def render(state: Var[T], config: FormConfig): VModifier = Owned.function { implicit owner =>
      val subStates: Var[Seq[Any]] =
        state.imap[Seq[Any]](seq => ctx.rawConstruct(seq))(_.asInstanceOf[Product].productIterator.toList)

      subStates.sequence.map { subStates =>
        config.labeledFormGroup(
          ctx.params
            .zip(subStates)
            .map { case (param, subState) =>
              val subForm = (param.typeclass.render _).asInstanceOf[(Var[Any], FormConfig) => VModifier]
              val label   = param.annotations.collectFirst { case Label(l) => l }.getOrElse(param.label + ":")
              label -> subForm(subState, config)
            }
        )
      }: VModifier
    }
  }

  override def split[T](ctx: SealedTrait[Form, T]): Form[T] = new Form[T] {
    override def default: T = {
      val defaultSubtype = ctx.subtypes.find(_.annotations.exists(_.isInstanceOf[Default])).getOrElse(ctx.subtypes.head)
      defaultSubtype.typeclass.default
    }
    override def render(selectedValue: Var[T], config: FormConfig): VModifier = Owned.function { implicit owner =>
      val selectedSubtype: Var[SealedTrait.Subtype[Form, T, _]] =
        selectedValue.imap[SealedTrait.Subtype[Form, T, _]](subType => subType.typeclass.default)(value =>
          ctx.choose(value)(_.subtype)
        )

      def labelForSubtype[Type, SType](subtype: SealedTrait.Subtype[Form, Type, SType]): String =
        subtype.annotations.collectFirst { case Label(l) => l }.getOrElse(subtype.typeInfo.short)

      config.unionSubform(
        config.selectInput[SealedTrait.Subtype[Form, T, _]](
          options = ctx.subtypes,
          selectedValue = selectedSubtype,
          show = labelForSubtype,
        ),
        selectedValue.map { value =>
          ctx.choose(value) { sub =>
            VModifier.when(value.isInstanceOf[T])(sub.typeclass.asInstanceOf[Form[T]].render(selectedValue, config))
          }
        },
      )
    }

  }
}
