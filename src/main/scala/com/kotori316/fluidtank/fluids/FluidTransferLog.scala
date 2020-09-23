package com.kotori316.fluidtank.fluids

import cats.Show

sealed trait FluidTransferLog {
  def logString: String

  override final def toString: String = logString
}

object FluidTransferLog {

  case class FillFluid(toFill: FluidAmount, filled: FluidAmount, before: Tank, after: Tank) extends FluidTransferLog {
    override def logString: String = s"FillFluid{Filled=$filled, ToFill=$toFill, Before={${before.fluidAmount}}, After={${after.fluidAmount}}}"
  }

  case class FillFailed(fluid: FluidAmount, tank: Tank) extends FluidTransferLog {
    override def logString: String = s"FillFailed{FailedToFill=$fluid, Tank={${tank.fluidAmount}}}"
  }

  case class FillAll(fluid: FluidAmount, tank: Tank) extends FluidTransferLog {
    override def logString: String = s"FillAll{Filled=$fluid, Tank={${tank.fluidAmount}}}"
  }

  case class DrainFluid(toDrain: FluidAmount, drained: FluidAmount, before: Tank, after: Tank) extends FluidTransferLog {
    override def logString: String = s"DrainFluid{Drained=$drained, ToDrain=$toDrain, Before={${before.fluidAmount}}, After={${after.fluidAmount}}}"
  }

  case class DrainFailed(fluid: FluidAmount, tank: Tank) extends FluidTransferLog {
    override def logString: String = s"DrainFailed{ToDrain=$fluid, Tank={${tank.fluidAmount}}}"
  }

  case class Empty(fluid: FluidAmount, tank: Tank) extends FluidTransferLog {
    override def logString: String = s"Empty{s=$fluid, Tank={${tank.fluidAmount}}}"
  }

  implicit val showFluidTransferLog: Show[FluidTransferLog] = _.logString
}
