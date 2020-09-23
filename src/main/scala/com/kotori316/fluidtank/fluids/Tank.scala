package com.kotori316.fluidtank.fluids

import net.minecraft.fluid.Fluid

case class Tank(fluidAmount: FluidAmount, capacity: Long) {
  def fluid: Fluid = fluidAmount.fluid

  def amount: Long = fluidAmount.amount
}

object Tank {
  final val EMPTY: Tank = Tank(FluidAmount.EMPTY, 0L)
}
