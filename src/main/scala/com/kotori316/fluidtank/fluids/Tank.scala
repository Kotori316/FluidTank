package com.kotori316.fluidtank.fluids

import cats.implicits.catsSyntaxEq
import cats.kernel.Eq
import net.minecraft.fluid.Fluid

case class Tank(fluidAmount: FluidAmount, capacity: Long) {
  def fluid: Fluid = fluidAmount.fluid

  def amount: Long = fluidAmount.amount

  def isEmpty: Boolean = fluidAmount.isEmpty
}

object Tank {
  final val EMPTY: Tank = Tank(FluidAmount.EMPTY, 0L)
  implicit final val eqTank: Eq[Tank] = Eq.instance((a, b) => a.fluidAmount === b.fluidAmount && a.capacity === b.capacity)
}
