package com.kotori316.fluidtank.network

import java.util.function.Supplier

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.tiles.CATTile
import net.minecraft.core.{BlockPos, Registry}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraftforge.network.NetworkEvent

/**
 * To client only.
 */
case class FluidCacheMessage(dimensionId: ResourceKey[Level],
                             pos: BlockPos,
                             amounts: Seq[FluidAmount]) {
  def this(tile: CATTile) = {
    this(
      Option(tile.getLevel).map(_.dimension()).getOrElse(Level.OVERWORLD),
      tile.getBlockPos,
      tile.fluidAmountList
    )
  }

  def write(buffer: FriendlyByteBuf): Unit = {
    buffer.writeBlockPos(pos).writeResourceLocation(dimensionId.location)
    buffer.writeInt(amounts.size)
    amounts.foreach(a => buffer.writeNbt(a.write(new CompoundTag)))
  }

  def onReceive(ctx: Supplier[NetworkEvent.Context]): Unit = {
    FluidTank.proxy.getLevel(ctx.get).filter(_.dimension.equals(dimensionId))
      .map(_.getBlockEntity(pos))
      .collect { case c: CATTile => c }
      .foreach(tile => ctx.get.enqueueWork(() => tile.fluidCache = this.amounts))
    ctx.get.setPacketHandled(true)
  }
}

object FluidCacheMessage {
  def apply(buffer: FriendlyByteBuf): FluidCacheMessage = {
    val pos = buffer.readBlockPos
    val dimensionId = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation)
    val size = buffer.readInt
    val amounts = Seq.fill(size)(buffer.readNbt()).map(FluidAmount.fromNBT)
    new FluidCacheMessage(dimensionId, pos, amounts)
  }
}
