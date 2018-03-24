package com.kotori316.fluidtank.blocks

import net.minecraft.block.properties.{IProperty, PropertyInteger}
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

abstract class BlockTankVariants extends BlockTank {
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
