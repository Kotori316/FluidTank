package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.{FluidTank, ModObjects}
import net.minecraft.block.material.Material
import net.minecraft.block.{Block, BlockRenderType, BlockState, ContainerBlock}
import net.minecraft.item.{BlockItem, Item}
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.IBlockReader

class FluidSourceBlock extends ContainerBlock(Block.Properties.create(Material.IRON)) {
  setRegistryName(FluidTank.modID, "fluid_source")
  val itemBlock = new BlockItem(this, new Item.Properties().group(ModObjects.CREATIVE_TABS))
  itemBlock.setRegistryName(FluidTank.modID, "fluid_source")

  override final def getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  override def createNewTileEntity(worldIn: IBlockReader): TileEntity = ModObjects.SOURCE_TYPE.create()
}
