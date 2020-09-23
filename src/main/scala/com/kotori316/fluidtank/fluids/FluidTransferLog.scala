package com.kotori316.fluidtank.fluids

import cats.Show

sealed trait FluidTransferLog {
  def logString: String

  override final def toString: String = logString
}

object FluidTransferLog {

  case class FillFluid(toFill: FluidAmount, filled: FluidAmount, tank: Tank) extends FluidTransferLog {
    override def logString: String = s"FillFluid{Filled=$filled, ToFill=$toFill, Tank={${tank.fluidAmount}}}"
  }

  case class FillFailed(fluid: FluidAmount, tank: Tank) extends FluidTransferLog {
    override def logString: String = s"FillFailed{FailedToFill=$fluid, Tank={${tank.fluidAmount}}}"
  }

  case class DrainFluid(toDrain: FluidAmount, drained: FluidAmount, tank: Tank) extends FluidTransferLog {
    override def logString: String = s"DrainFluid{Drained=$drained, ToDrain=$toDrain, Tank={${tank.fluidAmount}}}"
  }

  case class DrainFailed(fluid: FluidAmount, tank: Tank) extends FluidTransferLog {
    override def logString: String = s"DrainFailed{ToDrain=$fluid, Tank={${tank.fluidAmount}}}"
  }

  case class Empty(fluid: FluidAmount, tank: Tank) extends FluidTransferLog {
    override def logString: String = s"Empty{s=$fluid, Tank={${tank.fluidAmount}}}"
  }

  implicit val showFluidTransferLog: Show[FluidTransferLog] = _.logString
}
