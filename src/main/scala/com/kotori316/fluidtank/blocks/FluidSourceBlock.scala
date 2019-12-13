package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.tiles.FluidSourceTile
import net.minecraft.block.material.Material
import net.minecraft.block.{Block, BlockRenderType, BlockState, ContainerBlock}
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{BlockItem, Item, Items}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Hand
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult}
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.{IBlockReader, World}

class FluidSourceBlock extends ContainerBlock(Block.Properties.create(Material.IRON)) {
  setRegistryName(FluidTank.modID, "fluid_source")
  val itemBlock = new BlockItem(this, new Item.Properties().group(ModObjects.CREATIVE_TABS))
  itemBlock.setRegistryName(FluidTank.modID, "fluid_source")

  override final def getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  override def createNewTileEntity(worldIn: IBlockReader): TileEntity = ModObjects.SOURCE_TYPE.create()

  override def onBlockActivated(state: BlockState, worldIn: World, pos: BlockPos,
                                player: PlayerEntity, handIn: Hand, hit: BlockRayTraceResult): Boolean = {
    val stack = player.getHeldItem(handIn)
    val fluid = FluidAmount.fromItem(stack)
    if (fluid.isEmpty) {
      if (stack.getItem == Items.BUCKET) {
        // Reset to empty.
        changeContent(worldIn, pos, FluidAmount.EMPTY)
        if (!worldIn.isRemote)
          player.sendStatusMessage(new TranslationTextComponent("chat.fluidtank.change_source", FluidAmount.EMPTY.getLocalizedName), false)
        true
      } else {
        false
      }
    } else {
      changeContent(worldIn, pos, fluid)
      if (!worldIn.isRemote)
        player.sendStatusMessage(new TranslationTextComponent("chat.fluidtank.change_source", fluid.getLocalizedName), false)
      true
    }
  }

  def changeContent(world: World, pos: BlockPos, fluid: FluidAmount): Unit = if (!world.isRemote) {
    world.getTileEntity(pos) match {
      case s: FluidSourceTile => s.fluid = fluid
      case _ =>
    }
  }
}
