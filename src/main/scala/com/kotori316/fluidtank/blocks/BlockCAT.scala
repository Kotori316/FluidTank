package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.tiles.CATTile
import net.minecraft.block.{Block, BlockRenderType, BlockState, ContainerBlock}
import net.minecraft.entity.player.{PlayerEntity, ServerPlayerEntity}
import net.minecraft.item.{BlockItem, BlockItemUseContext, Item}
import net.minecraft.state.StateContainer
import net.minecraft.state.properties.BlockStateProperties.FACING
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult}
import net.minecraft.util.{ActionResultType, Direction, Hand}
import net.minecraft.world.{IBlockReader, World}
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fml.network.NetworkHooks
import net.minecraftforge.items.ItemHandlerHelper

class BlockCAT extends ContainerBlock(Block.Properties.create(ModObjects.MATERIAL).hardnessAndResistance(0.7f)) {
  // Chest as Tank
  setRegistryName(FluidTank.modID, BlockCAT.NAME)
  val itemBlock = new BlockItem(this, new Item.Properties().group(ModObjects.CREATIVE_TABS))
  itemBlock.setRegistryName(FluidTank.modID, BlockCAT.NAME)
  setDefaultState(getStateContainer.getBaseState.`with`(FACING, Direction.NORTH))

  override def fillStateContainer(builder: StateContainer.Builder[Block, BlockState]): Unit = builder.add(FACING)

  override final def getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  override def createNewTileEntity(worldIn: IBlockReader): TileEntity = new CATTile

  override def getStateForPlacement(context: BlockItemUseContext): BlockState = this.getDefaultState.`with`(FACING, context.getNearestLookingDirection)

  override def onBlockActivated(state: BlockState, worldIn: World, pos: BlockPos, player: PlayerEntity, handIn: Hand, hit: BlockRayTraceResult): ActionResultType = {
    val stack = player.getHeldItem(handIn)
    if (!player.isCrouching) {
      val copiedStack = if (stack.getCount == 1) stack else ItemHandlerHelper.copyStackWithSize(stack, 1)
      FluidUtil.getFluidHandler(copiedStack).asScala.value.value match {
        case Some(handlerItem) if !stack.isEmpty =>
          if (!worldIn.isRemote) {
            val direction = state.get(FACING)
            worldIn.getTileEntity(pos).asInstanceOf[CATTile].getFluidHandler(direction).ifPresent(cat =>
              BucketEventHandler.transferFluid(worldIn, pos, player, handIn, cat.getFluidAmountStream.findFirst().orElse(FluidAmount.EMPTY).toStack,
                stack, handlerItem, cat))
          }
          ActionResultType.SUCCESS
        case _ =>
          if (!worldIn.isRemote)
            NetworkHooks.openGui(player.asInstanceOf[ServerPlayerEntity], worldIn.getTileEntity(pos).asInstanceOf[CATTile], pos)
          ActionResultType.SUCCESS
      }
    } else {
      ActionResultType.PASS
    }
  }
}

object BlockCAT {
  final val NAME = "chest_as_tank"
}
