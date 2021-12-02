package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.tiles.CATTile
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.{BlockItem, Item}
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.level.block.{BaseEntityBlock, Block, RenderShape}
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.{InteractionHand, InteractionResult}
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.network.NetworkHooks
import net.minecraftforge.items.ItemHandlerHelper

class BlockCAT extends BaseEntityBlock(BlockBehaviour.Properties.of(ModObjects.MATERIAL).strength(0.7f)) {
  // Chest as Tank
  setRegistryName(FluidTank.modID, BlockCAT.NAME)
  val itemBlock = new BlockItem(this, new Item.Properties().tab(ModObjects.CREATIVE_TABS))
  itemBlock.setRegistryName(FluidTank.modID, BlockCAT.NAME)
  registerDefaultState(this.getStateDefinition.any.setValue(FACING, Direction.NORTH))

  override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit = builder.add(FACING)

  //noinspection ScalaDeprecation,ScalaDeprecation
  override final def getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = new CATTile(pos, state)

  override def getStateForPlacement(context: BlockPlaceContext): BlockState = this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection)

  //noinspection ScalaDeprecation
  override def use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult = {
    val stack = player.getItemInHand(hand)
    if (!player.isCrouching) {
      val copiedStack = if (stack.getCount == 1) stack else ItemHandlerHelper.copyStackWithSize(stack, 1)
      FluidUtil.getFluidHandler(copiedStack).asScala.value.value match {
        case Some(handlerItem) if !stack.isEmpty =>
          if (!level.isClientSide) {
            val direction = state.getValue(FACING)
            level.getBlockEntity(pos).asInstanceOf[CATTile].getFluidHandler(direction).ifPresent(cat =>
              BucketEventHandler.transferFluid(level, pos, player, hand, cat.getFluidAmountStream.findFirst().orElse(FluidAmount.EMPTY).toStack,
                stack, handlerItem, cat))
          }
          InteractionResult.SUCCESS
        case _ =>
          if (!level.isClientSide)
            NetworkHooks.openGui(player.asInstanceOf[ServerPlayer], level.getBlockEntity(pos).asInstanceOf[CATTile], pos)
          InteractionResult.SUCCESS
      }
      /* FluidUtil.getFluidHandler(copiedStack).asScala
        .filter(_ => !stack.isEmpty)
        .fold {
          if (!worldIn.isClientSide)
            NetworkHooks.openGui(player.asInstanceOf[ServerPlayer], worldIn.getBlockEntity(pos).asInstanceOf[CATTile], pos)
          InteractionResult.SUCCESS
        } { handlerItem =>
          if (!worldIn.isClientSide) {
            val direction = state.get(FACING)
            worldIn.getBlockEntity(pos).asInstanceOf[CATTile].getFluidHandler(direction).ifPresent(cat =>
              BucketEventHandler.transferFluid(worldIn, pos, player, handIn, cat.getFluidAmountStream.findFirst().orElse(FluidAmount.EMPTY).toStack,
                stack, handlerItem, cat))
          }
          InteractionResult.SUCCESS
        }.value*/
    } else {
      InteractionResult.PASS
    }
  }
}

object BlockCAT {
  final val NAME = "chest_as_tank"
}
