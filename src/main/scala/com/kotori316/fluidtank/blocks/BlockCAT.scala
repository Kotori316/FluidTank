package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.tiles.CATTile
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.resources.ResourceLocation
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

import scala.jdk.OptionConverters._

class BlockCAT extends BaseEntityBlock(BlockBehaviour.Properties.of(ModObjects.MATERIAL).strength(0.7f)) {
  // Chest as Tank
  final val registryName = new ResourceLocation(FluidTank.modID, BlockCAT.NAME)
  val itemBlock = new BlockItem(this, new Item.Properties())
  registerDefaultState(this.getStateDefinition.any.setValue(FACING, Direction.NORTH))

  override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit = builder.add(FACING)

  //noinspection ScalaDeprecation,deprecation
  override final def getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = new CATTile(pos, state)

  override def getStateForPlacement(context: BlockPlaceContext): BlockState = this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection)

  //noinspection ScalaDeprecation,deprecation
  override def use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult = {
    val stack = player.getItemInHand(hand)
    if (!player.isCrouching) {
      val result = for {
        cat <- level.getBlockEntity(pos, ModObjects.CAT_TYPE).toScala
        if !stack.isEmpty
        catHandler <- cat.getFluidHandler(state.getValue(FACING))
        r <- if (!level.isClientSide) BucketEventHandler.transferFluid(catHandler, catHandler.getFluidAmountStream.findFirst().orElse(FluidAmount.EMPTY), stack)
        else BucketEventHandler.checkStack(stack)
        if r.result.isSuccess
      } yield r
      result match {
        case Some(value) =>
          if (!level.isClientSide) BucketEventHandler.setItem(level, pos, player, hand, value, stack)
          InteractionResult.SUCCESS
        case _ =>
          if (!level.isClientSide)
            player.openMenu(level.getBlockEntity(pos).asInstanceOf[CATTile])
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
