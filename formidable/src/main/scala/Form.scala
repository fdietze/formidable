package formidable

import colibri.reactive._
import outwatch._

// used to mark default cases of sealed traits
case class Default() extends scala.annotation.StaticAnnotation

trait Form[T] {
  def default: T
  def render(
    state: Var[T] = Var(default),
    config: FormConfig = FormConfig.default,
  ): VModifier
}

object Form extends FormDerivation
