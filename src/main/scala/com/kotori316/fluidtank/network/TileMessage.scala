package com.kotori316.fluidtank.network

import java.util.function.Supplier

import com.kotori316.fluidtank.FluidTank
import net.minecraft.core.{BlockPos, Registry}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.network.NetworkEvent

/**
 * To both client and server.
 */
class TileMessage(pos: BlockPos, dim: ResourceKey[Level], tag: CompoundTag) {

  def write(buffer: FriendlyByteBuf): Unit = {
    buffer.writeBlockPos(pos).writeResourceLocation(dim.location())
    buffer.writeNbt(tag)
  }

  def onReceive(supplier: Supplier[NetworkEvent.Context]): Unit = {
    for {
      world <- FluidTank.proxy.getLevel(supplier.get())
      if world.dimension() == dim
      tile <- Option(world.getBlockEntity(pos))
    } {
      supplier.get().enqueueWork(() => tile.load(tag))
    }
    supplier.get().setPacketHandled(true)
  }
}

object TileMessage {
  def apply(buffer: FriendlyByteBuf): TileMessage = {
    val pos = buffer.readBlockPos()
    val dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation())
    val nbt = buffer.readNbt()
    new TileMessage(pos, dim, nbt)
  }

  def apply(tile: BlockEntity): TileMessage = {
    val pos = tile.getBlockPos
    val dim = Option(tile.getLevel).map(_.dimension()).getOrElse(Level.OVERWORLD)
    val nbt = tile.serializeNBT()
    new TileMessage(pos, dim, nbt)
  }
}
