package com.kotori316.fluidtank.fluids

import cats.Monad
import cats.data.Chain
import net.minecraftforge.fluids.capability.IFluidHandler

trait ListHandler[T] {
  def getSumOfCapacity: Long

  protected def action[F[_] : Monad]
  (op: ListTankOperation[F, T], resource: GenericAmount[T], action: IFluidHandler.FluidAction,
   updateTanks: F[Tank[T]] => Unit, outputLog: (Chain[FluidTransferLog], IFluidHandler.FluidAction) => Unit)
  : GenericAmount[T] = {
    val (log, left, newTanks) = op.run((), resource)
    val moved = resource - left
    if (action.execute())
      updateTanks(newTanks)
    outputLog(log, action)
    moved
  }

  def fill(resource: GenericAmount[T], action: IFluidHandler.FluidAction): GenericAmount[T]

  def drain(toDrain: GenericAmount[T], action: IFluidHandler.FluidAction): GenericAmount[T]
}
