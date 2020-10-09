package com.kotori316.fluidtank.fluids

import cats.data.{Chain, ReaderWriterStateT}

class CreativeTankHandler extends TankHandler {
  setTank(Tank(FluidAmount.EMPTY, Long.MaxValue))

  override def getFillOperation(tank: Tank): TankOperation = {
    if (tank.fluidAmount.isEmpty) {
      // Fill tank.
      super.getFillOperation(tank).map(t => t.copy(t.fluidAmount.setAmount(t.capacity)))
    } else {
      ReaderWriterStateT.applyS { s =>
        if (tank.fluidAmount fluidEqual s) {
          (Chain(FluidTransferLog.FillAll(s, tank)), FluidAmount.EMPTY, tank)
        } else {
          (Chain(FluidTransferLog.FillFailed(s, tank)), s, tank)
        }
      }
    }
  }

  override def getDrainOperation(tank: Tank): TankOperation =
    if (tank.fluidAmount.isEmpty) {
      super.getDrainOperation(tank).map(_ => tank)
    } else {
      ReaderWriterStateT.applyS { s =>
        if ((tank.fluidAmount fluidEqual s) || (FluidAmount.EMPTY fluidEqual s)) {
          (Chain(FluidTransferLog.DrainFluid(s, s, tank, tank)), FluidAmount.EMPTY, tank)
        } else {
          (Chain(FluidTransferLog.DrainFailed(s, tank)), s, tank)
        }
      }
    }
}
