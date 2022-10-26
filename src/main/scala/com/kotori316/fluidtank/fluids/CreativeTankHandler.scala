package com.kotori316.fluidtank.fluids

import cats.data.{Chain, ReaderWriterStateT}
import net.minecraft.world.level.material.Fluid

class CreativeTankHandler extends TankHandler {
  setTank(Tank(FluidAmount.EMPTY, Long.MaxValue))

  override def getFillOperation(tank: Tank[Fluid]): TankOperation[Fluid] = CreativeTankHandler.creativeFillOp(tank)

  override def getDrainOperation(tank: Tank[Fluid]): TankOperation[Fluid] = CreativeTankHandler.creativeDrainOp(tank)
}

object CreativeTankHandler {
  def creativeFillOp(tank: Tank[Fluid]): TankOperation[Fluid] =
    if (tank.isEmpty) {
      // Fill tank.
      fillOp(tank).map(t => t.copy(t.fluidAmount.setAmount(t.capacity)))
    } else {
      ReaderWriterStateT.applyS { s =>
        if (tank.fluidAmount fluidEqual s) {
          (Chain(FluidTransferLog.FillAll(s, tank)), FluidAmount.EMPTY, tank)
        } else {
          (Chain(FluidTransferLog.FillFailed(s, tank)), s, tank)
        }
      }
    }

  def creativeDrainOp(tank: Tank[Fluid]): TankOperation[Fluid] =
    if (tank.isEmpty) {
      drainOp(tank) // Nothing to change.
    } else {
      ReaderWriterStateT.applyS { s =>
        if ((tank.fluidAmount fluidEqual s) || (FluidAmount.EMPTY fluidEqual s)) {
          (Chain(FluidTransferLog.DrainFluid(s, s, tank, tank)), tank.fluidAmount.setAmount(0L), tank)
        } else {
          (Chain(FluidTransferLog.DrainFailed(s, tank)), s, tank)
        }
      }
    }
}
