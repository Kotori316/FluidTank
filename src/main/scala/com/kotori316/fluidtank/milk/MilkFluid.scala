package com.kotori316.fluidtank.milk

import com.kotori316.fluidtank.FluidTank
import net.minecraft.block.{BlockState, Blocks}
import net.minecraft.fluid.{Fluid, FluidState}
import net.minecraft.item.{Item, Items}
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.{VoxelShape, VoxelShapes}
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.{Direction, ResourceLocation, SoundEvents}
import net.minecraft.world.{IBlockReader, IWorldReader}
import net.minecraftforge.fluids.FluidAttributes

class MilkFluid extends Fluid {
  setRegistryName(FluidTank.modID, MilkFluid.NAME)

  override def getFilledBucket: Item = Items.MILK_BUCKET

  override def canDisplace(s: FluidState, w: IBlockReader, p: BlockPos, f: Fluid, d: Direction): Boolean = true

  override def getFlow(w: IBlockReader, p: BlockPos, s: FluidState): Vector3d = Vector3d.ZERO

  override def getTickRate(w: IWorldReader): Int = 0

  override def getExplosionResistance: Float = 0F

  override def getActualHeight(s: FluidState, w: IBlockReader, p: BlockPos): Float = 0F

  override def getHeight(s: FluidState): Float = 0F

  override def getBlockState(state: FluidState): BlockState = Blocks.AIR.getDefaultState

  override def isSource(state: FluidState): Boolean = false

  override def getLevel(state: FluidState): Int = 0

  override def func_215664_b(s: FluidState, w: IBlockReader, p: BlockPos): VoxelShape = VoxelShapes.empty

  override def createAttributes(): FluidAttributes = {
    FluidAttributes.builder(
      new ResourceLocation(FluidTank.modID, "blocks/milk_still"),
      new ResourceLocation(FluidTank.modID, "blocks/milk_flow")
    )
      .translationKey(s"block.${FluidTank.modID}.milk")
      .sound(SoundEvents.ITEM_BUCKET_FILL, SoundEvents.ITEM_BUCKET_EMPTY)
      .build(this)
  }
}

object MilkFluid {
  final val NAME = "vanilla_milk"
}
