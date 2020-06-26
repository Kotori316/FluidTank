package com.kotori316.fluidtank.network

import java.util.function.Supplier

import com.kotori316.fluidtank.FluidTank
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.PacketBuffer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.DimensionType
import net.minecraftforge.fml.network.NetworkEvent

/**
 * To both client and server.
 */
class TileMessage {
  var pos: BlockPos = BlockPos.ZERO
  var dim: ResourceLocation = _
  var nbt: CompoundNBT = _

  def write(buffer: PacketBuffer): Unit = {
    buffer.writeBlockPos(pos).writeResourceLocation(dim)
    buffer.writeCompoundTag(nbt)
  }

  def onReceive(supplier: Supplier[NetworkEvent.Context]): Unit = {
    for (world <- FluidTank.proxy.getWorld(supplier.get()) if world.func_234922_V_.func_240901_a_() == dim;
         tile <- Option(world.getTileEntity(pos))) {
      supplier.get().enqueueWork(() => tile.func_230337_a_(world.getBlockState(pos), nbt))
    }
    supplier.get().setPacketHandled(true)
  }
}

object TileMessage {
  def apply(buffer: PacketBuffer): TileMessage = {
    val m = new TileMessage()
    m.pos = buffer.readBlockPos()
    m.dim = buffer.readResourceLocation()
    m.nbt = buffer.readCompoundTag()
    m
  }

  def apply(tile: TileEntity): TileMessage = {
    val m = new TileMessage()
    m.pos = tile.getPos
    m.dim = Option(tile.getWorld).map(_.func_234922_V_).getOrElse(DimensionType.field_235999_c_).func_240901_a_()
    m.nbt = tile.write(new CompoundNBT)
    m
  }
}
