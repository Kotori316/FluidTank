package com.kotori316.fluidtank

import com.kotori316.fluidtank.blocks._
import com.kotori316.fluidtank.items.ReservoirItem
import com.kotori316.fluidtank.milk.MilkFluid
import com.kotori316.fluidtank.tiles._
import com.kotori316.fluidtank.transport.{FluidPipeBlock, ItemPipeBlock, ItemPipeTile, PipeTile}
import com.mojang.datafixers.DSL
import net.minecraft.block.Block
import net.minecraft.block.material.{Material, MaterialColor, PushReaction}
import net.minecraft.item.{ItemGroup, ItemStack}
import net.minecraft.loot.LootFunctionType
import net.minecraft.tags.FluidTags
import net.minecraft.tileentity.{TileEntity, TileEntityType}
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.MarkerManager

import scala.reflect.ClassTag

object ModObjects {
  //---------- Objects used in block and items ----------

  final val CREATIVE_TABS = new ItemGroup(FluidTank.MOD_NAME) {
    override def createIcon = new ItemStack(woodTank)
  }.setTabPath(FluidTank.modID)
  final val MATERIAL = new Material(MaterialColor.AIR, false, true, true, false,
    false, false, PushReaction.BLOCK)
  final val MATERIAL_PIPE = new Material(MaterialColor.AIR, false, false, true, false,
    false, false, PushReaction.BLOCK)
  private[this] final val d = 1 / 16d
  final val BOUNDING_BOX = new AxisAlignedBB(2 * d, 0, 2 * d, 14 * d, 1d, 14 * d)
  final val TANK_SHAPE = VoxelShapes.create(BOUNDING_BOX)

  //---------- BLOCKS ----------

  private[this] final val woodTank = new BlockTank(Tiers.WOOD)
  private[this] final val normalTanks = Tiers.list.filter(_.hasTagRecipe).map(new BlockTank(_))
  private[this] final val creativeTank = new BlockCreativeTank
  private[this] final val voidTank = new BlockVoidTank
  final val blockTanks = woodTank +: normalTanks.toList :+ creativeTank :+ voidTank
  final val blockCat = new BlockCAT
  final val blockFluidPipe = new FluidPipeBlock
  final val blockItemPipe = new ItemPipeBlock
  final val blockSource = new FluidSourceBlock

  //---------- ITEMS ----------
  final val itemReservoirs = List(Tiers.WOOD, Tiers.STONE, Tiers.IRON).map(t => new ReservoirItem(t))

  //---------- TileEntities ----------

  private[this] final var types: List[TileEntityType[_ <: TileEntity]] = Nil
  final val TANK_TYPE = createTileType(() => new TileTank, blockTanks)
  final val TANK_CREATIVE_TYPE = createTileType(() => new TileTankCreative, List(creativeTank))
  final val TANK_VOID_TYPE = createTileType(() => new TileTankVoid, List(voidTank))
  final val CAT_TYPE = createTileType(() => new CATTile, List(blockCat))
  final val FLUID_PIPE_TYPE = createTileType(() => new PipeTile, List(blockFluidPipe))
  final val ITEM_PIPE_TYPE = createTileType(() => new ItemPipeTile, List(blockItemPipe))
  final val SOURCE_TYPE = createTileType(() => new FluidSourceTile, List(blockSource))

  def createTileType[T <: TileEntity](supplier: () => T, blocks: Seq[Block])(implicit tag: ClassTag[T]): TileEntityType[T] = {
    val t = TileEntityType.Builder.create[T](() => supplier(), blocks: _*).build(DSL.emptyPartType())
    t.setRegistryName(FluidTank.modID, tag.runtimeClass.getSimpleName.toLowerCase)
    types = t :: types
    t
  }

  def getTileTypes: List[TileEntityType[_ <: TileEntity]] = types

  //---------- Containers ----------

  final val CAT_CONTAINER_TYPE = CATContainer.makeType()

  //---------- Fluids ----------
  final val MILK_FLUID = new MilkFluid
  final val MILK_TAG = FluidTags.makeWrapperTag("forge:milk")

  //---------- LootFunction ----------
  final val TANK_CONTENT_LOOT = Registry.register(Registry.LOOT_FUNCTION_TYPE,
    new ResourceLocation(FluidTank.modID, "content_tank"),
    new LootFunctionType(new ContentTankSerializer))

  // ---------- Markers ----------
  final val MARKER_BlockTank = MarkerManager.getMarker("BlockTank")
  final val MARKER_RenderItemTank = MarkerManager.getMarker("RenderItemTank")
  final val MARKER_Connection = MarkerManager.getMarker("Connection")
  final val MARKER_PipeTileBase = MarkerManager.getMarker("PipeTileBase")
  final val MARKER_TankHandler = MarkerManager.getMarker("TankHandler")
  final val MARKER_ListTankHandler = MarkerManager.getMarker("ListTankHandler")
  final val MARKER_DebugFluidHandler = MarkerManager.getMarker("DebugFluidHandler")
}
