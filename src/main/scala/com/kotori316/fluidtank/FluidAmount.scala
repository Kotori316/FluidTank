package com.kotori316.fluidtank

import com.kotori316.fluidtank.ModTank.Entries
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.fluid.{Fluid, Fluids}
import net.minecraft.item.{BucketItem, ItemStack, Items}
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

case class FluidAmount(fluid: Fluid, amount: Long, nbt: Option[CompoundTag]) {
  def setAmount(newAmount: Long): FluidAmount = {
    if (newAmount == this.amount) this // No need to create new instance.
    else FluidAmount(fluid, newAmount, nbt)
  }

  def write(tag: CompoundTag): CompoundTag = {
    tag.putString(FluidAmount.NBT_fluid, FluidAmount.registry.getId(fluid).toString)
    tag.putLong(FluidAmount.NBT_amount, amount)
    nbt.foreach(n => tag.put(FluidAmount.NBT_tag, n))
    tag
  }

  def nonEmpty: Boolean = fluid != Fluids.EMPTY && amount > 0

  def isEmpty: Boolean = !nonEmpty

  def isGaseous: Boolean = false //fluid.getAttributes.isGaseous

  def getLocalizedName: String = String.valueOf(FluidAmount.registry.getId(fluid))

  def +(that: FluidAmount): FluidAmount = {
    if (fluid == Fluids.EMPTY) that
    else setAmount(this.amount + that.amount)
  }

  def -(that: FluidAmount): FluidAmount = setAmount(this.amount - that.amount)

  def fluidEqual(that: FluidAmount): Boolean = this.fluid == that.fluid && this.nbt == that.nbt

  override def toString = FluidAmount.registry.getId(fluid).getPath + "@" + amount + "mB" + nbt.fold("")(" " + _.toString)
}

object FluidAmount {
  final val NBT_fluid = "fluid"
  final val NBT_amount = "amount"
  final val NBT_tag = "tag"
  final val AMOUNT_BUCKET = 1000 //FluidAttributes.BUCKET_VOLUME
  val EMPTY = FluidAmount(Fluids.EMPTY, 0, None)
  val BUCKET_LAVA = FluidAmount(Fluids.LAVA, AMOUNT_BUCKET, None)
  val BUCKET_WATER = FluidAmount(Fluids.WATER, AMOUNT_BUCKET, None)
  val BUCKET_MILK = FluidAmount(Entries.MILK_FLUID, AMOUNT_BUCKET, None)

  def fromItem(stack: ItemStack): FluidAmount = {
    stack.getItem match {
      case Items.LAVA_BUCKET => BUCKET_LAVA
      case Items.WATER_BUCKET => BUCKET_WATER
      case Items.MILK_BUCKET => BUCKET_MILK
      case Items.BUCKET => EMPTY
      case bucket: BucketItem =>
        BUCKET_WATER
      //bucket.pure[Id].map(_.getFluid).map(FluidAmount(_, AMOUNT_BUCKET, None))
      case _ => EMPTY
    }
  }

  def isFluidContainer(stack: ItemStack): Boolean = {
    stack.getItem.isInstanceOf[BucketItem] || fromItem(stack).nonEmpty
  }

  def fromNBT(tag: CompoundTag): FluidAmount = {
    val fluid = registry.get(new Identifier(Option(tag).map(_.getString(NBT_fluid)).getOrElse(registry.getId(Fluids.EMPTY).toString)))
    if (fluid == null || fluid == EMPTY.fluid) {
      EMPTY
    } else {
      val amount = tag.getLong(NBT_amount)
      val nbt = if (tag.contains(NBT_tag, NbtType.COMPOUND)) Option(tag.getCompound(NBT_tag)).filterNot(_.isEmpty) else None
      FluidAmount(fluid, amount, nbt)
    }
  }

  def registry = Registry.FLUID

  trait Tank {
    /**
     * @return Fluid that was accepted by the tank.
     */
    def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Long = 0): FluidAmount

    /**
     * @param fluidAmount the fluid representing the kind and maximum amount to drain.
     *                    Empty Fluid means fluid type can be anything.
     * @param doDrain     false means simulating.
     * @param min         minimum amount to drain.
     * @return the fluid and amount that is (or will be) drained.
     */
    def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Long = 0): FluidAmount

  }

}
