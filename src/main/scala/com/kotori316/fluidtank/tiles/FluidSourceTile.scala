package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.fluids.FluidAmount
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler}

class FluidSourceTile(p: BlockPos, s: BlockState) extends BlockEntity(ModObjects.SOURCE_TYPE, p, s) {

  private[this] var mFluid = FluidAmount.EMPTY
  var interval = 1
  var locked = true
  lazy val enabled: Boolean = Config.content.enableFluidSupplier.get().booleanValue()

  def tick(): Unit = if (!level.isClientSide && level.getGameTime % interval == 0 && enabled) {
    // In server world only.
    for (direction <- directions) {
      for {
        tile <- Option(getLevel.getBlockEntity(getBlockPos.offset(direction)))
        cap <- tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite).asScala.value.value
      } yield {
        val accepted = cap.fill(fluid.toStack, IFluidHandler.FluidAction.SIMULATE)
        if (accepted > 0) {
          cap.fill(fluid.setAmount(accepted).toStack, IFluidHandler.FluidAction.EXECUTE)
        } else {
          0
        }
      }
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

  override def save(compound: CompoundTag): CompoundTag = {
    saveAdditional(compound)
    super.save(compound)
  }

  override def getUpdateTag: CompoundTag = this.saveWithFullMetadata()

}
