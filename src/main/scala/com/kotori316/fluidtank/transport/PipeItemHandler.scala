package com.kotori316.fluidtank.transport

import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler

class PipeItemHandler(pipe: ItemPipeTile) extends IItemHandler {
  override def getSlots: Int = 1

  override def getStackInSlot(slot: Int): ItemStack = ItemStack.EMPTY

  override def insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack = {
    stack
  }

  override def extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack = ItemStack.EMPTY

  override def getSlotLimit(slot: Int): Int = 64

  override def isItemValid(slot: Int, stack: ItemStack): Boolean = true
}
