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
    // Bucket filling code is moved to BucketEventHandler and using event.
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

  override final def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit = {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
    worldIn.getTileEntity(pos) match {
      case tank: TileTankNoDisplay => if (!worldIn.isRemote) tank.onBlockPlacedBy()
      case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile)
    }
  }

  override final def hasComparatorInputOverride(state: IBlockState): Boolean = true

  override final def getComparatorInputOverride(blockState: IBlockState, worldIn: World, pos: BlockPos): Int = {
    worldIn.getTileEntity(pos) match {
      case tileTank: TileTankNoDisplay => tileTank.getComparatorLevel
      case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile); 0
    }
  }

  override final def onReplaced(state: IBlockState, worldIn: World, pos: BlockPos, newState: IBlockState, isMoving: Boolean): Unit = {
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

  override final def getPickBlock(state: IBlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: EntityPlayer) = {
    val stack = super.getPickBlock(state, target, world, pos, player)
    saveTankNBT(world.getTileEntity(pos), stack)
    stack
  }

  override final def harvestBlock(worldIn: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, te: TileEntity, stack: ItemStack): Unit = {
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
