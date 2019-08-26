package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.Config
import com.kotori316.fluidtank.tiles.{Tiers, TileTankNoDisplay}
import net.minecraft.block.BlockState
import net.minecraft.item.{ItemGroup, ItemStack}
import net.minecraft.util.NonNullList
import net.minecraft.world.IBlockReader

import scala.util.Try

class BlockInvisibleTank(t: Tiers) extends BlockTank(t) {
  override def namePrefix = "invisible_tank_"

  override def createTileEntity(state: BlockState, world: IBlockReader) = new TileTankNoDisplay(tier)

  override def fillItemGroup(group: ItemGroup, items: NonNullList[ItemStack]): Unit = {
    if (Try(Config.content.showInvisibleTank.get()).map(_.booleanValue()).getOrElse(false)) {
      super.fillItemGroup(group, items)
    }
  }
}
