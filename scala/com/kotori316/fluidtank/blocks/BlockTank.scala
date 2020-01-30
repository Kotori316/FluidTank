package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tiers, TileTank, TileTankNoDisplay}
import com.kotori316.fluidtank.{Config, FluidTank}

class BlockTank(val rank: Int, defaultTier: Tiers) extends AbstractTank {

  final val itemBlock = new ItemBlockTank(this, rank)
  final lazy val visibleProperty = PropertyBool.create("visible")

  def getTierByMeta(meta: Int): Tiers = defaultTier

  setRegistryName(FluidTank.modID, "blocktank" + rank)
  setUnlocalizedName(FluidTank.modID + ".blocktank" + rank)
  itemBlock.setRegistryName(FluidTank.modID, "blocktank" + rank)
  setDefaultState(blockState.getBaseState.withProperty(visibleProperty, Boolean.box(true)))

  override final def createNewTileEntity(worldIn: World, meta: Int) =
    if ((meta & 8) != 8) new TileTank(getTierByMeta(meta)) else new TileTankNoDisplay(getTierByMeta(meta))

  override def getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack = {
    val stack = super.getPickBlock(state, target, world, pos, player)
    saveTankNBT(world.getTileEntity(pos), stack)
    stack
  }

  private def saveTankNBT(tileEntity: TileEntity, stack: ItemStack): Unit = {
    Option(tileEntity).collect { case tank: TileTankNoDisplay if tank.hasContent => tank.getBlockTag }
      .foreach(tag => stack.setTagInfo(TileTankNoDisplay.NBT_BlockTag, tag))
    Option(tileEntity).collect { case tank: TileTankNoDisplay => tank.getStackName }.flatten
      .foreach(stack.setStackDisplayName)
  }

  override def harvestBlock(worldIn: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, te: TileEntity, stack: ItemStack): Unit = {
    player.addStat(StatList.getBlockStats(this))
    player.addExhaustion(0.005F)
    harvesters.set(player)
    if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockStates, prevents item dupe
      val blockStack = new ItemStack(this, 1, damageDropped(state))
      saveTankNBT(te, blockStack)
      val list = new java.util.ArrayList[ItemStack]()
      list.add(blockStack)
      val i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack)
      val chance = ForgeEventFactory.fireBlockHarvesting(list, worldIn, pos, state, i, 1.0f, false, harvesters.get)
      for (drop <- list) {
        if (worldIn.rand.nextFloat <= chance) Block.spawnAsEntity(worldIn, pos, drop)
      }
    }
    harvesters.set(null)
  }

  override def damageDropped(state: IBlockState): Int = if (state.getValue(visibleProperty)) 0 else 8

  override def getStateFromMeta(meta: Int) = this.getDefaultState.withProperty(visibleProperty, Boolean.box((meta & 8) == 0))

  override def createBlockState(): BlockStateContainer = new BlockStateContainer(this, Seq(visibleProperty): _*)

  override def getSubBlocks(itemIn: CreativeTabs, items: NonNullList[ItemStack]): Unit = {
    super.getSubBlocks(itemIn, items)
    if (Config.content.showInvisibleTank) {
      items.add(new ItemStack(this, 1, 8))
    }
  }
}

object BlockTank {
  val blockTank1 = new BlockTank(1, Tiers.WOOD)
  val blockTank2 = new BlockTankVariants(2, Tiers.STONE)
  val blockTank3 = new BlockTankVariants(3, Tiers.IRON)
  val blockTank4 = new BlockTank(4, Tiers.GOLD)
  val blockTank5 = new BlockTank(5, Tiers.DIAMOND)
  val blockTank6 = new BlockTank(6, Tiers.EMERALD)
  val blockTank7 = new BlockTank(7, Tiers.STAR)
  val blockTankCreative = new BlockTankCreative
}
