package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tiers, TileTank, TileTankNoDisplay}
import net.minecraft.block.{Block, BlockRenderType, BlockState}
import net.minecraft.enchantment.{EnchantmentHelper, Enchantments}
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.stats.Stats
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult, RayTraceResult}
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.{BlockRenderLayer, Direction, Hand, NonNullList}
import net.minecraft.world.{IBlockReader, World}
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

  override def hasTileEntity(state: BlockState) = true

  override def createTileEntity(state: BlockState, world: IBlockReader): TileTankNoDisplay = {
    new TileTank(tier)
  }

  override def onBlockActivated(state: BlockState, worldIn: World, pos: BlockPos, playerIn: PlayerEntity, handIn: Hand, hit: BlockRayTraceResult) = {
    // Bucket filling code is moved to BucketEventHandler and using event.
    if (playerIn.getHeldItemMainhand.isEmpty) {
      if (!worldIn.isRemote) {
        worldIn.getTileEntity(pos) match {
          case tileTank: TileTankNoDisplay => playerIn.sendStatusMessage(new StringTextComponent(tileTank.connection.toString), true)
          case tile => FluidTank.LOGGER.error("There is not TileTank at the pos : " + pos + " but " + tile)
        }
      }
      true
    } else {
      false
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

  protected def saveTankNBT(tileEntity: TileEntity, stack: ItemStack): Unit = {
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

  override final def harvestBlock(worldIn: World, player: PlayerEntity, pos: BlockPos, state: BlockState, te: TileEntity, stack: ItemStack): Unit = {
    player.addStat(Stats.BLOCK_MINED.get(this))
    player.addExhaustion(0.005F)

    if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockStates, prevents item dupe
      val blockStack = new ItemStack(this, 1)
      saveTankNBT(te, blockStack)
      val list = NonNullList.create[ItemStack]()
      list.add(blockStack)
      val i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack)
      val chance = ForgeEventFactory.fireBlockHarvesting(list, worldIn, pos, state, i, 1.0f, false, player)
      list.forEach(drop => if (worldIn.rand.nextFloat <= chance) Block.spawnAsEntity(worldIn, pos, drop))
    }
  }

  override def getShape(state: BlockState, worldIn: IBlockReader, pos: BlockPos, context: ISelectionContext) = ModObjects.TANK_SHAPE

  override def getCollisionShape(state: BlockState, worldIn: IBlockReader, pos: BlockPos, context: ISelectionContext) = ModObjects.TANK_SHAPE
}
