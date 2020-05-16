package com.kotori316.fluidtank.transport

import com.kotori316.fluidtank._
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.CapabilityItemHandler

class ItemPipeTile extends PipeTileBase(ModObjects.ITEM_PIPE_TYPE) {
  val handler = new PipeItemHandler(this)

  override def tick(): Unit = {
    if (connection.isEmpty)
      makeConnection()
  }

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    Cap.asJava(
      Cap.make(handler.asInstanceOf[T])
        .filter(_ => cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        .filter(_ => side != null && getBlockState.get(PipeBlock.FACING_TO_PROPERTY_MAP.get(side)).is(PipeBlock.Connection.CONNECTED, PipeBlock.Connection.INPUT))
        .orElse(super.getCapability(cap, side).asScala)
    )
  }
}
