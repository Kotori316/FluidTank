package com.kotori316.fluidtank

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.{FluidAmount => BCAmount}
import alexiil.mc.lib.attributes.fluid.volume.{FluidKeys, FluidVolume}
import com.kotori316.fluidtank.ModTank.Entries
import net.minecraft.fluid.{Fluid, Fluids}
import net.minecraft.item.{BucketItem, ItemStack, Items}
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.registry.{DefaultedRegistry, Registry}

/**
 * Just a wrapper of [[FluidVolume]]
 */
case class FluidAmount(fluidVolume: FluidVolume) {
  val amount: Long = fluidVolume.amount().asLong(1000L)
  val fluid: Fluid = fluidVolume.getRawFluid

  def setAmount(newAmount: Long): FluidAmount = {
    if (newAmount == amount) this // No need to create new instance.
    else FluidAmount(fluidVolume.fluidKey.withAmount(BCAmount.of(newAmount, 1000L)))
  }

  def write(tag: CompoundTag): CompoundTag = fluidVolume.toTag(tag)

  def nonEmpty: Boolean = !isEmpty

  def isEmpty: Boolean = fluidVolume.isEmpty

  def isGaseous: Boolean = fluidVolume.fluidKey.gaseous

  def getLocalizedName: String = String.valueOf(FluidAmount.registry.getId(fluid))

  def +(that: FluidAmount): FluidAmount = {
    if (fluidVolume.getRawFluid == Fluids.EMPTY) that
    else FluidAmount(fluidVolume.withAmount(fluidVolume.amount().add(that.fluidVolume.amount())))
  }

  def -(that: FluidAmount): FluidAmount = FluidAmount(fluidVolume.withAmount(fluidVolume.amount().sub(that.fluidVolume.amount())))

  def fluidEqual(that: FluidAmount): Boolean = FluidVolume.areEqualExceptAmounts(this.fluidVolume, that.fluidVolume)

  override def toString: String = FluidAmount.registry.getId(fluidVolume.getRawFluid).getPath + "@" + fluidVolume.amount() + "mB"
}

object FluidAmount {
  final val NBT_fluid = "fluid"
  final val NBT_amount = "amount"
  final val NBT_tag = "tag"
  final val AMOUNT_BUCKET = 1000 //FluidAttributes.BUCKET_VOLUME
  val EMPTY: FluidAmount = FluidAmount(FluidVolumeUtil.EMPTY)
  val BUCKET_LAVA: FluidAmount = FluidAmount(FluidKeys.LAVA.withAmount(BCAmount.BUCKET))
  val BUCKET_WATER: FluidAmount = FluidAmount(FluidKeys.WATER.withAmount(BCAmount.BUCKET))
  val BUCKET_MILK: FluidAmount = FluidAmount(FluidKeys.get(Entries.MILK_FLUID).withAmount(BCAmount.BUCKET))

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
    val volume = FluidVolume.fromTag(tag)
    FluidAmount(volume)
  }

  def registry: DefaultedRegistry[Fluid] = Registry.FLUID

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
