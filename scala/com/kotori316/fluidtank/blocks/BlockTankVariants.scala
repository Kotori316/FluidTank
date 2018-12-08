package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.Config
import com.kotori316.fluidtank.tiles.Tiers
import net.minecraft.block.properties.{IProperty, PropertyInteger}
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

class BlockTankVariants(rank: Int, tiers: Tiers) extends BlockTank(rank, defaultTier = tiers) {

  final lazy val tierArray = Tiers.list.filter(t => t.rank == rank).toArray

  override def getTierByMeta(meta: Int) = {
    if ((meta & 7) < tierArray.length)
      tierArray(meta & 7)
    else
      tierArray(0)
  }

  final lazy val variantProperty = PropertyInteger.create("variant", 0, tierArray.length - 1)

  final def properties: Seq[IProperty[_ <: Comparable[_]]] = Seq(variantProperty, visibleProperty)

  setDefaultState(this.blockState.getBaseState.withProperty(variantProperty, Int.box(0)).withProperty(visibleProperty, Boolean.box(true)))

  override def getStateFromMeta(meta: Int): IBlockState = super.getStateFromMeta(meta).withProperty(variantProperty, Int.box(meta & 7))

  override def createBlockState(): BlockStateContainer = new BlockStateContainer(this, properties: _*)

  override def getSubBlocks(itemIn: CreativeTabs, items: NonNullList[ItemStack]): Unit = {
    for (i <- tierArray.indices) {
      items.add(new ItemStack(this, 1, i))
      if (Config.content.showInvisibleTank)
        items.add(new ItemStack(this, 1, i | 8))
    }
  }

  override def damageDropped(state: IBlockState): Int =
    super.damageDropped(state) | state.getValue(variantProperty).intValue()
}
