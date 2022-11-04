package com.kotori316.fluidtank.integration.mekanism_gas

import cats.data.Chain
import cats.implicits.{catsSyntaxEq, catsSyntaxFoldOps}
import com.kotori316.fluidtank.fluids.{FluidTransferLog, GenericAmount, ListHandler, Tank, drainOp, fillOp, opList}
import com.kotori316.fluidtank.{FluidTank, ModObjects, Utils}
import mekanism.api.Action
import mekanism.api.chemical.IChemicalHandler
import mekanism.api.chemical.gas.{Gas, GasStack, IGasHandler}
import net.minecraftforge.fluids.capability.IFluidHandler

class GasListHandler(gasHandlers: Chain[GasTankHandler]) extends ListHandler[Gas] with IChemicalHandler[Gas, GasStack] with IGasHandler {
  override type ListType[A] = Chain[A]

  override def getSumOfCapacity: Long = gasHandlers.map(_.getCapacity).iterator.sum

  override def fill(resource: GenericAmount[Gas], action: IFluidHandler.FluidAction): GenericAmount[Gas] =
    this.action(opList(gasHandlers.map(_.getTank).map(fillOp)), resource, action)

  override def drain(toDrain: GenericAmount[Gas], action: IFluidHandler.FluidAction): GenericAmount[Gas] =
    this.action(opList(gasHandlers.map(_.getTank).map(drainOp)), toDrain, action)

  override protected def outputLog(logs: Chain[FluidTransferLog], action: IFluidHandler.FluidAction): Unit = {
    if (Utils.isInDev && action.execute()) {
      FluidTank.LOGGER.debug(ModObjects.MARKER_GasListHandler, logs.mkString_(action.toString + " ", ", ", ""))
    }
  }

  override protected def updateTanks(newTanks: Chain[Tank[Gas]]): Unit = {
    require(newTanks.length === gasHandlers.length, s"The length of handlers and tanks must match. $gasHandlers, $newTanks")
    (gasHandlers.iterator zip newTanks.iterator)
      .foreach { case (handler, tank) => handler.setTank(tank) }
  }

  override def getEmptyStack: GasStack = GasStack.EMPTY

  override def getTanks: Int = 1

  override def getChemicalInTank(tank: Int): GasStack = drain(GasAmount.EMPTY.setAmount(Long.MaxValue), IFluidHandler.FluidAction.SIMULATE).toStack

  override def setChemicalInTank(tank: Int, stack: GasStack): Unit = {
    drain(GasAmount.EMPTY.setAmount(Long.MaxValue), IFluidHandler.FluidAction.EXECUTE)
    fill(GasAmount.fromStack(stack), IFluidHandler.FluidAction.EXECUTE)
  }

  override def getTankCapacity(tank: Int): Long = getSumOfCapacity

  override def isValid(tank: Int, stack: GasStack): Boolean = true

  override def insertChemical(tank: Int, stack: GasStack, action: Action): GasStack = {
    val filled = this.fill(GasAmount.fromStack(stack), action.toFluidAction)
    new GasStack(stack, stack.getAmount - filled.amount)
  }

  override def extractChemical(tank: Int, amount: Long, action: Action): GasStack = {
    this.drain(GasAmount.EMPTY.setAmount(amount), action.toFluidAction).toStack
  }
}
