package com.kotori316.fluidtank.fluids

import cats.data.Chain
import net.minecraftforge.fluids.capability.IFluidHandler

trait ListHandler[T] {
  type ListType[_]

  def getSumOfCapacity: Long

  protected def action(op: ListTankOperation[ListType, T], resource: GenericAmount[T], action: IFluidHandler.FluidAction): GenericAmount[T] = {
    val (log, left, newTanks) = op.run((), resource)
    val moved = resource - left
    if (action.execute())
      updateTanks(newTanks)
    outputLog(log, action)
    moved
  }

  def fill(resource: GenericAmount[T], action: IFluidHandler.FluidAction): GenericAmount[T]

  def drain(toDrain: GenericAmount[T], action: IFluidHandler.FluidAction): GenericAmount[T]

  protected def outputLog(logs: Chain[FluidTransferLog], action: IFluidHandler.FluidAction): Unit

  protected def updateTanks(newTanks: ListType[Tank[T]]): Unit
}
