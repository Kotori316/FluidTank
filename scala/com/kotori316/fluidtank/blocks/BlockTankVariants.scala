package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.tiles.Tiers
import net.minecraft.block.properties.{IProperty, PropertyInteger}
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

class BlockTankVariants(rank: Int, tiers: Tiers) extends BlockTank(rank, defaultTier = tiers) {

    final lazy val tierArray = Tiers.list.filter(t => t.rank == rank).toArray

    override def getTierByMeta(meta: Int) = {
        if (meta < tierArray.length)
            tierArray(meta)
        else
            tierArray(0)
    }

    final lazy val property = PropertyInteger.create("variant", 0, tierArray.length - 1)

    final def properties: Seq[IProperty[_ <: Comparable[_]]] = Seq(property)

    setDefaultState(this.blockState.getBaseState.withProperty(property, Int.box(0)))

    override def getStateFromMeta(meta: Int): IBlockState = getDefaultState.withProperty(property, Int.box(meta))

    override def getMetaFromState(state: IBlockState): Int = damageDropped(state)

    override def createBlockState(): BlockStateContainer = new BlockStateContainer(this, properties: _*)

    override def getSubBlocks(itemIn: CreativeTabs, items: NonNullList[ItemStack]): Unit = {
        for (i <- tierArray.indices) {
            items.add(new ItemStack(this, 1, i))
        }
    }

    override def damageDropped(state: IBlockState): Int = state.getValue(property).intValue()
}
