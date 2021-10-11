package com.kotori316.fluidtank.fluids

import cats.Eq
import net.minecraft.world.level.material.Fluid

case class Tank(fluidAmount: FluidAmount, capacity: Long) {
  def fluid: Fluid = fluidAmount.fluid

  def amount: Long = fluidAmount.amount

  def isEmpty: Boolean = fluidAmount.isEmpty
}

object Tank {
  final val EMPTY: Tank = Tank(FluidAmount.EMPTY, 0L)
  implicit final val eqTank: Eq[Tank] = Eq.and(Eq.by(_.fluidAmount), Eq.by(_.capacity))
}
