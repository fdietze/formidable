package formidable

import outwatch._
import colibri.reactive._

import magnolia1._

trait FormDerivation {
  def apply[A](implicit instance: Form[A]): Form[A] = instance
  def state[A](implicit instance: Form[A]): Var[A]  = Var(instance.default)

  type Typeclass[T] = Form[T]

  def join[T](ctx: CaseClass[Typeclass, T]): Form[T] = new Form[T] {
    override def default: T = ctx.construct(param => param.default.getOrElse(param.typeclass.default))

    override def render(state: Var[T], config: FormConfig): VModifier = Owned.function { implicit owner =>
      val subStates: Var[Seq[Any]] =
        state.imap[Seq[Any]](seq => ctx.rawConstruct(seq))(_.asInstanceOf[Product].productIterator.toList)

      subStates.sequence.map { subStates =>
        config.labeledFormGroup(
          ctx.parameters
            .zip(subStates)
            .map { case (param, subState) =>
              val subForm = ((s: Var[param.PType], c) => param.typeclass.render(s, c))
                .asInstanceOf[(Var[Any], FormConfig) => VModifier]
              val label = param.annotations.collectFirst { case Label(l) => l }.getOrElse(param.label + ":")
              label -> subForm(subState, config)
            }
        )
      }: VModifier
    }
  }

  def split[T](ctx: SealedTrait[Form, T]): Form[T] = new Form[T] {
    override def default: T = {
      val defaultSubtype = ctx.subtypes.find(_.annotations.exists(_.isInstanceOf[Default])).getOrElse(ctx.subtypes.head)
      defaultSubtype.typeclass.default
    }
    override def render(selectedValue: Var[T], config: FormConfig): VModifier = Owned.function { implicit owner =>
      val selectedSubtype: Var[Subtype[Form, T]] =
        selectedValue.imap[Subtype[Form, T]](subType => subType.typeclass.default)(value => ctx.split(value)(identity))

      config.unionSubform(
        config.selectInput[Subtype[Form, T]](
          options = ctx.subtypes,
          selectedValue = selectedSubtype,
          show = subtype => subtype.typeName.short,
        ),
        subForm = selectedValue.map { value =>
          ctx.split(value) { sub =>
            VModifier.when(value.isInstanceOf[T])(sub.typeclass.asInstanceOf[Form[T]].render(selectedValue, config))
          }
        },
      )
    }

  }

  implicit def gen[T]: Form[T] = macro Magnolia.gen[T]
}
