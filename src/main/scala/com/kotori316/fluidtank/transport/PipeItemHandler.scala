package com.kotori316.fluidtank.transport

import com.kotori316.fluidtank._
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandler

class PipeItemHandler(pipeTile: ItemPipeTile) extends IItemHandler {
  override def getSlots: Int = 1

  override def getStackInSlot(slot: Int): ItemStack = ItemStack.EMPTY

  override def insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack = {
    val pipePosIterator = pipeTile.connection.outputs(pipeTile.getBlockPos).iterator
    var rest = stack
    while (pipePosIterator.hasNext) {
      val pipePos = pipePosIterator.next()
      val handlerIterator =
        for {
          direction <- directions.iterator
          pos = pipePos.offset(direction)
          if pipeTile.getLevel.getBlockState(pipePos).getValue(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput
          h <- pipeTile.findItemHandler(pipeTile.getLevel, pos, direction).value.value
        } yield h._1

      while (handlerIterator.hasNext) {
        val handler = handlerIterator.next()
        rest = handler.insertItem(slot, rest, simulate)
        if (rest.isEmpty)
          return ItemStack.EMPTY
      }
    }
    rest
  }

  override def extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack = ItemStack.EMPTY

  override def getSlotLimit(slot: Int): Int = 64

  override def isItemValid(slot: Int, stack: ItemStack): Boolean = true
}
