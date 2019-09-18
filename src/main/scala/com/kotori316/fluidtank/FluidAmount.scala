package com.kotori316.fluidtank

import cats._
import cats.implicits._
import javax.annotation.Nonnull
import net.minecraft.fluid.{Fluid, Fluids}
import net.minecraft.item.{BucketItem, ItemStack, Items}
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.{FluidAttributes, FluidStack, FluidUtil}
import net.minecraftforge.registries.ForgeRegistries

case class FluidAmount(@Nonnull fluid: Fluid, amount: Long, @Nonnull nbt: Option[CompoundNBT]) {
  def setAmount(newAmount: Long) = FluidAmount(fluid, newAmount, nbt)

  def write(tag: CompoundNBT): CompoundNBT = {
    tag.putString(FluidAmount.NBT_fluid, FluidAmount.registry.getKey(fluid).toString)
    tag.putLong(FluidAmount.NBT_amount, amount)
    nbt.foreach(n => tag.put(FluidAmount.NBT_tag, n))
    tag
  }

  def nonEmpty = fluid != Fluids.EMPTY && amount > 0

  def isEmpty = !nonEmpty

  def isGaseous = fluid.getAttributes.isGaseous

  def getLocalizedName: String = String.valueOf(FluidAmount.registry.getKey(fluid))

  def +(that: FluidAmount): FluidAmount = {
    if (fluid == Fluids.EMPTY) that
    else setAmount(this.amount + that.amount)
  }

  def -(that: FluidAmount): FluidAmount = setAmount(this.amount - that.amount)

  def fluidEqual(that: FluidAmount) = this.fluid === that.fluid && this.nbt === that.nbt

  def toStack: FluidStack = if (this == FluidAmount.EMPTY) FluidStack.EMPTY else new FluidStack(fluid, Utils.toInt(amount))

  override def toString = FluidAmount.registry.getKey(fluid).getPath + "@" + amount + "mB" + nbt.fold("")(" " + _.toString)
}

object FluidAmount {
  final val NBT_fluid = "fluid"
  final val NBT_amount = "amount"
  final val NBT_tag = "tag"
  final val AMOUNT_BUCKET = FluidAttributes.BUCKET_VOLUME
  val EMPTY = FluidAmount(Fluids.EMPTY, 0, None)
  val BUCKET_LAVA = FluidAmount(Fluids.LAVA, AMOUNT_BUCKET, None)
  val BUCKET_WATER = FluidAmount(Fluids.WATER, AMOUNT_BUCKET, None)

  def fromItem(stack: ItemStack): FluidAmount = {
    stack.getItem match {
      case Items.LAVA_BUCKET => BUCKET_LAVA
      case Items.WATER_BUCKET => BUCKET_WATER
      case bucket: BucketItem =>
        bucket.pure[Id].map(_.getFluid).map(FluidAmount(_, AMOUNT_BUCKET, None))
      case _ => FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).pure[Id].map(fromStack)
    }
  }

  def fromNBT(tag: CompoundNBT): FluidAmount = {
    val fluid = registry.getValue(new ResourceLocation(Option(tag).map(_.getString(NBT_fluid)).getOrElse(Fluids.EMPTY.getRegistryName.toString)))
    if (fluid == null || fluid == EMPTY.fluid) {
      EMPTY
    } else {
      val amount = tag.getLong(NBT_amount)
      val nbt = if (tag.contains(NBT_tag, NBT.TAG_COMPOUND)) Option(tag.getCompound(NBT_tag)).filterNot(_.isEmpty) else None
      FluidAmount(fluid, amount, nbt)
    }
  }

  def fromStack(stack: FluidStack): FluidAmount = {
    val fluid = stack.getRawFluid
    if (fluid == null || fluid == Fluids.EMPTY) {
      FluidAmount.EMPTY
    } else {
      FluidAmount(fluid, stack.getAmount, Option(stack.getTag))
    }
  }

  def registry = ForgeRegistries.FLUIDS

  trait Tank extends IFluidHandler {
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

    override def getTanks = 1

    override def isFluidValid(tank: Int, stack: FluidStack): Boolean = true

    override def fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int = Utils.toInt(fill(FluidAmount.fromStack(resource), action.execute()).amount)

    override def drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack = drain(fromStack(resource), action.execute()).toStack

    override def drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack = drain(fromStack(getFluidInTank(0)).setAmount(maxDrain), action.execute()).toStack
  }

  implicit val showFA: Show[FluidAmount] = Show.fromToString
}
