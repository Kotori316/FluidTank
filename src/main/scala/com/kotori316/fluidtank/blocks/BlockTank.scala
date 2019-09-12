package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tiers, TileTank, TileTankNoDisplay}
import net.minecraft.block.{Block, BlockRenderType, BlockState}
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult, RayTraceResult}
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.{BlockRenderLayer, Direction, Hand}
import net.minecraft.world.{IBlockReader, World}
import net.minecraftforge.fluids.{FluidStack, FluidUtil}
import net.minecraftforge.items.ItemHandlerHelper

class BlockTank(val tier: Tiers) extends Block(Block.Properties.create(ModObjects.MATERIAL).hardnessAndResistance(1f)) {

  /*
  RegistryName will be "fluidtank:tank_wood".
  Invisible tank -> "fluidtank:invisible_tank_wood"
  Creative Tank -> "fluidtank:creative"
   */
  setRegistryName(FluidTank.modID, namePrefix + tier.toString.toLowerCase)
  val itemBlock = new ItemBlockTank(this)

  def namePrefix = "tank_"

  override def hasTileEntity(state: BlockState) = true

  override def createTileEntity(state: BlockState, world: IBlockReader): TileTankNoDisplay = {
    new TileTank(tier)
  }

  override def onBlockActivated(state: BlockState, worldIn: World, pos: BlockPos, playerIn: PlayerEntity, handIn: Hand, hit: BlockRayTraceResult) = {
    worldIn.getTileEntity(pos) match {
      case tileTank: TileTankNoDisplay =>
        val stack = playerIn.getHeldItem(handIn)
        if (playerIn.getHeldItemMainhand.isEmpty) {
          if (!worldIn.isRemote) {
            playerIn.sendStatusMessage(new StringTextComponent(tileTank.connection.toString), true)
          }
          true
        } else if (!stack.getItem.isInstanceOf[ItemBlockTank]) {
          val copiedStack = if (stack.getCount == 1) stack else ItemHandlerHelper.copyStackWithSize(stack, 1)
          FluidUtil.getFluidHandler(copiedStack).asScala.value.value match {
            case Some(handlerItem) if !stack.isEmpty =>
              if (!worldIn.isRemote) {
                val tankHandler = tileTank.connection.handler
                val drainAmount = handlerItem.drain(Int.MaxValue, IFluidHandler.FluidAction.SIMULATE).getAmount
                val itemHandler = playerIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).orElseGet(() => EmptyHandler.INSTANCE)
                val resultFill = FluidUtil.tryEmptyContainerAndStow(stack, tankHandler, itemHandler, drainAmount, playerIn, true)
                if (resultFill.isSuccess) {
                  playerIn.setHeldItem(handIn, resultFill.getResult)
                } else {
                  val fillAmount = handlerItem.fill(tileTank.connection.getFluidStack.map(_.setAmount(Int.MaxValue)).map(_.toStack).getOrElse(FluidStack.EMPTY), IFluidHandler.FluidAction.SIMULATE)
                  val resultDrain = FluidUtil.tryFillContainerAndStow(stack, tankHandler, itemHandler, fillAmount, playerIn, true)
                  if (resultDrain.isSuccess) {
                    playerIn.setHeldItem(handIn, resultDrain.getResult)
                  }
                }
              }
              true
            case _ => false
          }
        } else {
          false
        }
      case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile); false
    }
  }

  override final def getRenderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT

  override final def getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  override final def isSideInvisible(state: BlockState, adjacentBlockState: BlockState, side: Direction) = true

  override final def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: BlockState, placer: LivingEntity, stack: ItemStack): Unit = {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
    worldIn.getTileEntity(pos) match {
      case tank: TileTankNoDisplay => if (!worldIn.isRemote) tank.onBlockPlacedBy()
      case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile)
    }
  }

  override final def hasComparatorInputOverride(state: BlockState): Boolean = true

  override final def getComparatorInputOverride(blockState: BlockState, worldIn: World, pos: BlockPos): Int = {
    worldIn.getTileEntity(pos) match {
      case tileTank: TileTankNoDisplay => tileTank.getComparatorLevel
      case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile); 0
    }
  }

  override final def onReplaced(state: BlockState, worldIn: World, pos: BlockPos, newState: BlockState, isMoving: Boolean): Unit = {
    worldIn.getTileEntity(pos) match {
      case tank: TileTankNoDisplay => tank.onDestroy()
      case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile)
    }
    worldIn.removeTileEntity(pos)
  }

  def saveTankNBT(tileEntity: TileEntity, stack: ItemStack): Unit = {
    Option(tileEntity).collect { case tank: TileTankNoDisplay if tank.hasContent => tank.getBlockTag }
      .foreach(tag => stack.setTagInfo(TileTankNoDisplay.NBT_BlockTag, tag))
    Option(tileEntity).collect { case tank: TileTankNoDisplay => tank.getStackName }.flatten
      .foreach(stack.setDisplayName)
  }

  override final def getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: PlayerEntity) = {
    val stack = super.getPickBlock(state, target, world, pos, player)
    saveTankNBT(world.getTileEntity(pos), stack)
    stack
  }

  override def getShape(state: BlockState, worldIn: IBlockReader, pos: BlockPos, context: ISelectionContext) = ModObjects.TANK_SHAPE

  override def getCollisionShape(state: BlockState, worldIn: IBlockReader, pos: BlockPos, context: ISelectionContext) = ModObjects.TANK_SHAPE
}
