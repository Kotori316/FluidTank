package com.kotori316.fluidtank.fluids

import cats.{Hash, Show}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.{Fluid, Fluids}
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.registries.ForgeRegistries

case class FluidKey(fluid: Fluid, tag: Option[CompoundTag]) {
  def toAmount(amount: Long): FluidAmount = FluidAmount(fluid, amount, tag)

  def createStack(amount: Int): FluidStack = new FluidStack(fluid, amount, tag.orNull)

  def isEmpty: Boolean = fluid == Fluids.EMPTY

  def isDefined: Boolean = !isEmpty
}

object FluidKey {
  def apply(fluid: Fluid, tag: Option[CompoundTag]): FluidKey = new FluidKey(fluid, tag.map(_.copy()))

  def from(fluidAmount: FluidAmount): FluidKey = FluidKey(fluidAmount.fluid, fluidAmount.nbt)

  def from(stack: FluidStack): FluidKey = FluidKey(stack.getRawFluid, Option(stack.getTag))

  implicit val FluidKeyHash: Hash[FluidKey] = Hash.fromUniversalHashCode
  implicit val FluidKeyShow: Show[FluidKey] = key =>
    key.tag match {
      case Some(tag) => f"FluidKey(${ForgeRegistries.FLUIDS.getKey(key.fluid)}, $tag)"
      case None => f"FluidKey(${ForgeRegistries.FLUIDS.getKey(key.fluid)})"
    }
}
