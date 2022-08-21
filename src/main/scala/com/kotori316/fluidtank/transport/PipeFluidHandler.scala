package com.kotori316.fluidtank.transport

import com.kotori316.fluidtank._
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

class PipeFluidHandler(pipeTile: PipeTileBase) extends IFluidHandler {
  override def getFluidInTank(tank: Int): FluidStack = FluidStack.EMPTY

  override def getTankCapacity(tank: Int): Int = 0

  override def getTanks: Int = 0

  override def isFluidValid(tank: Int, stack: FluidStack): Boolean = false

  override def drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack = FluidStack.EMPTY

  override def drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack = FluidStack.EMPTY

  override def fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int = {
    val pipePosIterator = pipeTile.connection.outputs(pipeTile.getBlockPos).iterator
    val rest = resource.copy()
    while (pipePosIterator.hasNext) {
      val pipePos = pipePosIterator.next()
      val handlerIterator = directions.map(dir => pipePos.offset(dir) -> dir).iterator
        .filter { case (_, direction) => pipeTile.getLevel.getBlockState(pipePos).getValue(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput }
        .flatMap { case (pos, direction) => Cap.make(pipeTile.getLevel.getBlockEntity(pos))
          .flatMap(_.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite).asScala).value.value
        }
      while (handlerIterator.hasNext) {
        val handler = handlerIterator.next()
        rest.setAmount(rest.getAmount - handler.fill(rest, action))
        if (rest.isEmpty)
          return 0
      }
    }
    /*return*/ resource.getAmount - rest.getAmount
  }

}
