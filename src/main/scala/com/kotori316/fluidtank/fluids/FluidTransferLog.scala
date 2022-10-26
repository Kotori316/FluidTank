package com.kotori316.fluidtank.fluids

import cats.Show

sealed trait FluidTransferLog {
  def logString: String

  override final def toString: String = logString
}

object FluidTransferLog {

  case class FillFluid[A](toFill: GenericAmount[A], filled: GenericAmount[A], before: Tank[A], after: Tank[A]) extends FluidTransferLog {
    override def logString: String = s"FillFluid{Filled=$filled, ToFill=$toFill, Before={${before.fluidAmount}}, After={${after.fluidAmount}}}"
  }

  case class FillFailed[A](fluid: GenericAmount[A], tank: Tank[A]) extends FluidTransferLog {
    override def logString: String = s"FillFailed{FailedToFill=$fluid, Tank={${tank.fluidAmount}}}"
  }

  case class FillAll[A](fluid: GenericAmount[A], tank: Tank[A]) extends FluidTransferLog {
    override def logString: String = s"FillAll{Filled=$fluid, Tank={${tank.fluidAmount}}}"
  }

  case class DrainFluid[A](toDrain: GenericAmount[A], drained: GenericAmount[A], before: Tank[A], after: Tank[A]) extends FluidTransferLog {
    override def logString: String = s"DrainFluid{Drained=$drained, ToDrain=$toDrain, Before={${before.fluidAmount}}, After={${after.fluidAmount}}}"
  }

  case class DrainFailed[A](fluid: GenericAmount[A], tank: Tank[A]) extends FluidTransferLog {
    override def logString: String = s"DrainFailed{ToDrain=$fluid, Tank={${tank.fluidAmount}}}"
  }

  case class Empty[A](fluid: GenericAmount[A], tank: Tank[A]) extends FluidTransferLog {
    override def logString: String = s"Empty{s=$fluid, Tank={${tank.fluidAmount}}}"
  }

  implicit val showFluidTransferLog: Show[FluidTransferLog] = _.logString
}
