package com.kotori316.fluidtank.tiles

import cats.Id
import cats.implicits._
import com.kotori316.fluidtank._
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.{ITickableTileEntity, TileEntity}
import net.minecraftforge.fluids.capability.CapabilityFluidHandler

class FluidSourceTile extends TileEntity(ModObjects.SOURCE_TYPE)
  with ITickableTileEntity {

  var fluid = FluidAmount.EMPTY
  var interval = 1

  override def tick(): Unit = if (!world.isRemote && world.getGameTime % interval == 0) {
    // In server world only.
    for (direction <- directions) {
      for {
        tile <- Option(getWorld.getTileEntity(getPos.offset(direction))).toOptionT[Id]
        cap <- tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite).asScala.mapK(evalExtractor)
      } yield {
        val accepted = cap.fill(fluid.toStack, FluidAmount.b2a(false))
        if (accepted > 0) {
          cap.fill(fluid.setAmount(accepted).toStack, FluidAmount.b2a(true))
        } else {
          0
        }
      }
    }
  }

  override def read(compound: CompoundNBT): Unit = {
    super.read(compound)
    fluid = FluidAmount.fromNBT(compound.getCompound("fluid"))
    interval = Math.max(1, compound.getInt("interval"))
  }

  override def write(compound: CompoundNBT): CompoundNBT = {
    val fluidNBT = new CompoundNBT
    this.fluid.write(fluidNBT)
    compound.put("fluid", fluidNBT)
    compound.putInt("interval", interval)
    super.write(compound)
  }
}
