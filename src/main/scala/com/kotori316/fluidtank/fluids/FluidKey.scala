package com.kotori316.fluidtank.fluids

import cats.{Hash, Show}
import net.minecraft.fluid.{Fluid, Fluids}
import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.fluids.FluidStack

case class FluidKey(fluid: Fluid, tag: Option[CompoundNBT]) {
  def toAmount(amount: Long): FluidAmount = FluidAmount(fluid, amount, tag)

  def isEmpty: Boolean = fluid == Fluids.EMPTY

  def isDefined: Boolean = !isEmpty
}

object FluidKey {
  def from(fluidAmount: FluidAmount): FluidKey = FluidKey(fluidAmount.fluid, fluidAmount.nbt)

  def from(stack: FluidStack): FluidKey = FluidKey(stack.getRawFluid, Option(stack.getTag))

  implicit val FluidKeyHash: Hash[FluidKey] = Hash.fromUniversalHashCode
  implicit val FluidKeyShow: Show[FluidKey] = key =>
    key.tag match {
      case Some(tag) => f"FluidKey(${key.fluid.getRegistryName}, $tag)"
      case None => f"FluidKey(${key.fluid.getRegistryName})"
    }
}
