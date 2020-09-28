package com.kotori316.fluidtank.fluids

import cats.data.{Chain, ReaderWriterStateT}

class CreativeTankHandler extends TankHandler {
  setTank(Tank(FluidAmount.EMPTY, Long.MaxValue))

  override protected def getFillOperation(tank: Tank): TankOperation = {
    if (getTank.fluidAmount.isEmpty) {
      // Fill tank.
      super.getFillOperation(tank).map(t => t.copy(t.fluidAmount.setAmount(t.capacity)))
    } else {
      ReaderWriterStateT { (_, s) =>
        if (tank.fluidAmount fluidEqual s) {
          (Chain(FluidTransferLog.FillAll(s, tank)), FluidAmount.EMPTY, tank)
        } else {
          (Chain(FluidTransferLog.FillFailed(s, tank)), s, tank)
        }
      }
    }
  }

  override protected def getDrainOperation(tank: Tank): TankOperation =
    super.getDrainOperation(tank).map(_ => tank)
}
