package com.kotori316.fluidtank.network

import java.util.function.Supplier

import com.kotori316.fluidtank.FluidTank
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.PacketBuffer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent

/**
  * To both client and server.
  */
class TileMessage {
  var pos = BlockPos.ZERO
  var dim = 0
  var nbt: CompoundNBT = _

  def write(buffer: PacketBuffer): Unit = {
    buffer.writeBlockPos(pos).writeInt(dim)
    buffer.writeCompoundTag(nbt)
  }

  def onReceive(supplier: Supplier[NetworkEvent.Context]): Unit = {
    for (world <- FluidTank.proxy.getWorld(supplier.get()) if world.getDimension.getType.getId == dim;
         tile <- Option(world.getTileEntity(pos))) {
      supplier.get().enqueueWork(() => tile.read(nbt))
      supplier.get().setPacketHandled(true)
    }
  }
}

object TileMessage {
  def apply(buffer: PacketBuffer): TileMessage = {
    val m = new TileMessage()
    m.pos = buffer.readBlockPos()
    m.dim = buffer.readInt()
    m.nbt = buffer.readCompoundTag()
    m
  }

  def apply(tile: TileEntity): TileMessage = {
    val m = new TileMessage()
    m.pos = tile.getPos
    m.dim = Option(tile.getWorld).map(_.getDimension.getType.getId).getOrElse(0)
    m.nbt = tile.write(new CompoundNBT)
    m
  }
}