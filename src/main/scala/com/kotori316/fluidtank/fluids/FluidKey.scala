package com.kotori316.fluidtank.fluids

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.{Fluid, Fluids}

case class FluidKey(fluid: Fluid, tag: Option[CompoundTag]) {
  def toAmount(amount: FabricAmount): FluidAmount = FluidAmount(fluid, amount, tag)

  def isEmpty: Boolean = fluid == Fluids.EMPTY

  def isDefined: Boolean = !isEmpty
}

object FluidKey {
  def apply(fluid: Fluid, tag: Option[CompoundTag]): FluidKey = new FluidKey(fluid, tag.map(_.copy()))

  def from(fluidAmount: FluidAmount): FluidKey = FluidKey(fluidAmount.fluid, fluidAmount.nbt)
}
