package formidable

import colibri.reactive._
import outwatch._

case class Default() extends scala.annotation.StaticAnnotation

trait Form[T] {
  def apply(
    state: Var[T],
    config: FormConfig = FormConfig.default,
  )(implicit owner: Owner): VModifier
  def default: T
}

object Form extends FormDerivation
