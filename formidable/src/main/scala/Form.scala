package formidable

import colibri.reactive._
import outwatch._

// used to mark default cases of sealed traits
case class Default() extends scala.annotation.StaticAnnotation

// used to customize labels for fields and types
case class Label(label: String) extends scala.annotation.StaticAnnotation

trait Form[T] {
  def default: T
  def render(
    state: Var[T] = Var(default),
    config: FormConfig = FormConfig.default,
  ): VMod

  def imap[S](f: T => S)(g: S => T): Form[S] = new Form[S] {
    override def default: S = f(Form.this.default)
    override def render(
      state: Var[S],
      config: FormConfig,
    ): VMod = Form.this.render(state.imap(f)(g), config)
  }
}

object Form extends FormDerivation
