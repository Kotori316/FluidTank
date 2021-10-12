package com.kotori316.fluidtank

import cats.data.OptionT
import cats.{Eval, Now}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional

import scala.language.implicitConversions

trait CapConverter {

  type Cap[T] = OptionT[Eval, T]

  object Cap {
    def make[T](obj: T): Cap[T] = {
      OptionT.fromOption[Eval](Option(obj))
    }

    def empty[A]: Cap[A] = {
      OptionT.none
    }
  }

  implicit def toAsScalaLO[T](value: LazyOptional[T]): CapConverter.AsScalaLO[T] = new CapConverter.AsScalaLO(value)
}

object CapConverter extends CapConverter {

  def transform0[T](cap: LazyOptional[T]): Eval[Option[T]] = {
    if (cap.isPresent) {
      Eval.always {
        if (cap.isPresent) {
          Option(cap.orElse(null.asInstanceOf[T]))
        } else {
          None
        }
      }
    } else {
      Eval.now(Option.empty[T])
    }
  }

  implicit class AsScalaLO[T](private val cap: LazyOptional[T]) extends AnyVal {
    def asScala: Cap[T] = OptionT(transform0[T](cap))
  }

}