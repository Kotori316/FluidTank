package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.tiles.FluidSourceTile
import net.minecraft.block.material.Material
import net.minecraft.block.{Block, BlockRenderType, BlockState, ContainerBlock}
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item._
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult, RayTraceResult}
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.util.{Hand, NonNullList}
import net.minecraft.world.{IBlockReader, World}

class FluidSourceBlock extends ContainerBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(0.5f)) {
  setRegistryName(FluidTank.modID, FluidSourceBlock.NAME)
  val itemBlock = new BlockItem(this, new Item.Properties().group(ModObjects.CREATIVE_TABS))
  itemBlock.setRegistryName(FluidTank.modID, FluidSourceBlock.NAME)

  override final def getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  override def createNewTileEntity(worldIn: IBlockReader): TileEntity = ModObjects.SOURCE_TYPE.create()

  override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: BlockState, placer: LivingEntity, stack: ItemStack): Unit = {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
    Option(worldIn.getTileEntity(pos)).foreach {
      case s: FluidSourceTile =>
        if (Option(stack.getTag).exists(_.contains("unlocked"))) {
          s.locked = false
        } else {
          s.fluid = FluidAmount.BUCKET_WATER
        }
    }
  }

  override def getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: PlayerEntity) = {
    val stack = super.getPickBlock(state, target, world, pos, player)
    if (Option(world.getTileEntity(pos)).collect { case s: FluidSourceTile => !s.locked }.getOrElse(false)) {
      stack.getOrCreateTag().putBoolean("unlocked", true)
    }
    stack
  }

  override def fillItemGroup(group: ItemGroup, items: NonNullList[ItemStack]): Unit = {
    val stack = new ItemStack(this)
    stack.getOrCreateTag().putBoolean("unlocked", true)
    items.add(stack)
  }

  override def onBlockActivated(state: BlockState, worldIn: World, pos: BlockPos,
                                player: PlayerEntity, handIn: Hand, hit: BlockRayTraceResult): Boolean = {
    val stack = player.getHeldItem(handIn)
    val fluid = FluidAmount.fromItem(stack)
    if (fluid.isEmpty) {
      stack.getItem match {
        case Items.BUCKET =>
          // Reset to empty.
          changeContent(worldIn, pos, FluidAmount.EMPTY, player, handIn == Hand.MAIN_HAND)
          true
        case Items.CLOCK =>
          // Change interval time to push fluid.
          val i = if (handIn == Hand.MAIN_HAND) 1 else -1
          changeInterval(worldIn, pos, stack.getCount * i, player)
          true
        case _ => false
      }
    } else {
      changeContent(worldIn, pos, fluid, player, handIn == Hand.MAIN_HAND)
      true
    }
  }

  def changeContent(world: World, pos: BlockPos, fluid: FluidAmount, player: PlayerEntity, isMainHand: Boolean): Unit = if (!world.isRemote) {
    world.getTileEntity(pos) match {
      case s: FluidSourceTile =>
        if (s.fluid fluidEqual fluid) {
          val replace = if (isMainHand) s.fluid + fluid else s.fluid - fluid
          s.fluid = if (replace.nonEmpty) replace else FluidAmount.EMPTY
        } else if (fluid.isEmpty) {
          s.fluid = FluidAmount.EMPTY
        } else {
          s.fluid = fluid
        }
        if (!s.locked || fluid.fluidEqual(FluidAmount.BUCKET_WATER))
          player.sendStatusMessage(new TranslationTextComponent(FluidSourceBlock.CHANGE_SOURCE, s.fluid), false)
      case _ =>
    }
  }

  def changeInterval(world: World, pos: BlockPos, dt: Int, player: PlayerEntity): Unit = if (!world.isRemote) {
    world.getTileEntity(pos) match {
      case s: FluidSourceTile =>
        s.interval = Math.max(1, s.interval + dt)
        player.sendStatusMessage(new TranslationTextComponent(FluidSourceBlock.CHANGE_INTERVAL, s.interval), false)
      case _ =>
    }
  }
}

object FluidSourceBlock {
  final val NAME = "fluid_source"
  final val CHANGE_SOURCE = "chat.fluidtank.change_source"
  final val CHANGE_INTERVAL = "chat.fluidtank.change_interval"
}
