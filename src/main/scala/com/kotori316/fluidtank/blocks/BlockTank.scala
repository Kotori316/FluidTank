package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.network.SideProxy
import com.kotori316.fluidtank.tiles.{Tiers, TileTank, TileTankNoDisplay}
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.{EntityLiving, EntityLivingBase, EntitySpawnPlacementRegistry, EntityType}
import net.minecraft.init.{Enchantments, Items}
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.tileentity.TileEntity
import net.minecraft.util._
import net.minecraft.util.math.{BlockPos, RayTraceResult}
import net.minecraft.util.text.TextComponentString
import net.minecraft.world.{IBlockReader, IWorldReaderBase, World}
import net.minecraftforge.event.ForgeEventFactory

class BlockTank(val tier: Tiers) extends Block(Block.Properties.create(ModObjects.MATERIAL).hardnessAndResistance(1f)) {

  /*
  RegistryName will be "fluidtank:tank_wood".
  Invisible tank -> "fluidtank:invisible_tank_wood"
  Creative Tank -> "fluidtank:creative"
   */
  setRegistryName(FluidTank.modID, namePrefix + tier.toString.toLowerCase)
  val itemBlock = new ItemBlockTank(this)

  def namePrefix = "tank_"

  override final def hasTileEntity(state: IBlockState) = true

  override def createTileEntity(state: IBlockState, world: IBlockReader): TileTankNoDisplay = {
    new TileTank(tier)
  }

  override def onBlockActivated(state: IBlockState, worldIn: World, pos: BlockPos, playerIn: EntityPlayer,
                                hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    val stack = playerIn.getHeldItem(hand)
    // 1.12.2 code
    /*var returnFlag = false

    for (stackHandler <- FluidUtil.getFluidHandler(if (stack.getCount == 1) stack else ItemHandlerHelper.copyStackWithSize(stack, 1)).asScala;
         tileTank <- Option(worldIn.getTileEntity(pos).asInstanceOf[TileTankNoDisplay])
         if !stack.getItem.isInstanceOf[ItemBlockTank]
    ) {
      if (SideProxy.isServer(tileTank)) {
        for (tankHandler <- tileTank.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing).asScala;
             itemHandler <- playerIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP).asScalaIterator
        ) {
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
      }
      returnFlag = true
    }
    if (returnFlag)
      return true*/

    //1.13.2 code
    val stackFluid = FluidAmount.fromItem(stack)
    // Filling tank.
    if (stackFluid.nonEmpty) {
      val tileTank = worldIn.getTileEntity(pos).asInstanceOf[TileTankNoDisplay]
      if (SideProxy.isServer(tileTank)) {
        val fillAmount = tileTank.connection.handler.fill(stackFluid, doFill = false, min = FluidAmount.AMOUNT_BUCKET)
        if (fillAmount.nonEmpty) {
          tileTank.connection.handler.fill(stackFluid, doFill = true, min = FluidAmount.AMOUNT_BUCKET)
          if (!playerIn.abilities.isCreativeMode) {
            stack.shrink(1)
            if (!playerIn.addItemStackToInventory(stack.getContainerItem)) {
              playerIn.dropItem(stack.getContainerItem, false)
            }
          }
        }
      }
      return true
    }
    // Drain fluid from tank.
    if (stack.getItem == Items.BUCKET) {
      val tileTank = worldIn.getTileEntity(pos).asInstanceOf[TileTankNoDisplay]
      if (SideProxy.isServer(tileTank)) {
        val drained = tileTank.connection.handler.drain(FluidAmount.EMPTY.setAmount(FluidAmount.AMOUNT_BUCKET), doDrain = false)
        if (drained.nonEmpty) {
          tileTank.connection.handler.drain(FluidAmount.EMPTY.setAmount(FluidAmount.AMOUNT_BUCKET), doDrain = true)
          if (!playerIn.abilities.isCreativeMode) {
            stack.shrink(1)
            if (!playerIn.addItemStackToInventory(new ItemStack(drained.fluid.getFilledBucket))) {
              playerIn.dropItem(new ItemStack(drained.fluid.getFilledBucket), false)
            }
          }
        }
      }
      return true
    }

    if (playerIn.getHeldItemMainhand.isEmpty) {
      if (!worldIn.isRemote) {
        worldIn.getTileEntity(pos) match {
          case tileTank: TileTankNoDisplay => playerIn.sendStatusMessage(new TextComponentString(tileTank.connection.toString), true)
          case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile)
        }
      }
      true
    } else {
      false
    }
  }

  override final def getRenderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT

  override final def getRenderType(state: IBlockState): EnumBlockRenderType = EnumBlockRenderType.MODEL

  override final def isFullCube(state: IBlockState) = false

  override final def getCollisionShape(state: IBlockState, worldIn: IBlockReader, pos: BlockPos) = ModObjects.TANK_SHAPE

  override final def getShape(state: IBlockState, worldIn: IBlockReader, pos: BlockPos) = ModObjects.TANK_SHAPE

  override final def isSideInvisible(state: IBlockState, adjacentBlockState: IBlockState, side: EnumFacing) = true

  override final def canCreatureSpawn(state: IBlockState, world: IWorldReaderBase, pos: BlockPos,
                                      t: EntitySpawnPlacementRegistry.SpawnPlacementType, entityType: EntityType[_ <: EntityLiving]) = {
    false
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

  override def onReplaced(state: IBlockState, worldIn: World, pos: BlockPos, newState: IBlockState, isMoving: Boolean): Unit = {
    worldIn.getTileEntity(pos) match {
      case tank: TileTankNoDisplay => tank.onDestroy()
      case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile)
    }
    worldIn.removeTileEntity(pos)
  }

  protected def saveTankNBT(tileEntity: TileEntity, stack: ItemStack): Unit = {
    Option(tileEntity).collect { case tank: TileTankNoDisplay if tank.hasContent => tank.getBlockTag }
      .foreach(tag => stack.setTagInfo(TileTankNoDisplay.NBT_BlockTag, tag))
    Option(tileEntity).collect { case tank: TileTankNoDisplay => tank.getStackName }.flatten
      .foreach(stack.setDisplayName)
  }

  override def getPickBlock(state: IBlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: EntityPlayer) = {
    val stack = super.getPickBlock(state, target, world, pos, player)
    saveTankNBT(world.getTileEntity(pos), stack)
    stack
  }

  override def harvestBlock(worldIn: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, te: TileEntity, stack: ItemStack): Unit = {
    player.addStat(StatList.BLOCK_MINED.get(this))
    player.addExhaustion(0.005F)
    harvesters.set(player)
    if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockStates, prevents item dupe
      val blockStack = new ItemStack(this, 1)
      saveTankNBT(te, blockStack)
      val list = NonNullList.create[ItemStack]()
      list.add(blockStack)
      val i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack)
      val chance = ForgeEventFactory.fireBlockHarvesting(list, worldIn, pos, state, i, 1.0f, false, harvesters.get)
      list.forEach(drop => if (worldIn.rand.nextFloat <= chance) Block.spawnAsEntity(worldIn, pos, drop))
    }
    harvesters.set(null)
  }
}
