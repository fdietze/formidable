package formidable

import outwatch._
import colibri.reactive._
import scala.collection.mutable

import magnolia1._

trait FormDerivation {
  def apply[A](implicit instance: Form[A]): Form[A] = instance
  def state[A](implicit instance: Form[A]): Var[A]  = Var(instance.default)

  type Typeclass[T] = Form[T]

  def join[T](ctx: CaseClass[Typeclass, T]): Form[T] = new Form[T] {
    override def default: T = ctx.construct(param => param.default.getOrElse(param.typeclass.default))

    override def render(state: Var[T], config: FormConfig): VModifier = Owned.function { implicit owner =>
      println(s"product[${ctx.typeName.short}]: rendering")
      val combinedFieldState: Var[Seq[Any]] =
        state.imap[Seq[Any]](seq => ctx.rawConstruct(seq))(_.asInstanceOf[Product].productIterator.toList)

      combinedFieldState.sequence.map { fieldStates =>
        println(
          s"product[${ctx.typeName.short}]: state changed: ${ctx.parameters
              .map(_.label)
              .zip(fieldStates.map(_.now()))
              .map { case (label, value) =>
                s"$label: $value"
              }
              .mkString(",")}"
        )
        config.labeledFormGroup(
          ctx.parameters
            .zip(fieldStates)
            .map { case (param, subState) =>
              val subForm = ((s: Var[param.PType], c) => param.typeclass.render(s, c))
                .asInstanceOf[(Var[Any], FormConfig) => VModifier]
              param.label -> subForm(subState, config)
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
      println(s"sum[${ctx.typeName.short}]: rendering")

      val valueBackup = mutable.HashMap.empty[Subtype[Form, T], T].withDefault(_.typeclass.default)
      val selectedSubtype: Var[Subtype[Form, T]] =
        selectedValue.imap[Subtype[Form, T]](subtype => valueBackup(subtype)) { value =>
          val subtype = ctx.split(value)(identity)
          valueBackup(subtype) = value
          subtype
        }

      val subFormBackup = mutable.HashMap.empty[Subtype[Form, T], (Var[T], VModifier)]

      config.unionSubform(
        config.selectInput[Subtype[Form, T]](
          options = ctx.subtypes,
          selectedValue = selectedSubtype,
          show = subtype => subtype.typeName.short,
        ),
        subForm = selectedValue.map { newValue =>
          println(s"sum[${ctx.typeName.short}]: state changed: $newValue")
          ctx.split(newValue) { subtype =>
            val (formState, form) = subFormBackup.getOrElseUpdate(
              key = subtype,
              defaultValue = {
                val formInstance = subtype.typeclass.asInstanceOf[Form[T]]
                val state        = Var(formInstance.default)
                val form         = formInstance.render(state, config) // TODO: this is lazy and always re-rendered....
                (state, form)
              },
            )

            println(subFormBackup.size)

            formState.set(newValue)

            VModifier(
              VModifier.managedEval(formState.observable.unsafeForeach(selectedValue.set)),
              form,
            )
          }
        },
      )
    }

  }

  implicit def gen[T]: Form[T] = macro Magnolia.gen[T]
}
