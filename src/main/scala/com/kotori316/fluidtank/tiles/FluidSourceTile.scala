package com.kotori316.fluidtank.tiles

import cats.Id
import cats.implicits._
import com.kotori316.fluidtank._
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.{ITickableTileEntity, TileEntity}
import net.minecraftforge.fluids.capability.CapabilityFluidHandler

class FluidSourceTile extends TileEntity(ModObjects.SOURCE_TYPE)
  with ITickableTileEntity {

  private[this] var mFluid = FluidAmount.EMPTY
  var interval = 1
  var locked = true
  lazy val enabled = Config.content.enableFluidSupplier.get().booleanValue()

  override def tick(): Unit = if (!world.isRemote && world.getGameTime % interval == 0 && enabled) {
    // In server world only.
    for (direction <- directions) {
      for {
        tile <- Option(getWorld.getTileEntity(getPos.offset(direction))).toOptionT[Id]
        cap <- tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite).asScala.mapK(evalExtractor)
      } yield {
        cap match {
          case tank: FluidAmount.Tank =>
            val accepted = tank.fill(fluid, doFill = false)
            if (accepted.nonEmpty) {
              tank.fill(accepted, doFill = true).amount
            } else {
              0L
            }
          case _ =>
            val accepted = cap.fill(fluid.toStack, FluidAmount.b2a(false))
            if (accepted > 0) {
              cap.fill(fluid.setAmount(accepted).toStack, FluidAmount.b2a(true))
            } else {
              0
            }
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

  override def read(compound: CompoundNBT): Unit = {
    super.read(compound)
    locked = compound.getBoolean("locked")
    mFluid = FluidAmount.fromNBT(compound.getCompound("fluid"))
    interval = Math.max(1, compound.getInt("interval"))
  }

  override def write(compound: CompoundNBT): CompoundNBT = {
    val fluidNBT = new CompoundNBT
    compound.putBoolean("locked", locked)
    this.mFluid.write(fluidNBT)
    compound.put("fluid", fluidNBT)
    compound.putInt("interval", interval)
    super.write(compound)
  }

  override def getUpdateTag = this.write(new CompoundNBT)

}
