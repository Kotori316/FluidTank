package com.kotori316.fluidtank

import cats.Show
import net.minecraft.fluid.{Fluid, Fluids}
import net.minecraft.item.{BucketItem, ItemStack, Items}
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.{FluidAttributes, FluidStack}
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import net.minecraftforge.registries.ForgeRegistries

import scala.util.Try

case class FluidAmount(fluid: Fluid, amount: Long, nbt: Option[CompoundNBT]) {
  def setAmount(newAmount: Long) = FluidAmount(fluid, newAmount, nbt)

  def write(tag: CompoundNBT): CompoundNBT = {
    tag.putString(FluidAmount.NBT_fluid, FluidAmount.registry.getKey(fluid).toString)
    tag.putLong(FluidAmount.NBT_amount, amount)
    tag.put(FluidAmount.NBT_tag, nbt.getOrElse(new CompoundNBT))
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

  def toStack: FluidStack = new FluidStack(fluid, Utils.toInt(amount))
}

object FluidAmount {
  final val NBT_fluid = "fluid"
  final val NBT_amount = "amount"
  final val NBT_tag = "tag"
  final val AMOUNT_BUCKET = FluidAttributes.BUCKET_VOLUME
  val EMPTY = FluidAmount(Fluids.EMPTY, 0, None)
  val BUCKET_LAVA = FluidAmount(Fluids.LAVA, AMOUNT_BUCKET, None)
  val BUCKET_WATER = FluidAmount(Fluids.WATER, AMOUNT_BUCKET, None)
  private[this] final val bucket_fluid_field: BucketItem => Fluid =
    item => ObfuscationReflectionHelper.getPrivateValue(classOf[BucketItem], item, "field_77876_a"): Fluid

  def fromItem(stack: ItemStack): FluidAmount = {
    stack.getItem match {
      case Items.LAVA_BUCKET => BUCKET_LAVA
      case Items.WATER_BUCKET => BUCKET_WATER
      case bucket: BucketItem =>
        Try(bucket_fluid_field(bucket)).map(FluidAmount(_, AMOUNT_BUCKET, None)).getOrElse(EMPTY)
      case _ => EMPTY
    }
  }

  def fromNBT(tag: CompoundNBT): FluidAmount = {
    val fluid = registry.getValue(new ResourceLocation(tag.getString(NBT_fluid)))
    if (fluid == EMPTY.fluid) {
      EMPTY
    } else {
      val amount = tag.getLong(NBT_amount)
      val nbt = tag.getCompound(NBT_tag)
      FluidAmount(fluid, amount, Some(nbt))
    }
  }

  def fromStack(stack: FluidStack): FluidAmount = {
    val fluid = stack.getFluid
    if (fluid == Fluids.EMPTY) {
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

  implicit val showFA: Show[FluidAmount] = fa => registry.getKey(fa.fluid).getPath + "@" + fa.amount + "mB"
}
