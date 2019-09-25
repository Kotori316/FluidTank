package com.kotori316.fluidtank.transport

import cats._
import cats.data._
import cats.implicits._
import com.kotori316.fluidtank._
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler}

class PipeFluidHandler(pipeTile: PipeTile) extends FluidAmount.Tank {
  /**
   * @param fluidAmount the fluid representing the kind and maximum amount to drain.
   *                    Empty Fluid means fluid type can be anything.
   * @param doDrain     false means simulating.
   * @param min         minimum amount to drain.
   * @return the fluid and amount that is (or will be) drained.
   */
  override def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Int): FluidAmount = FluidAmount.EMPTY

  override def getFluidInTank(tank: Int): FluidStack = FluidStack.EMPTY

  override def getTankCapacity(tank: Int): Int = 0

  override def getTanks: Int = 0

  override def isFluidValid(tank: Int, stack: FluidStack): Boolean = false

  override def drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack = FluidStack.EMPTY

  override def drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack = FluidStack.EMPTY

  /**
   * @return Fluid that was accepted by the tank.
   */
  override def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Int): FluidAmount = {
    val destinations = for {
      p <- pipeTile.connection.outputSorted(pipeTile.getPos)
      (pos, direction) <- PipeTile.facings.map(dir => p.offset(dir) -> dir)
      if pipeTile.getWorld.getBlockState(p).get(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput
      dest <- OptionT.pure[Eval](pipeTile.getWorld.getTileEntity(pos))
        .flatMap(_.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite).asScala)
        .toList
    } yield dest
    val filled = destinations.foldl(fluidAmount) { case (amount, handler) =>
      if (amount.isEmpty) amount
      else amount - amount.setAmount(handler.fill(amount.toStack, FluidAmount.b2a(doFill)))
    }
    fluidAmount - filled
  }

}
