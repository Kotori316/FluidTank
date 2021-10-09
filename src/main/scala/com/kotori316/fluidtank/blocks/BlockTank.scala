package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tier, TileTank}
import net.minecraft.block.{AbstractBlock, Block, BlockState}
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.state.StateContainer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.shapes.{ISelectionContext, VoxelShape}
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult, RayTraceResult}
import net.minecraft.util.{ActionResultType, Direction, Hand}
import net.minecraft.world.{IBlockReader, World}
import net.minecraftforge.fluids.{FluidStack, FluidUtil}
import net.minecraftforge.items.ItemHandlerHelper

class BlockTank(val tier: Tier) extends Block(AbstractBlock.Properties.create(ModObjects.MATERIAL).hardnessAndResistance(1f).notSolid()) {

  /*
  RegistryName will be "fluidtank:tank_wood".
  Invisible tank -> "fluidtank:invisible_tank_wood"
  Creative Tank -> "fluidtank:creative"
   */
  setRegistryName(FluidTank.modID, namePrefix + tier.toString.toLowerCase)
  setDefaultState(this.stateContainer.getBaseState.`with`(TankPos.TANK_POS_PROPERTY, TankPos.SINGLE))
  val itemBlock = new ItemBlockTank(this)

  override final def asItem(): Item = itemBlock

  def namePrefix = "tank_"

  override def hasTileEntity(state: BlockState) = true

  override def createTileEntity(state: BlockState, world: IBlockReader): TileEntity = {
    new TileTank(tier)
  }

  //noinspection ScalaDeprecation
  override def onBlockActivated(state: BlockState, worldIn: World, pos: BlockPos, playerIn: PlayerEntity, handIn: Hand, hit: BlockRayTraceResult): ActionResultType = {
    worldIn.getTileEntity(pos) match {
      case tileTank: TileTank =>
        val stack = playerIn.getHeldItem(handIn)
        if (playerIn.getHeldItemMainhand.isEmpty) {
          if (!worldIn.isRemote) {
            playerIn.sendStatusMessage(tileTank.connection.getTextComponent, true)
          }
          ActionResultType.SUCCESS
        } else if (!stack.getItem.isInstanceOf[ItemBlockTank]) {
          val copiedStack = if (stack.getCount == 1) stack else ItemHandlerHelper.copyStackWithSize(stack, 1)
          FluidUtil.getFluidHandler(copiedStack).asScala.value.value match {
            case Some(handlerItem) if !stack.isEmpty =>
              if (!worldIn.isRemote) {
                val tankHandler = tileTank.connection.handler
                BucketEventHandler.transferFluid(worldIn, pos, playerIn, handIn, tileTank.connection.getFluidStack.map(_.setAmount(Int.MaxValue)).map(_.toStack).getOrElse(FluidStack.EMPTY), stack, handlerItem, tankHandler)
              }
              ActionResultType.SUCCESS
            case _ => ActionResultType.PASS
          }
        } else {
          ActionResultType.PASS
        }
      case tile => FluidTank.LOGGER.error(ModObjects.MARKER_BlockTank, "There is not TileTank at the pos : " + pos + " but " + tile); ActionResultType.PASS
    }
  }

  //  override final def getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  //noinspection ScalaDeprecation
  override final def isSideInvisible(state: BlockState, adjacentBlockState: BlockState, side: Direction) = true

  override final def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: BlockState, placer: LivingEntity, stack: ItemStack): Unit = {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
    worldIn.getTileEntity(pos) match {
      case tank: TileTank => if (!worldIn.isRemote) tank.onBlockPlacedBy()
      case tile => FluidTank.LOGGER.error(ModObjects.MARKER_BlockTank, "There is not TileTank at the pos : " + pos + " but " + tile)
    }
  }

  //noinspection ScalaDeprecation
  override final def hasComparatorInputOverride(state: BlockState): Boolean = true

  //noinspection ScalaDeprecation
  override final def getComparatorInputOverride(blockState: BlockState, worldIn: World, pos: BlockPos): Int = {
    worldIn.getTileEntity(pos) match {
      case tileTank: TileTank => tileTank.getComparatorLevel
      case tile => FluidTank.LOGGER.error(ModObjects.MARKER_BlockTank, "There is not TileTank at the pos : " + pos + " but " + tile); 0
    }
  }

  //noinspection ScalaDeprecation
  override final def onReplaced(state: BlockState, worldIn: World, pos: BlockPos, newState: BlockState, isMoving: Boolean): Unit = {
    if (!state.isIn(newState.getBlock)) {
      worldIn.getTileEntity(pos) match {
        case tank: TileTank => tank.onDestroy()
        case tile => FluidTank.LOGGER.error(ModObjects.MARKER_BlockTank, "There is not TileTank at the pos : " + pos + " but " + tile)
      }
      worldIn.removeTileEntity(pos)
    }
  }

  def saveTankNBT(tileEntity: TileEntity, stack: ItemStack): Unit = {
    Option(tileEntity).collect { case tank: TileTank if tank.hasContent => tank.getBlockTag }
      .foreach(tag => stack.setTagInfo(TileTank.NBT_BlockTag, tag))
    Option(tileEntity).collect { case tank: TileTank => tank.getStackName }.flatten
      .foreach(stack.setDisplayName)
  }

  override final def getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: PlayerEntity): ItemStack = {
    val stack = super.getPickBlock(state, target, world, pos, player)
    saveTankNBT(world.getTileEntity(pos), stack)
    stack
  }

  //noinspection ScalaDeprecation
  override def getShape(state: BlockState, worldIn: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape = ModObjects.TANK_SHAPE

  override def fillStateContainer(builder: StateContainer.Builder[Block, BlockState]): Unit = {
    super.fillStateContainer(builder)
    builder.add(TankPos.TANK_POS_PROPERTY)
  }
}
