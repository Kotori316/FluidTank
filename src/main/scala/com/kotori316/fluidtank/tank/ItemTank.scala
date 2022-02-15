package com.kotori316.fluidtank.tank

import alexiil.mc.lib.attributes.fluid.amount.{FluidAmount => BCAmount}
import com.kotori316.fluidtank.FluidAmount
import net.minecraft.nbt.{CompoundTag, Tag}
import org.jetbrains.annotations.Nullable

import scala.math.Ordering.Implicits._

/**
 * This class represent single slot tank.
 *
 * This class is mutable.
 *
 * @param fluid    represents current internal status. mutable.
 * @param capacity immutable
 */
class ItemTank(private var fluid: FluidAmount, capacity: Long) extends FluidAmount.Tank {
  def getFluid: FluidAmount = this.fluid

  def getCapacity: Long = this.capacity

  override def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Long): FluidAmount = {
    if (canFillFluidType(fluidAmount) && fluidAmount.nonEmpty) {
      val newAmount = capacityInBC min (this.fluid.fluidVolume.amount() add fluidAmount.fluidVolume.amount())
      val filled = newAmount sub this.fluid.fluidVolume.amount()
      if (filled >= BCAmount.of(min, FluidAmount.AMOUNT_BUCKET)) {
        if (doFill) {
          // this.fluid may be empty, so I should use given fluid.
          this.fluid = fluidAmount.setAmount(newAmount)
        }
        fluidAmount.setAmount(filled)
      } else {
        // This tank is already full or accepted isn't over min.
        FluidAmount.EMPTY
      }
    } else {
      FluidAmount.EMPTY
    }
  }

  /**
   * @param fluidAmount the fluid representing the kind and maximum amount to drain.
   *                    Empty Fluid means fluid type can be anything, but currently this is not supported.
   * @param doDrain     false means simulating.
   * @param min         minimum amount to drain.
   * @return the fluid and amount that is (or will be) drained.
   */
  override def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Long): FluidAmount = {
    if (this.fluid.nonEmpty && (fluidAmount.fluidEqual(FluidAmount.EMPTY) || this.fluid.fluidEqual(fluidAmount))) {
      val drain = this.fluid.fluidVolume.amount() min fluidAmount.fluidVolume.amount()
      if (drain >= BCAmount.of(min, FluidAmount.AMOUNT_BUCKET)) {
        val newAmount = this.fluid.fluidVolume.amount() sub drain
        if (doDrain) {
          this.fluid = this.fluid.setAmount(newAmount)
        }
        this.fluid.setAmount(drain)
      } else {
        FluidAmount.EMPTY
      }
    } else {
      FluidAmount.EMPTY
    }
  }

  def canFillFluidType(fluid: FluidAmount): Boolean = {
    this.fluid.isEmpty || this.fluid.fluidEqual(fluid)
  }

  private def capacityInBC: BCAmount = BCAmount.of(capacity, FluidAmount.AMOUNT_BUCKET)

}

object ItemTank {
  def empty(capacity: Long): ItemTank = new ItemTank(FluidAmount.EMPTY, capacity)

  def from(@Nullable tag: CompoundTag, tier: Tiers): ItemTank = {
    if (tag == null) {
      empty(tier.amount())
    } else {
      val fluid = FluidAmount.fromNBT(tag.getCompound(TankBlock.NBT_Tank))
      val capacity: Long =
        if (tag.contains(TankBlock.NBT_Capacity, Tag.TAG_INT))
          tag.getInt(TankBlock.NBT_Capacity)
        else tier.amount()
      new ItemTank(fluid, capacity)
    }
  }
}
