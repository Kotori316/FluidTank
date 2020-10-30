package com.kotori316.fluidtank.transport

import cats.implicits._
import com.kotori316.fluidtank._
import com.kotori316.fluidtank.tiles.Tiers
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.CapabilityFluidHandler.{FLUID_HANDLER_CAPABILITY => CAP}

final class PipeTile extends PipeTileBase(ModObjects.FLUID_PIPE_TYPE) {
  private[this] val handler = new PipeFluidHandler(this)

  override def tick(): Unit = if (!world.isRemote) {
    if (connection.isEmpty)
      makeConnection()
    import scala.jdk.CollectionConverters._
    PipeBlock.FACING_TO_PROPERTY_MAP.asScala.flatMap { case (direction, value) =>
      if (getBlockState.get(value).isInput) {
        val sourcePos = pos.offset(direction)
        val c = for {
          t <- Cap.make(getWorld.getTileEntity(sourcePos))
          cap <- getCapFromCache(t, sourcePos, direction.getOpposite, CAP)
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
        if getWorld.getBlockState(p).get(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput
        t <- Option(getWorld.getTileEntity(pos)).toList
        dest <- getCapFromCache(t, pos, direction.getOpposite, CAP).toList
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
    Cap.asJava(
      Cap.make(handler.asInstanceOf[T])
        .filter(_ => cap == CAP)
        .filter(_ => side != null && getBlockState.get(PipeBlock.FACING_TO_PROPERTY_MAP.get(side)).is(PipeBlock.Connection.CONNECTED, PipeBlock.Connection.INPUT))
        .orElse(super.getCapability(cap, side).asScala)
    )
  }

}

object PipeTile {
  final val amountPerTick = Utils.toInt(Tiers.STONE.amount)
}
