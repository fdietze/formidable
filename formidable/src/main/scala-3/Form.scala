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
import magnolia1.*

// TODO: recursive case classes
// https://github.com/softwaremill/magnolia#limitations

trait FormDerivation extends AutoDerivation[Form] {
  def apply[A](implicit instance: Form[A]): Form[A] = instance
  def state[A](implicit instance: Form[A]): Var[A]  = Var(instance.default)

  def join[T](ctx: CaseClass[Typeclass, T]): Form[T] = new Form[T] {
    def default: T = ctx.construct(param => param.default.getOrElse(param.typeclass.default))

    override def apply(state: Var[T], config: FormConfig)(using Owner): VModifier = {
      val subStates:Var[Seq[Any]] = state.imap[Seq[Any]](seq => ctx.rawConstruct(seq))(_.asInstanceOf[Product].productIterator.toList)

      subStates.sequence.map { subStates =>
        config.labeledFormGroup(
        ctx.params
            .zip(subStates)
            .map { case (param, subState) =>
              val subForm = (param.typeclass.apply _).asInstanceOf[((Var[Any], FormConfig) => VModifier)]
              param.label -> subForm(subState, config)
            }
        )
      }
    }
  }


  override def split[T](ctx: SealedTrait[Form, T]):Form[T] = new Form[T] {
    override def default: T = ctx.subtypes.head.typeclass.default
    override def apply(state: Var[T], config: FormConfig)(using Owner): VModifier = {
      val labelToSubtype =
        ctx.subtypes.view.map { sub => sub.typeInfo.short -> sub }.toMap

      div(
        select(
          ctx.subtypes.map { subtype =>
            option(
              subtype.typeInfo.short,
              selected <-- state.map(value => ctx.choose(value)(_ == subtype)),
            )
          }.toSeq,
          onChange.value.map(label => labelToSubtype(label).typeclass.default) --> state,
        ),
        state.map { value =>
          ctx.choose(value)(sub => sub.typeclass.asInstanceOf[Form[T]](state, config))
        },
      )
    }

  }
}
