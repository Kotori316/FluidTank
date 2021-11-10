package com.kotori316.fluidtank.milk

import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.world.item.{Item, Items}
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.{Fluid, FluidState}
import net.minecraft.world.level.{BlockGetter, LevelReader}
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.{Shapes, VoxelShape}

class MilkFluid extends Fluid {

  override def getBucket: Item = Items.MILK_BUCKET

  override def canBeReplacedWith(fluidState: FluidState, blockGetter: BlockGetter, blockPos: BlockPos, fluid: Fluid, direction: Direction): Boolean = false

  override def getFlow(blockGetter: BlockGetter, blockPos: BlockPos, fluidState: FluidState): Vec3 = Vec3.ZERO

  override def getTickDelay(levelReader: LevelReader): Int = 0

  override def getExplosionResistance: Float = 0F

  override def getHeight(fluidState: FluidState, blockGetter: BlockGetter, blockPos: BlockPos): Float = 0F

  override def getOwnHeight(fluidState: FluidState): Float = 0F

  override def createLegacyBlock(fluidState: FluidState): BlockState = Blocks.AIR.defaultBlockState()

  override def isSource(fluidState: FluidState): Boolean = false

  override def getAmount(fluidState: FluidState): Int = 0

  override def getShape(fluidState: FluidState, blockGetter: BlockGetter, blockPos: BlockPos): VoxelShape = Shapes.empty

  //
  //  override def createAttributes(): FluidAttributes = {
  //    FluidAttributes.builder(
  //      new ResourceLocation(FluidTank.modID, "blocks/milk_still"),
  //      new ResourceLocation(FluidTank.modID, "blocks/milk_flow")
  //    )
  //      .translationKey(s"block.${FluidTank.modID}.milk")
  //      .sound(SoundEvents.ITEM_BUCKET_FILL, SoundEvents.ITEM_BUCKET_EMPTY)
  //      .build(this)
  //  }
}

object MilkFluid {
  final val NAME = "vanilla_milk"
}
