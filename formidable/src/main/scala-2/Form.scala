package formidable

import outwatch._
import outwatch.dsl._
import colibri.reactive._

import magnolia1._

// TODO: recursive case classes
// https://github.com/softwaremill/magnolia#limitations

trait FormDerivation {
  def apply[A](implicit instance: Form[A]): Form[A] = instance
  def state[A](implicit instance: Form[A]): Var[A]  = Var(instance.default)

  type Typeclass[T] = Form[T]

  def join[T](ctx: CaseClass[Typeclass, T]): Form[T] = new Form[T] {
    def default: T = ctx.construct(param => param.default.getOrElse(param.typeclass.default))

    def apply(state: Var[T], config: FormConfig): VModifier = Owned {
      val subStates: Var[Seq[Any]] =
        state.imap[Seq[Any]](seq => ctx.rawConstruct(seq))(_.asInstanceOf[Product].productIterator.toList)

      subStates.sequence.map { subStates =>
        config.labeledFormGroup(
          ctx.parameters
            .zip(subStates)
            .map { case (param, subState) =>
              val subForm = ((s: Var[param.PType], c) => param.typeclass.apply(s, c))
                .asInstanceOf[(Var[Any], FormConfig) => VModifier]
              param.label -> subForm(subState, config)
            },
        )
      }: VModifier
    }
  }

  def split[T](ctx: SealedTrait[Form, T]): Form[T] = new Form[T] {
    override def default: T = {
      val defaultSubtype = ctx.subtypes.find(_.annotations.exists(_.isInstanceOf[Default])).getOrElse(ctx.subtypes.head)
      defaultSubtype.typeclass.default
    }
    override def apply(state: Var[T], config: FormConfig): VModifier = Owned {
      val labelToSubtype =
        ctx.subtypes.view.map { sub => sub.typeName.short -> sub }.toMap

      div(
        select(
          ctx.subtypes.map { subtype =>
            option(
              subtype.typeName.short,
              selected <-- state.map(value => ctx.split(value)(_ == subtype)),
            )
          }.toSeq,
          onChange.value.map(label => labelToSubtype(label).typeclass.default) --> state,
        ),
        state.map { value =>
          ctx.split(value) { sub =>
            VModifier.ifTrue(value.isInstanceOf[T])(sub.typeclass.asInstanceOf[Form[T]](state, config))
          }
        },
      ): VModifier
    }

  }

  implicit def gen[T]: Form[T] = macro Magnolia.gen[T]
}
