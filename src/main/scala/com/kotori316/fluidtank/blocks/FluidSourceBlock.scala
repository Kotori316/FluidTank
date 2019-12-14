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

class FluidSourceBlock extends ContainerBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(0.5f)) {
  setRegistryName(FluidTank.modID, FluidSourceBlock.NAME)
  val itemBlock = new BlockItem(this, new Item.Properties().group(ModObjects.CREATIVE_TABS))
  itemBlock.setRegistryName(FluidTank.modID, FluidSourceBlock.NAME)

  override final def getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  override def createNewTileEntity(worldIn: IBlockReader): TileEntity = ModObjects.SOURCE_TYPE.create()

  override def onBlockActivated(state: BlockState, worldIn: World, pos: BlockPos,
                                player: PlayerEntity, handIn: Hand, hit: BlockRayTraceResult): Boolean = {
    val stack = player.getHeldItem(handIn)
    val fluid = FluidAmount.fromItem(stack)
    if (fluid.isEmpty) {
      stack.getItem match {
        case Items.BUCKET =>
          // Reset to empty.
          changeContent(worldIn, pos, FluidAmount.EMPTY, player)
          true
        case Items.CLOCK =>
          // Change interval time to push fluid.
          val i = if (handIn == Hand.MAIN_HAND) 1 else -1
          changeInterval(worldIn, pos, stack.getCount * i, player)
          true
        case _ => false
      }
    } else {
      changeContent(worldIn, pos, fluid, player)
      true
    }
  }

  def changeContent(world: World, pos: BlockPos, fluid: FluidAmount, player: PlayerEntity): Unit = if (!world.isRemote) {
    world.getTileEntity(pos) match {
      case s: FluidSourceTile =>
        if (s.fluid fluidEqual fluid) {
          s.fluid += fluid
        } else {
          s.fluid = fluid
        }
        player.sendStatusMessage(new TranslationTextComponent(FluidSourceBlock.CHANGE_SOURCE, s.fluid.getLocalizedName), false)
      case _ =>
    }
  }

  def changeInterval(world: World, pos: BlockPos, dt: Int, player: PlayerEntity): Unit = if (!world.isRemote) {
    world.getTileEntity(pos) match {
      case s: FluidSourceTile =>
        s.interval = Math.max(1, s.interval + dt)
        player.sendStatusMessage(new TranslationTextComponent(FluidSourceBlock.CHANGE_INTERVAL, s.interval), false)
      case _ =>
    }
  }
}

object FluidSourceBlock {
  final val NAME = "fluid_source"
  final val CHANGE_SOURCE = "chat.fluidtank.change_source"
  final val CHANGE_INTERVAL = "chat.fluidtank.change_interval"
}
