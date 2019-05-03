package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.Config
import com.kotori316.fluidtank.tiles.{Tiers, TileTankNoDisplay}
import net.minecraft.block.state.IBlockState
import net.minecraft.item.{ItemGroup, ItemStack}
import net.minecraft.util.NonNullList
import net.minecraft.world.IBlockReader

class BlockInvisibleTank(t: Tiers) extends BlockTank(t) {
  override def namePrefix = "invisible_tank_"

  override def createTileEntity(state: IBlockState, world: IBlockReader) = new TileTankNoDisplay(tier)

  override def fillItemGroup(group: ItemGroup, items: NonNullList[ItemStack]): Unit = {
    if (Config.content.showInvisibleTank.get()) {
      super.fillItemGroup(group, items)
    }
  }
}
