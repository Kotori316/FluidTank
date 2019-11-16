package com.kotori316.fluidtank.transport

import cats.Eval
import cats.data.OptionT
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
    val pipePosIterator = pipeTile.connection.outputSorted(pipeTile.getPos).iterator
    var rest = fluidAmount
    while (pipePosIterator.hasNext) {
      val pipePos = pipePosIterator.next()
      val handlerIterator = PipeTile.facings.map(dir => pipePos.offset(dir) -> dir).iterator
        .filter { case (_, direction) => pipeTile.getWorld.getBlockState(pipePos).get(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput }
        .flatMap { case (pos, direction) => OptionT.fromOption[Eval](Option(pipeTile.getWorld.getTileEntity(pos)))
          .flatMap(_.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite).asScala).value.value
        }
      while (handlerIterator.hasNext) {
        val handler = handlerIterator.next()
        rest -= rest.setAmount(handler.fill(rest.toStack, FluidAmount.b2a(doFill)))
        if (rest.isEmpty)
          return fluidAmount
      }
    }
    /*return*/ fluidAmount - rest
    /*
        val destinations = for {
          p <- LazyList.from(pipeTile.connection.outputSorted(pipeTile.getPos))
          (pos, direction) <- PipeTile.facings.map(dir => p.offset(dir) -> dir)
          if pipeTile.getWorld.getBlockState(p).get(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput
          dest <- OptionT.fromOption[Eval](Option(pipeTile.getWorld.getTileEntity(pos)))
            .flatMap(_.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite).asScala).value.value
        } yield dest
        val notFilled = destinations.foldl(fluidAmount) { case (rest, handler) =>
          if (rest.isEmpty) rest
          else rest - rest.setAmount(handler.fill(rest.toStack, FluidAmount.b2a(doFill)))
        }
        fluidAmount - notFilled*/
  }

}
