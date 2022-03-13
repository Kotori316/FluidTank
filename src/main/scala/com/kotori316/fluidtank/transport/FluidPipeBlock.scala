package com.kotori316.fluidtank.transport

import java.util.Objects

import com.kotori316.fluidtank.{ModObjects, Utils, toAsScalaLO}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler}
import net.minecraftforge.fluids.{FluidStack, FluidUtil}

class FluidPipeBlock extends PipeBlock {
  override protected def getRegName = "pipe"

  override protected def isHandler(level: BlockGetter, pos: BlockPos, property: EnumProperty[PipeBlockConnection]) = isFluidHandler(level, pos, property)

  override protected def getConnection(direction: Direction, entity: BlockEntity): PipeBlockConnection = {
    entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite).asScala
      .map(_.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.SIMULATE))
      .map(filled => if (filled >= 4000) PipeBlockConnection.OUTPUT else PipeBlockConnection.CONNECTED)
      .getOrElse(PipeBlockConnection.NO_CONNECTION)
      .value
  }

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ModObjects.FLUID_PIPE_TYPE.create(pos, state)

  override def getTicker[T <: BlockEntity](level: Level, state: BlockState, value: BlockEntityType[T]): BlockEntityTicker[T] = if (level.isClientSide) null
  else Utils.checkType(value, ModObjects.FLUID_PIPE_TYPE, (_, _, _, pipe: PipeTile) => pipe.tick())

  private def isFluidHandler(w: BlockGetter, pipePos: BlockPos, p: EnumProperty[PipeBlockConnection]): Boolean = {
    val d = Objects.requireNonNull(PipeBlock.FACING_TO_PROPERTY_MAP.inverse.get(p))
    isFluidHandler(w, pipePos.relative(d), d)
  }

  def isFluidHandler(world: BlockGetter, pos: BlockPos, direction: Direction): Boolean = {
    val t = world.getBlockEntity(pos)
    t != null && t.getLevel != null && FluidUtil.getFluidHandler(t.getLevel, pos, direction.getOpposite).isPresent
  }
}
