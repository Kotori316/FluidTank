package com.kotori316.fluidtank.fluids

import cats.Align
import cats.data.Chain
import cats.implicits._
import com.kotori316.fluidtank.{FluidTank, ModObjects, Utils}
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

class ListTankHandler(tankHandlers: Chain[TankHandler]) extends IFluidHandler {

  def getTankList: Chain[Tank] = tankHandlers.map(_.getTank)

  override def getTanks: Int = 1

  override def getFluidInTank(tank: Int): FluidStack =
    drainList(getTankList).runS((), FluidAmount.EMPTY.setAmount(getSumOfCapacity)).toStack

  def getSumOfCapacity: Long = tankHandlers.map(_.getTank.capacity).combineAll

  override def getTankCapacity(tank: Int): Int = Utils.toInt(getSumOfCapacity)

  override def isFluidValid(tank: Int, stack: FluidStack): Boolean = true

  override def fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int = {
    val (log, left, newTanks) = fillList(getTankList).run((), FluidAmount.fromStack(resource))
    val filledAmount: Int = Utils.toInt(resource.getAmount - left.amount)
    if (action.execute())
      updateTanks(newTanks)
    outputLog(log, action)
    filledAmount
  }

  protected def drainInternal(toDrain: FluidAmount, action: IFluidHandler.FluidAction): FluidStack = {
    val (log, left, newTanks) = drainList(getTankList).run((), toDrain)
    val drained = toDrain - left
    if (action.execute())
      updateTanks(newTanks)
    outputLog(log, action)
    drained.toStack
  }

  override def drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack = drainInternal(FluidAmount.fromStack(resource), action)

  override def drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack = drainInternal(FluidAmount.EMPTY.setAmount(maxDrain), action)

  protected def outputLog(logs: Chain[FluidTransferLog], action: IFluidHandler.FluidAction): Unit = {
    if (Utils.isInDev) {
      FluidTank.LOGGER.debug(ModObjects.MARKER_ListTankHandler, logs.mkString_(action.toString + " ", ", ", ""))
    }
  }

  protected def updateTanks(newTanks: Chain[Tank]): Unit = {
    Align[Chain].zipAll(tankHandlers, newTanks, EmptyTankHandler, Tank.EMPTY)
      .iterator.foreach { case (handler, tank) => handler.setTank(tank) }
  }
}
