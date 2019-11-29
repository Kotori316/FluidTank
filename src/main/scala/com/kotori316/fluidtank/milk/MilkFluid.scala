package com.kotori316.fluidtank.milk

import com.kotori316.fluidtank.FluidTank
import net.minecraft.block.{BlockState, Blocks}
import net.minecraft.fluid.{Fluid, IFluidState}
import net.minecraft.item.{Item, Items}
import net.minecraft.util.math.shapes.{VoxelShape, VoxelShapes}
import net.minecraft.util.math.{BlockPos, Vec3d}
import net.minecraft.util.{BlockRenderLayer, Direction, ResourceLocation}
import net.minecraft.world.{IBlockReader, IWorldReader}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.fluids.FluidAttributes

class MilkFluid extends Fluid {
  setRegistryName(FluidTank.modID, "vanilla_milk")

  @OnlyIn(Dist.CLIENT)
  override def getRenderLayer = BlockRenderLayer.SOLID

  override def getFilledBucket: Item = Items.MILK_BUCKET

  override def func_215665_a(s: IFluidState, w: IBlockReader, p: BlockPos, f: Fluid, d: Direction): Boolean = true

  override def func_215663_a(w: IBlockReader, p: BlockPos, s: IFluidState): Vec3d = Vec3d.ZERO

  override def getTickRate(w: IWorldReader): Int = 0

  override def getExplosionResistance: Float = 0F

  override def func_215662_a(s: IFluidState, w: IBlockReader, p: BlockPos): Float = 0F

  override def func_223407_a(s: IFluidState): Float = 0F

  override def getBlockState(state: IFluidState): BlockState = Blocks.AIR.getDefaultState

  override def isSource(state: IFluidState): Boolean = false

  override def getLevel(state: IFluidState): Int = 0

  override def func_215664_b(s: IFluidState, w: IBlockReader, p: BlockPos): VoxelShape = VoxelShapes.empty

  override def createAttributes(): FluidAttributes = {
    FluidAttributes.builder(
      new ResourceLocation(FluidTank.modID, "blocks/milk_still"),
      new ResourceLocation(FluidTank.modID, "blocks/milk_flow")
    )
      .translationKey(s"block.${FluidTank.modID}.milk")
      .build(this)
  }
}
