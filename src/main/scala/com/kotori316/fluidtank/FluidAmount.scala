package com.kotori316.fluidtank

import cats.Show
import net.minecraft.fluid.{Fluid, Fluids}
import net.minecraft.item.{BucketItem, ItemStack, Items}
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import net.minecraftforge.registries.ForgeRegistries

import scala.util.Try

case class FluidAmount(fluid: Fluid, amount: Long) {
  def setAmount(newAmount: Long) = FluidAmount(fluid, newAmount)

  def write(tag: CompoundNBT): CompoundNBT = {
    tag.putString(FluidAmount.NBT_fluid, FluidAmount.registry.getKey(fluid).toString)
    tag.putLong(FluidAmount.NBT_amount, amount)
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

  def fluidEqual(that: FluidAmount) = this.fluid == that.fluid
}

object FluidAmount {
  final val NBT_fluid = "fluid"
  final val NBT_amount = "amount"
  final val AMOUNT_BUCKET = 1000
  val EMPTY = FluidAmount(Fluids.EMPTY, 0)
  val BUCKET_LAVA = FluidAmount(Fluids.LAVA, AMOUNT_BUCKET)
  val BUCKET_WATER = FluidAmount(Fluids.WATER, AMOUNT_BUCKET)
  private[this] final val bucket_fluid_field: BucketItem => Fluid =
    item => ObfuscationReflectionHelper.getPrivateValue(classOf[BucketItem], item, "field_77876_a"):Fluid

  def fromItem(stack: ItemStack): FluidAmount = {
    stack.getItem match {
      case Items.LAVA_BUCKET => BUCKET_LAVA
      case Items.WATER_BUCKET => BUCKET_WATER
      case bucket: BucketItem=>
        Try(bucket_fluid_field(bucket)).map(FluidAmount(_, AMOUNT_BUCKET)).getOrElse(EMPTY)
      case _ => EMPTY
    }
  }

  def fromNBT(tag: CompoundNBT): FluidAmount = {
    val fluid = registry.getValue(new ResourceLocation(tag.getString(NBT_fluid)))
    if (fluid == EMPTY.fluid) {
      EMPTY
    } else {
      val amount = tag.getLong(NBT_amount)
      FluidAmount(fluid, amount)
    }
  }

  def registry = ForgeRegistries.FLUIDS

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

  implicit val showFA: Show[FluidAmount] = fa => registry.getKey(fa.fluid).getPath + "@" + fa.amount + "mB"
}
