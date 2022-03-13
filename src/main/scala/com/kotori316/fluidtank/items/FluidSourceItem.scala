package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.blocks.FluidSourceBlock
import net.minecraft.world.item.{BlockItem, Item, ItemStack}
import net.minecraft.world.level.block.Block

final class FluidSourceItem(blockIn: Block, builder: Item.Properties) extends BlockItem(blockIn, builder) {
  override def getDescriptionId(stack: ItemStack): String = {
    if (!FluidSourceBlock.isCheatStack(stack)) "block.fluidtank.water_source"
    else super.getDescriptionId(stack)
  }
}
