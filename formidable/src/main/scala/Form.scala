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

// TODO: recursive case classes

trait Form[T] {
  def apply(
    state: Var[T],
    config: FormConfig = FormConfig.default,
  )(using Owner): VModifier
  def default: T
}

object Form {
  def apply[A](using instance: Form[A]): Form[A] = instance
  def state[A](using instance: Form[A]): Var[A]  = Var(instance.default)

  inline def summonAll[A <: Tuple]: List[Form[_]] =
    inline erasedValue[A] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => summonInline[Form[t]] :: summonAll[ts]
    }

  private def toTuple(xs: List[_], acc: Tuple): Tuple = xs match {
    case Nil      => acc
    case (h :: t) => h *: toTuple(t, acc)
  }

  inline given derived[A](using m: Mirror.Of[A])(using Owner): Form[A] = {
    lazy val instances = summonAll[m.MirroredElemTypes]
    val labels         = constValueTuple[m.MirroredElemLabels].toList.asInstanceOf[List[String]]

    // type ElemEditors = Tuple.Map[m.MirroredElemTypes, Editor]
    // val elemEditors = summonAll[ElemEditors].toList.asInstanceOf[List[Editor[Any]]]
    // val containers = labels.zip(elemEditors) map { (label, editor) => editor.container(label) }

    inline m match {
      case s: Mirror.SumOf[A]     => deriveSum(s, instances, labels)
      case p: Mirror.ProductOf[A] => deriveProduct(p, instances, labels)
    }
  }

  def deriveSum[A](s: Mirror.SumOf[A], instances: => List[Form[_]], labels: List[String]): Form[A] = {
    new Form[A] {
      def default: A =
        instances.head
          .asInstanceOf[Form[A]]
          .default

      def apply(
        state: Var[A],
        config: FormConfig,
      )(using Owner) = {
        val labelToInstance: Map[String, Form[A]] =
          instances.zip(labels).map { case (instance, label) => label -> instance.asInstanceOf[Form[A]] }.toMap

        def labelForValue(value: A): String = {
          value.getClass.getSimpleName.split('$').head
        }

        div(
          select(
            instances.zip(labels).map { case (instance, label) =>
              option(
                label,
                selected <-- state.map(x => labelForValue(x) == label),
              )
            },
            onChange.value.map(label => labelToInstance(label).default) --> state,
          ),
          state.map { value =>
            val label = labelForValue(value)
            labelToInstance(label)(state, config)
          },
        )
      }
    }
  }

  def deriveProduct[A](
    p: Mirror.ProductOf[A],
    instances: => List[Form[_]],
    labels: List[String],
  )(using Owner): Form[A] =
    new Form[A] {
      def default: A =
        p.fromProduct(
          toTuple(instances.map(_.default), EmptyTuple),
        )

      def apply(state: Var[A], config: FormConfig)(using Owner) = {
        def listToTuple[T](l: List[T]): Tuple = l match {
          case x :: rest => x *: listToTuple(rest)
          case Nil       => EmptyTuple
        }

        val x: Var[Seq[Any]] =
          state
            .imap[Seq[Any]](x => p.fromProduct(listToTuple(x.toList)))(
              _.asInstanceOf[Product].productIterator.toList,
            )

        x.sequence.map { states =>
          config.labeledFormGroup(
            instances
              .zip(states)
              .zip(labels)
              .map { case ((instance, sub), label) =>
                val f = (instance.apply _).asInstanceOf[((Var[Any], FormConfig) => VNode)]
                label -> f(sub, config)
              },
          )
        }
      }
    }

}
