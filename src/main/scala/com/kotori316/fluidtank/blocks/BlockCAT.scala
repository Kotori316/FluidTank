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
import net.minecraftforge.network.NetworkHooks

import scala.jdk.OptionConverters.RichOptional

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
      val result = for {
        cat <- level.getBlockEntity(pos, ModObjects.CAT_TYPE).toScala
        if !stack.isEmpty
        catHandler <- cat.getFluidHandler(state.getValue(FACING)).resolve().toScala
        r <- if (!level.isClientSide) BucketEventHandler.transferFluid(catHandler, catHandler.getFluidAmountStream.findFirst().orElse(FluidAmount.EMPTY).toStack, stack)
        else BucketEventHandler.checkStack(stack)
        if r.result.isSuccess
      } yield r
      result match {
        case Some(value) =>
          if (!level.isClientSide) BucketEventHandler.setItem(level, pos, player, hand, value, stack)
          InteractionResult.SUCCESS
        case _ =>
          if (!level.isClientSide)
            NetworkHooks.openGui(player.asInstanceOf[ServerPlayer], level.getBlockEntity(pos).asInstanceOf[CATTile], pos)
          InteractionResult.SUCCESS
      }
    } else {
      InteractionResult.PASS
    }
  }
}

object BlockCAT {
  final val NAME = "chest_as_tank"
}
