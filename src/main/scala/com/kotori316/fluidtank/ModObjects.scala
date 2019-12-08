package com.kotori316.fluidtank

import com.kotori316.fluidtank.blocks.{BlockCAT, BlockCreativeTank, BlockInvisibleTank, BlockTank}
import com.kotori316.fluidtank.milk.MilkFluid
import com.kotori316.fluidtank.tiles._
import com.kotori316.fluidtank.transport.{PipeBlock, PipeTile}
import net.minecraft.block.Block
import net.minecraft.block.material.{Material, MaterialColor, PushReaction}
import net.minecraft.item.{ItemGroup, ItemStack}
import net.minecraft.tileentity.{TileEntity, TileEntityType}
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.shapes.VoxelShapes

import scala.reflect.ClassTag

object ModObjects {
  //---------- Objects used in block and items ----------

  final val CREATIVE_TABS = new ItemGroup(FluidTank.MOD_NAME) {
    override def createIcon = new ItemStack(woodTank)
  }.setTabPath(FluidTank.modID)
  final val MATERIAL = new Material(MaterialColor.AIR, false, true, true, false,
    true, false, false, PushReaction.BLOCK)
  final val MATERIAL_PIPE = new Material(MaterialColor.AIR, false, false, true, false,
    true, false, false, PushReaction.BLOCK)
  private[this] final val d = 1 / 16d
  final val BOUNDING_BOX = new AxisAlignedBB(2 * d, 0, 2 * d, 14 * d, 1d, 14 * d)
  final val TANK_SHAPE = VoxelShapes.create(BOUNDING_BOX)

  //---------- BLOCK ----------

  private[this] final val woodTank = new BlockTank(Tiers.WOOD)
  private[this] final val woodTankInvisible = new BlockInvisibleTank(Tiers.WOOD)
  private[this] final val normalTanks = Tiers.list.filter(_.hasTagRecipe).map(new BlockTank(_))
  private[this] final val normalTanksInv = Tiers.list.filter(_.hasTagRecipe).map(new BlockInvisibleTank(_))
  private[this] final val creativeTank = new BlockCreativeTank
  final val blockTanks = woodTank +: normalTanks.toList :+ creativeTank
  final val blockTanksInvisible = woodTankInvisible :: normalTanksInv.toList
  final val blockCat = new BlockCAT
  final val blockPipe = new PipeBlock

  //---------- TileEntities ----------

  private[this] final var types: List[TileEntityType[_ <: TileEntity]] = Nil
  final val TANK_TYPE = createTileType(() => new TileTank, blockTanks)
  final val TANK_NO_DISPLAY_TYPE = createTileType(() => new TileTankNoDisplay, blockTanksInvisible)
  final val TANK_CREATIVE_TYPE = createTileType(() => new TileTankCreative, List(creativeTank))
  final val CAT_TYPE = createTileType(() => new CATTile, List(blockCat))
  final val PIPE_TYPE = createTileType(() => new PipeTile, List(blockPipe))

  def createTileType[T <: TileEntity](supplier: () => T, blocks: Seq[Block])(implicit tag: ClassTag[T]): TileEntityType[T] = {
    val t = TileEntityType.Builder.create[T](() => supplier(), blocks: _*).build(null)
    t.setRegistryName(FluidTank.modID, tag.runtimeClass.getSimpleName.toLowerCase)
    types = t :: types
    t
  }

  def getTileTypes = types

  //---------- Containers ----------

  final val CAT_CONTAINER_TYPE = CATContainer.makeType()

  //---------- Fluids ----------
  final val MILK_FLUID = new MilkFluid
  final val MILK_TAG = new net.minecraft.tags.FluidTags.Wrapper(new ResourceLocation("minecraft:milk"))
}
