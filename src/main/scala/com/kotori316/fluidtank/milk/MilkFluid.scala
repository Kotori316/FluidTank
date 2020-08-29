package com.kotori316.fluidtank.milk

import net.minecraft.block.{BlockState, Blocks}
import net.minecraft.fluid.{Fluid, FluidState}
import net.minecraft.item.{Item, Items}
import net.minecraft.util.math.{BlockPos, Direction, Vec3d}
import net.minecraft.util.shape.{VoxelShape, VoxelShapes}
import net.minecraft.world.{BlockView, WorldView}

class MilkFluid extends Fluid {

  override def getBucketItem: Item = Items.MILK_BUCKET

  override def canBeReplacedWith(state: FluidState, world: BlockView, pos: BlockPos, fluid: Fluid, direction: Direction): Boolean = false

  override def getVelocity(world: BlockView, pos: BlockPos, state: FluidState): Vec3d = Vec3d.ZERO

  override def getTickRate(worldView: WorldView): Int = 0

  override def getBlastResistance: Float = 0F

  override def getHeight(fluidState: FluidState, blockView: BlockView, blockPos: BlockPos): Float = 0F

  override def getHeight(fluidState: FluidState): Float = 0F

  override def toBlockState(fluidState: FluidState): BlockState = Blocks.AIR.getDefaultState

  override def isStill(fluidState: FluidState): Boolean = false

  override def getLevel(fluidState: FluidState): Int = 0

  override def getShape(fluidState: FluidState, blockView: BlockView, blockPos: BlockPos): VoxelShape = VoxelShapes.empty

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
