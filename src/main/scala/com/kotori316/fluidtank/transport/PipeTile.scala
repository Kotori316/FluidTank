package com.kotori316.fluidtank.transport

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.tiles.Tier
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

final class PipeTile(p: BlockPos, s: BlockState) extends PipeTileBase(ModObjects.FLUID_PIPE_TYPE, p, s) {
  // private[this] val handler = new PipeFluidHandler(this)

  def tick(): Unit = if (!level.isClientSide) {
    /*if (connection.isEmpty)
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
    }*/
  }

}

object PipeTile {
  final val amountPerTick = Utils.toInt(Tier.STONE.amount)
}
