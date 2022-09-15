package com.kotori316.fluidtank.fluids

import cats.Eq
import net.minecraft.world.level.material.Fluid

case class Tank private(fluidAmount: FluidAmount, capacity: FabricAmount, dummy: Null) {
  def fluid: Fluid = fluidAmount.fluid

  def amount: FabricAmount = fluidAmount.fabricAmount

  def isEmpty: Boolean = fluidAmount.isEmpty

  def capacityInForge: Long = capacity.toForge

  def amountInForge: Long = fluidAmount.amount
}

object Tank {
  def apply(fluidAmount: FluidAmount, capacity: Long): Tank = new Tank(fluidAmount, FabricAmount.fromForge(capacity), null)

  final val EMPTY: Tank = Tank(FluidAmount.EMPTY, 0L)
  implicit final val eqTank: Eq[Tank] = Eq.and(Eq.by(_.fluidAmount), Eq.by(_.capacity))
}
