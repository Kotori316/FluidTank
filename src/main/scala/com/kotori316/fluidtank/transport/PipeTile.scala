package com.kotori316.fluidtank.transport

import cats.implicits._
import com.kotori316.fluidtank._
import com.kotori316.fluidtank.tiles.Tier
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.CapabilityFluidHandler

final class PipeTile(p: BlockPos, s: BlockState) extends PipeTileBase(ModObjects.FLUID_PIPE_TYPE, p, s) {
  private[this] val handler = new PipeFluidHandler(this)

  def tick(): Unit = if (!level.isClientSide) {
    if (connection.isEmpty)
      makeConnection()
    import scala.jdk.CollectionConverters._
    PipeBlock.FACING_TO_PROPERTY_MAP.asScala.flatMap { case (direction, value) =>
      if (getBlockState.getValue(value).isInput) {
        val sourcePos = getBlockPos.offset(direction)
        val c = for {
          t <- Cap.make(getLevel.getBlockEntity(sourcePos))
          cap <- getCapFromCache(t, sourcePos, direction.getOpposite, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        } yield cap -> sourcePos
        c.toList
      } else {
        List.empty
      }
    }.foreachEntry { (f, sourcePos) =>
      for {
        p <- connection.outputNonOrdered
        direction <- directions
        pos = p.offset(direction)
        if pos != sourcePos
        if getLevel.getBlockState(p).getValue(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput
        t <- Option(getLevel.getBlockEntity(pos)).toList
        dest <- getCapFromCache(t, pos, direction.getOpposite, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).toList
        if f != dest
      } {
        val transferSimulate = FluidUtil.tryFluidTransfer(dest, f, PipeTile.amountPerTick, false)
        if (!transferSimulate.isEmpty) {
          FluidUtil.tryFluidTransfer(dest, f, transferSimulate, true)
        }
      }
    }
  }

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
      if (side != null &&
        (!hasLevel || getBlockState.getValue(PipeBlock.FACING_TO_PROPERTY_MAP.get(side)).is(PipeBlock.Connection.CONNECTED, PipeBlock.Connection.INPUT))) {
        LazyOptional.of(() => handler.asInstanceOf[T])
      } else {
        LazyOptional.empty()
      }
    } else {
      super.getCapability(cap, side)
    }
  }

}

object PipeTile {
  final val amountPerTick = Utils.toInt(Tier.STONE.amount)
}
