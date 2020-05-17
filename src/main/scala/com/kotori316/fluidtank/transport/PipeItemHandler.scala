package com.kotori316.fluidtank.transport

import com.kotori316.fluidtank._
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler

class PipeItemHandler(pipeTile: ItemPipeTile) extends IItemHandler {
  override def getSlots: Int = 1

  override def getStackInSlot(slot: Int): ItemStack = ItemStack.EMPTY

  override def insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack = {
    val pipePosIterator = pipeTile.connection.outputs(pipeTile.getPos).iterator
    var rest = stack
    while (pipePosIterator.hasNext) {
      val pipePos = pipePosIterator.next()
      val handlerIterator = directions.map(dir => pipePos.offset(dir) -> dir).iterator
        .filter { case (_, direction) => pipeTile.getWorld.getBlockState(pipePos).get(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput }
        .flatMap { case (pos, direction) => pipeTile.findItemHandler(pipeTile.getWorld, pos, direction).value.value }
      while (handlerIterator.hasNext) {
        val (handler, _) = handlerIterator.next()
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
