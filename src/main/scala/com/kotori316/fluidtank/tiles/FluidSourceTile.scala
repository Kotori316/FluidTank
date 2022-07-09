package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.fluids.{FluidAmount, VariantUtil}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class FluidSourceTile(p: BlockPos, s: BlockState) extends BlockEntity(ModObjects.SOURCE_TYPE, p, s) {

  private[this] var mFluid = FluidAmount.EMPTY
  var interval = 1
  var locked = true
  lazy val enabled: Boolean = FluidTank.config.enableFluidSupplier

  def tick(): Unit = if (!level.isClientSide && level.getGameTime % interval == 0 && enabled) {
    // In server world only.
    for {
      direction <- Direction.values()
      pos = getBlockPos.offset(direction)
      if !level.getBlockState(pos).isAir
    } {
      VariantUtil.fillAtPos(fluid, level, pos, direction)
    }
  }

  def fluid: FluidAmount = {
    if (this.locked) {
      FluidAmount.BUCKET_WATER.setAmount(mFluid.amount)
    } else {
      mFluid
    }
  }

  def fluid_=(fluidAmount: FluidAmount): Unit = {
    if (this.locked) {
      if (fluidAmount fluidEqual FluidAmount.BUCKET_WATER)
        mFluid = fluidAmount
      else if (fluidAmount.isEmpty)
        mFluid = FluidAmount.EMPTY
    } else {
      mFluid = fluidAmount
    }
  }

  override def load(compound: CompoundTag): Unit = {
    super.load(compound)
    locked = compound.getBoolean("locked")
    mFluid = FluidAmount.fromNBT(compound.getCompound("fluid"))
    interval = Math.max(1, compound.getInt("interval"))
  }

  override def saveAdditional(compound: CompoundTag): Unit = {
    val fluidNBT = new CompoundTag
    compound.putBoolean("locked", locked)
    this.mFluid.write(fluidNBT)
    compound.put("fluid", fluidNBT)
    compound.putInt("interval", interval)
    super.saveAdditional(compound)
  }

  override def getUpdateTag: CompoundTag = this.saveWithoutMetadata()

}
