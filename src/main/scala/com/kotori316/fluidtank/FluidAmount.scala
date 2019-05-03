package com.kotori316.fluidtank

import net.minecraft.fluid.Fluid
import net.minecraft.init.{Fluids, Items}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraft.util.registry.IRegistry

case class FluidAmount(fluid: Fluid, amount: Int) {
  def setAmount(newAmount: Int) = FluidAmount(fluid, newAmount)

  def write(tag: NBTTagCompound): NBTTagCompound = {
    tag.setString(FluidAmount.NBT_fluid, FluidAmount.registry.getKey(fluid).toString)
    tag.setInt(FluidAmount.NBT_amount, amount)
    tag
  }

  def nonEmpty = fluid != Fluids.EMPTY && amount > 0

  def isEmpty = !nonEmpty

  def isGaseous(dummy: FluidAmount) = false

  def getLocalizedName: String = FluidAmount.registry.getKey(fluid).toString

  def +(that: FluidAmount): FluidAmount = {
    if (fluid == Fluids.EMPTY) that
    else setAmount(this.amount + that.amount)
  }

  def -(that: FluidAmount): FluidAmount = setAmount(this.amount - that.amount)

  def fluidEqual(that:FluidAmount) = this.fluid == that.fluid
}

object FluidAmount {
  final val NBT_fluid = "fluid"
  final val NBT_amount = "amount"
  final val AMOUNT_BUCKET = 1000
  val EMPTY = FluidAmount(Fluids.EMPTY, 0)
  val BUCKET_LAVA = FluidAmount(Fluids.LAVA, AMOUNT_BUCKET)
  val BUCKET_WATER = FluidAmount(Fluids.WATER, AMOUNT_BUCKET)

  def fromItem(stack: ItemStack): FluidAmount = {
    stack.getItem match {
      case Items.LAVA_BUCKET => BUCKET_LAVA
      case Items.WATER_BUCKET => BUCKET_WATER
      case _ => EMPTY
    }
  }

  def fromNBT(tag: NBTTagCompound): FluidAmount = {
    val fluid = registry.get(new ResourceLocation(tag.getString(NBT_fluid)))
    if (fluid == EMPTY.fluid) {
      EMPTY
    } else {
      val amount = tag.getInt(NBT_amount)
      FluidAmount(fluid, amount)
    }
  }

  def registry = IRegistry.field_212619_h

  trait Tank {
    /**
      * @return Fluid that was accepted by the tank.
      */
    def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Int = 0): FluidAmount

    /**
      * @param fluidAmount the fluid representing the kind and maximum amount to drain.
      *                    Empty Fluid means fluid type can be anything.
      * @param doDrain     false means simulating.
      * @param min         minimum amount to drain.
      * @return the fluid and amount that is (or will be) drained.
      */
    def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Int = 0): FluidAmount
  }

}
