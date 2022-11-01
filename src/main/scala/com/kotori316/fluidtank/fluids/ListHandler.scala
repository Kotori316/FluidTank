package com.kotori316.fluidtank.fluids

import net.minecraftforge.fluids.capability.IFluidHandler

trait ListHandler[T] {
  def getSumOfCapacity: Long

  def fill(resource: GenericAmount[T], action: IFluidHandler.FluidAction): GenericAmount[T]

  def drain(toDrain: GenericAmount[T], action: IFluidHandler.FluidAction): GenericAmount[T]
}
