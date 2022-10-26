package com.kotori316.fluidtank.fluids

import cats.Eq
import net.minecraft.world.level.material.Fluid

case class Tank[A](genericAmount: GenericAmount[A], capacity: Long) {
  def content: A = genericAmount.c

  def amount: Long = genericAmount.amount

  def isEmpty: Boolean = genericAmount.isEmpty

  @deprecated(message = "Temporary compile")
  @Deprecated(forRemoval = true)
  def fluidAmount: GenericAmount[A] = this.genericAmount
}

object Tank {
  final val EMPTY: Tank[Fluid] = Tank(FluidAmount.EMPTY, 0L)
  implicit final val eqTank: Eq[Tank[Fluid]] = Eq.and(Eq.by(_.genericAmount), Eq.by(_.capacity))
}
