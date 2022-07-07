package com.kotori316.fluidtank

import com.kotori316.fluidtank.blocks._
import com.kotori316.fluidtank.items.ReservoirItem
import com.kotori316.fluidtank.tiles._
import com.kotori316.fluidtank.transport.{FluidPipeBlock, ItemPipeBlock, ItemPipeTile, PipeTile}
import com.mojang.datafixers.DSL
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.core.{BlockPos, Registry}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.{Material, MaterialColor, PushReaction}
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.Shapes
import org.apache.logging.log4j.MarkerManager

import scala.reflect.ClassTag

object ModObjects {
  //---------- Objects used in block and items ----------

  final val CREATIVE_TABS = FabricItemGroupBuilder.build(
    new ResourceLocation(FluidTank.modID, FluidTank.modID), () => new ItemStack(ModObjects.tierToBlock(Tier.WOOD))
  );
  final val MATERIAL = new Material(MaterialColor.NONE, false, true, true, false,
    false, false, PushReaction.BLOCK)
  final val MATERIAL_PIPE = new Material(MaterialColor.NONE, false, false, true, false,
    false, false, PushReaction.BLOCK)
  private[this] final val d = 1 / 16d
  final val BOUNDING_BOX = new AABB(2 * d, 0, 2 * d, 14 * d, 1d, 14 * d)
  final val TANK_SHAPE = Shapes.create(BOUNDING_BOX)

  //---------- BLOCKS ----------

  private[this] final val woodTank = new BlockTank(Tier.WOOD)
  private[this] final val normalTanks = Tier.list.filter(_.hasTagRecipe).map(new BlockTank(_))
  private[this] final val creativeTank = new BlockCreativeTank
  private[this] final val voidTank = new BlockVoidTank
  final val blockTanks = woodTank +: normalTanks.toList :+ creativeTank :+ voidTank
  final val tierToBlock = blockTanks.groupMapReduce(_.tier)(identity) { case (a, _) => a }
  final val blockCat = new BlockCAT
  final val blockFluidPipe = new FluidPipeBlock
  final val blockItemPipe = new ItemPipeBlock
  final val blockSource = new FluidSourceBlock

  //---------- ITEMS ----------
  final val itemReservoirs = List(Tier.WOOD, Tier.STONE, Tier.IRON).map(t => new ReservoirItem(t))

  //---------- TileEntities ----------

  private[this] final var types: List[NamedEntry[BlockEntityType[_ <: BlockEntity]]] = Nil
  final val TANK_TYPE = createTileType((p, s) => new TileTank(p, s), blockTanks)
  final val TANK_CREATIVE_TYPE = createTileType((p, s) => new TileTankCreative(p, s), List(creativeTank))
  final val TANK_VOID_TYPE = createTileType((p, s) => new TileTankVoid(p, s), List(voidTank))
  final val CAT_TYPE = createTileType((p, s) => new CATTile(p, s), List(blockCat))
  final val FLUID_PIPE_TYPE = createTileType((p, s) => new PipeTile(p, s), List(blockFluidPipe))
  final val ITEM_PIPE_TYPE = createTileType((p, s) => new ItemPipeTile(p, s), List(blockItemPipe))
  final val SOURCE_TYPE = createTileType((p, s) => new FluidSourceTile(p, s), List(blockSource))

  def createTileType[T <: BlockEntity](supplier: (BlockPos, BlockState) => T, blocks: Seq[Block])(implicit tag: ClassTag[T]): BlockEntityType[T] = {
    val t = BlockEntityType.Builder.of[T]((p, s) => supplier(p, s), blocks: _*).build(DSL.emptyPartType())
    types = new NamedEntry(new ResourceLocation(FluidTank.modID, tag.runtimeClass.getSimpleName.toLowerCase), t) :: types
    t
  }

  def getTileTypes: List[NamedEntry[BlockEntityType[_ <: BlockEntity]]] = types

  //---------- Containers ----------

  final val CAT_CONTAINER_TYPE = CATContainer.makeType()

  //---------- LootFunction ----------
  final val TANK_CONTENT_LOOT = Registry.register(Registry.LOOT_FUNCTION_TYPE,
    new ResourceLocation(FluidTank.modID, "content_tank"),
    new LootItemFunctionType(new ContentTankSerializer))

  // ---------- Markers ----------
  final val MARKER_BlockTank = MarkerManager.getMarker("BlockTank")
  final val MARKER_TileTank = MarkerManager.getMarker("TileTank")
  final val MARKER_RenderItemTank = MarkerManager.getMarker("RenderItemTank")
  final val MARKER_Connection = MarkerManager.getMarker("Connection")
  final val MARKER_PipeTileBase = MarkerManager.getMarker("PipeTileBase")
  final val MARKER_TankHandler = MarkerManager.getMarker("TankHandler")
  final val MARKER_ListTankHandler = MarkerManager.getMarker("ListTankHandler")
  final val MARKER_DebugFluidHandler = MarkerManager.getMarker("DebugFluidHandler")

  class NamedEntry[+T](val name: ResourceLocation, val t: T)
}
