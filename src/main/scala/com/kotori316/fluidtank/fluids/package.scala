package com.kotori316.fluidtank

import cats.data.{Chain, ReaderWriterStateT}
import cats.syntax.eq._
import cats.syntax.group._
import cats.syntax.semigroupk._
import cats.{Applicative, Foldable, Id, Monad, MonoidK}
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

package object fluids {
  type FluidAmount = GenericAmount[Fluid]

  type TankOperation[A] = ReaderWriterStateT[Id, Unit, Chain[FluidTransferLog], GenericAmount[A], Tank[A]]
  type ListTankOperation[F[_], A] = ReaderWriterStateT[Id, Unit, Chain[FluidTransferLog], GenericAmount[A], F[Tank[A]]]

  def fillOp[A](tank: Tank[A]): TankOperation[A] = ReaderWriterStateT { case (_, s) =>
    if (s.isEmpty) {
      // Nothing to fill, skip.
      (Chain(FluidTransferLog.Empty(s, tank)), s, tank)
    } else if (tank.genericAmount.isEmpty || (tank.genericAmount contentEqual s)) {
      val filledAmount = (tank.capacity |-| tank.amount) min s.amount
      val filledStack = s.setAmount(filledAmount)
      val newTank = tank.copy(tank.genericAmount + filledStack)
      (Chain(FluidTransferLog.FillFluid(s, filledStack, tank, newTank)), s - filledStack, newTank)
    } else {
      (Chain(FluidTransferLog.FillFailed(s, tank)), s, tank)
    }
  }

  def drainOp[A](tank: Tank[A]): TankOperation[A] = if (tank.isEmpty) ReaderWriterStateT.applyS(s => Monad[Id].pure((Chain(FluidTransferLog.DrainFailed(s, tank)), s, tank)))
  else ReaderWriterStateT { case (_, s) =>
    if (s.amount === 0L) {
      // Nothing to drain.
      (Chain(FluidTransferLog.Empty(s, tank)), s, tank)
    } else if (s.contentIsEmpty || (s contentEqual tank.genericAmount)) {
      val drainAmount = tank.amount min s.amount
      val drainedStack = tank.genericAmount.setAmount(drainAmount)
      val newTank = tank.copy(tank.genericAmount.setAmount(tank.amount |-| drainAmount))
      val subtracted = if (drainedStack.nonEmpty) s - drainedStack else s
      (Chain(FluidTransferLog.DrainFluid(s, drainedStack, tank, newTank)), subtracted, newTank)
    } else {
      (Chain(FluidTransferLog.DrainFailed(s, tank)), s, tank)
    }
  }

  def opList[F[+_], A](opList: F[TankOperation[A]])(implicit applicative: Applicative[F], F: Foldable[F], monoidK: MonoidK[F]): ListTankOperation[F, A] = {
    val initialState: ListTankOperation[F, A] = ReaderWriterStateT.applyS(f => Monad[Id].pure((Chain.empty, f, monoidK.empty)))
    F.foldLeft(opList, initialState) { (s, op) =>
      s.flatMap(filledTankList => op.map(t => filledTankList <+> applicative.pure(t)))
    }
  }

  def fillList[F[+_], A](tanks: F[Tank[A]])(implicit applicative: Applicative[F], F: Foldable[F], monoidK: MonoidK[F]): ListTankOperation[F, A] =
    opList(applicative.map(tanks)(fillOp))

  def fillAll[F[+_], A](tanks: F[Tank[A]])(implicit applicative: Applicative[F], F: Foldable[F], monoidK: MonoidK[F]): ListTankOperation[F, A] = {
    val op: Tank[A] => TankOperation[A] = t =>
      for {
        before <- ReaderWriterStateT.get[Id, Unit, Chain[FluidTransferLog], GenericAmount[A]]
        _ <- ReaderWriterStateT.modify[Id, Unit, Chain[FluidTransferLog], GenericAmount[A]](f => f.setAmount(t.capacity))
        y <- fillOp(t)
        rest <- ReaderWriterStateT.get[Id, Unit, Chain[FluidTransferLog], GenericAmount[A]]
        r = if (rest.amount < t.capacity) rest.setAmount(0) else before
        _ <- ReaderWriterStateT.set[Id, Unit, Chain[FluidTransferLog], GenericAmount[A]](r)
      } yield y
    opList(applicative.map(tanks)(op))
  }

  def drainList[F[+_], A](tanks: F[Tank[A]])(implicit applicative: Applicative[F], F: Foldable[F], monoidK: MonoidK[F]): ListTankOperation[F, A] =
    opList(applicative.map(tanks)(drainOp))

  final object EmptyTankHandler extends TankHandler {
    override def setTank(newTank: Tank[Fluid]): Unit = ()

    override def getFluidInTank(tank: Int): FluidStack = FluidStack.EMPTY

    override protected def outputLog(logs: Chain[FluidTransferLog], action: IFluidHandler.FluidAction): Unit = ()

    override def getFillOperation(tank: Tank[Fluid]): TankOperation[Fluid] = ReaderWriterStateT.applyS { s =>
      Monad[Id].pure(Chain(FluidTransferLog.FillFailed(s, tank)), s, tank)
    }

    override def getDrainOperation(tank: Tank[Fluid]): TankOperation[Fluid] = ReaderWriterStateT.applyS { s =>
      Monad[Id].pure(Chain(FluidTransferLog.DrainFailed(s, tank)), s, tank)
    }

    override def toString: String = "EmptyTankHandler"
  }

  class VoidTankHandler extends TankHandler {
    override def setTank(newTank: Tank[Fluid]): Unit = ()

    override def getFluidInTank(tank: Int): FluidStack = FluidStack.EMPTY

    override def getFillOperation(tank: Tank[Fluid]): TankOperation[Fluid] = ReaderWriterStateT.applyS { s =>
      Monad[Id].pure(Chain(FluidTransferLog.FillAll(s, tank)), FluidAmount.EMPTY, tank)
    }

    override def getDrainOperation(tank: Tank[Fluid]): TankOperation[Fluid] = ReaderWriterStateT.applyS { s =>
      Monad[Id].pure(Chain(FluidTransferLog.Empty(s, tank)), s, tank)
    }

    override def toString: String = "VoidTankHandler"
  }

  implicit class FluidAmountExtension(private val amount: FluidAmount) extends AnyVal {
    def fluidEqual(that: FluidAmount): Boolean = amount.contentEqual(that)

    def toStack: FluidStack = FluidAmount.toStack(amount)

    def fluid: Fluid = amount.c
  }
}
