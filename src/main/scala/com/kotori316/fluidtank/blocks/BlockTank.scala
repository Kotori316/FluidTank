package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tier, TileTank}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.{Item, ItemStack}
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.level.block.{Block, EntityBlock}
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.phys.shapes.{CollisionContext, VoxelShape}
import net.minecraft.world.phys.{BlockHitResult, HitResult}
import net.minecraft.world.{InteractionHand, InteractionResult}
import net.minecraftforge.fluids.FluidStack
import org.jetbrains.annotations.Nullable

import scala.annotation.nowarn

class BlockTank(val tier: Tier) extends Block(BlockBehaviour.Properties.of(ModObjects.MATERIAL).strength(1f).dynamicShape())
  with EntityBlock {

  /*
  RegistryName will be "fluidtank:tank_wood".
  Invisible tank -> "fluidtank:invisible_tank_wood"
  Creative Tank -> "fluidtank:creative"
   */
  final val registryName = new ResourceLocation(FluidTank.modID, namePrefix + tier.toString.toLowerCase)
  registerDefaultState(this.getStateDefinition.any.setValue(TankPos.TANK_POS_PROPERTY, TankPos.SINGLE))
  final val itemBlock: ItemBlockTank = createTankItem()

  protected def createTankItem(): ItemBlockTank = new ItemBlockTank(this)

  override final def asItem(): Item = itemBlock

  def namePrefix = "tank_"

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = {
    new TileTank(tier, pos, state)
  }

  //noinspection ScalaDeprecation,deprecation
  override def use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult = {
    level.getBlockEntity(pos) match {
      case tileTank: TileTank =>
        val stack = player.getItemInHand(hand)
        if (player.getMainHandItem.isEmpty) {
          if (!level.isClientSide) {
            player.displayClientMessage(tileTank.connection.getTextComponent, true)
          }
          InteractionResult.SUCCESS
        } else if (!stack.getItem.isInstanceOf[ItemBlockTank]) {
          val result = for {
            r <- if (!level.isClientSide) BucketEventHandler.transferFluid(tileTank.connection.getFluidHandler,
              tileTank.connection.getFluidStack.map(_.setAmount(Int.MaxValue)).map(_.toStack).getOrElse(FluidStack.EMPTY),
              stack)
            else BucketEventHandler.checkStack(stack)
            if r.result.isSuccess
          } yield r
          result match {
            case Some(value) =>
              if (!level.isClientSide) BucketEventHandler.setItem(level, pos, player, hand, value, stack)
              InteractionResult.SUCCESS
            case _ => InteractionResult.PASS
          }
        } else {
          InteractionResult.PASS
        }
      case tile =>
        FluidTank.LOGGER.error(ModObjects.MARKER_BlockTank, "There is not TileTank at the pos : " + pos + " but " + tile)
        InteractionResult.PASS
    }
  }

  //  override final def getRenderType(state: BlockState): RenderShape = RenderShape.MODEL

  //noinspection ScalaDeprecation,deprecation
  override final def skipRendering(state: BlockState, adjacentBlockState: BlockState, side: Direction) = true

  override def setPlacedBy(level: Level, pos: BlockPos, state: BlockState, @Nullable entity: LivingEntity, stack: ItemStack): Unit = {
    super.setPlacedBy(level, pos, state, entity, stack)
    level.getBlockEntity(pos) match {
      case tank: TileTank => if (!level.isClientSide) tank.onBlockPlacedBy()
      case tile => FluidTank.LOGGER.error(ModObjects.MARKER_BlockTank, "There is not TileTank at the pos : " + pos + " but " + tile)
    }
  }

  //noinspection ScalaDeprecation,deprecation
  override final def hasAnalogOutputSignal(state: BlockState): Boolean = true

  //noinspection ScalaDeprecation,deprecation
  override final def getAnalogOutputSignal(blockState: BlockState, level: Level, pos: BlockPos): Int = {
    level.getBlockEntity(pos) match {
      case tileTank: TileTank => tileTank.getComparatorLevel
      case tile => FluidTank.LOGGER.error(ModObjects.MARKER_BlockTank, "There is not TileTank at the pos : " + pos + " but " + tile); 0
    }
  }

  @nowarn //noinspection ScalaDeprecation,deprecation
  override final def onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, moved: Boolean): Unit = {
    if (!state.is(newState.getBlock)) {
      level.getBlockEntity(pos) match {
        case tank: TileTank => tank.onDestroy()
        case tile => FluidTank.LOGGER.error(ModObjects.MARKER_BlockTank, "There is not TileTank at the pos : " + pos + " but " + tile)
      }
      super.onRemove(state, level, pos, newState, moved)
    }
  }

  def saveTankNBT(tileEntity: BlockEntity, stack: ItemStack): Unit = {
    Option(tileEntity).collect { case tank: TileTank if tank.hasContent => tank.getBlockTag }
      .foreach(Utils.setTileTag(stack, _))
    Option(tileEntity).collect { case tank: TileTank => tank.getStackName }.flatten
      .foreach(stack.setHoverName)
  }

  override final def getCloneItemStack(state: BlockState, target: HitResult, level: BlockGetter, pos: BlockPos, player: Player): ItemStack = {
    val stack = super.getCloneItemStack(state, target, level, pos, player)
    saveTankNBT(level.getBlockEntity(pos), stack)
    stack
  }

  //noinspection ScalaDeprecation,deprecation
  override def getShape(state: BlockState, worldIn: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = ModObjects.TANK_SHAPE

  override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit = {
    super.createBlockStateDefinition(builder)
    builder.add(TankPos.TANK_POS_PROPERTY)
  }
}
