package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.packet.SideProxy
import com.kotori316.fluidtank.tiles.{Tiers, TileTankNoDisplay}
import com.kotori316.fluidtank.{FluidTank, Utils}
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, ITileEntityProvider}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.{EntityLiving, EntityLivingBase}
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{BlockRenderLayer, EnumBlockRenderType, EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.{FluidStack, FluidUtil}
import net.minecraftforge.items.{CapabilityItemHandler, ItemHandlerHelper}

abstract class AbstractTank extends Block(Utils.MATERIAL) with ITileEntityProvider {

  setCreativeTab(Utils.CREATIVE_TABS)
  setHardness(1.0f)

  import AbstractTank._

  val itemBlock: ItemBlockTank

  def getTierByMeta(meta: Int): Tiers

  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer,
                                hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    val stack = playerIn.getHeldItem(hand)
    val stackHandlerOption = Option(FluidUtil.getFluidHandler(if (stack.getCount == 1) stack else ItemHandlerHelper.copyStackWithSize(stack, 1)))
    var returnFlag = false

    for (stackHandler <- stackHandlerOption;
         tileTank <- Option(worldIn.getTileEntity(pos).asInstanceOf[TileTankNoDisplay])
         if !stack.getItem.isInstanceOf[ItemBlockTank]
         ) {
      if (SideProxy.isServer(tileTank)) {
        val tankHandler = tileTank.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)
        val itemHandler = playerIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)
        val drainAmount = Option(stackHandler.drain(Int.MaxValue, false)).fold(0)(_.amount)
        val resultFill = FluidUtil.tryEmptyContainerAndStow(stack, tankHandler, itemHandler, drainAmount, playerIn, true)
        if (resultFill.isSuccess) {
          playerIn.setHeldItem(hand, resultFill.getResult)
        } else {
          val fillAmount = stackHandler.fill(tileTank.connection.getFluidStack.map(_.copyWithAmount(Int.MaxValue)).orNull, false)
          val resultDrain = FluidUtil.tryFillContainerAndStow(stack, tankHandler, itemHandler, fillAmount, playerIn, true)
          if (resultDrain.isSuccess) {
            playerIn.setHeldItem(hand, resultDrain.getResult)
          }
        }
      }
      returnFlag = true
    }
    if (returnFlag)
      return true

    if (playerIn.getHeldItemMainhand.isEmpty) {
      if (!worldIn.isRemote) {
        worldIn.getTileEntity(pos) match {
          case tileTank: TileTankNoDisplay => playerIn.sendStatusMessage(tileTank.connection.getTextComponent, true)
          case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile)
        }
      }
      true
    } else {
      super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)
    }
  }

  override final def getBlockLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT

  override def getRenderType(state: IBlockState): EnumBlockRenderType = EnumBlockRenderType.MODEL

  override final def isFullCube(state: IBlockState) = false

  override final def isOpaqueCube(state: IBlockState) = false

  override final def hasTileEntity(state: IBlockState) = true

  override final def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos) = Utils.BOUNDING_BOX

  override final def shouldSideBeRendered(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing) = true

  override def canCreatureSpawn(state: IBlockState, world: IBlockAccess, pos: BlockPos, living: EntityLiving.SpawnPlacementType) = false

  override def breakBlock(worldIn: World, pos: BlockPos, state: IBlockState): Unit = {
    worldIn.getTileEntity(pos) match {
      case tank: TileTankNoDisplay => tank.onDestroy()
      case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile)
    }
    super.breakBlock(worldIn, pos, state)
  }

  override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit = {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
    worldIn.getTileEntity(pos) match {
      case tank: TileTankNoDisplay => tank.onBlockPlacedBy()
      case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile)
    }
  }

  override def hasComparatorInputOverride(state: IBlockState): Boolean = true

  override def getComparatorInputOverride(blockState: IBlockState, worldIn: World, pos: BlockPos): Int = {
    worldIn.getTileEntity(pos) match {
      case tileTank: TileTankNoDisplay => tileTank.getComparatorLevel
      case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile); 0
    }
  }

  override def getMetaFromState(state: IBlockState): Int = damageDropped(state)

  override def getStateFromMeta(meta: Int): IBlockState = getDefaultState
}

object AbstractTank {

  implicit class FluidStackHelper(val fluidStack: FluidStack) extends AnyVal {

    def copyWithAmount(amount: Int): FluidStack = {
      if (fluidStack == null)
        null
      else
        new FluidStack(fluidStack, amount)
    }

    def setAmount(amount: Int): FluidStack = {
      fluidStack.amount = amount
      fluidStack
    }

    def isEmpty: Boolean = {
      fluidStack == null || fluidStack.amount <= 0
    }
  }

}