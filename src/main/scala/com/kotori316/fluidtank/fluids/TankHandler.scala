package com.kotori316.fluidtank.fluids

import cats.data.Chain
import cats.implicits.{catsSyntaxEq, catsSyntaxFoldOps}
import com.kotori316.fluidtank._

/**
 * Mutable, because forge requires to be.
 * The handler will be cached, so mutable is the easiest way to tell the change of content to the holder.
 */
class TankHandler {

  private[this] final var tank: Tank = Tank.EMPTY

  def setTank(newTank: Tank): Unit = {
    if (this.tank =!= newTank) {
      this.tank = newTank
      onContentsChanged()
    }
  }

  def initCapacity(capacity: Long): Unit = {
    val newTank = getTank.copy(capacity = capacity)
    // Not to use setter to avoid NPE of connection.
    this.tank = newTank
  }

  def getTank: Tank = tank

  def onContentsChanged(): Unit = ()

  def getFillOperation(tank: Tank): TankOperation = fillOp(tank)

  def getDrainOperation(tank: Tank): TankOperation = drainOp(tank)

  private final def action(op: TankOperation, resource: FluidAmount, action: FluidAction): FluidAmount = {
    val (log, left, newTank) = op.run((), resource)
    val moved: FluidAmount = resource - left
    if (action.execute())
      setTank(newTank)
    outputLog(log, action)
    moved
  }

  final def fill(resource: FluidAmount, action: FluidAction): FluidAmount = {
    this.action(getFillOperation(this.tank), resource, action)
  }

  final def drain(toDrain: FluidAmount, action: FluidAction): FluidAmount = {
    this.action(getDrainOperation(this.tank), toDrain, action)
  }

  protected def outputLog(logs: Chain[FluidTransferLog], action: FluidAction): Unit = {
    if (Utils.isInDev) {
      FluidTank.LOGGER.debug(ModObjects.MARKER_TankHandler, logs.mkString_(action.toString + " ", ", ", ""))
    }
  }

  override def toString: String = s"${getClass.getName} with $tank"
}

object TankHandler {
  def apply(capacity: Long): TankHandler = {
    val h = new TankHandler()
    h.setTank(h.getTank.copy(capacity = capacity))
    h
  }

  def apply(tank: Tank): TankHandler = {
    val h = new TankHandler()
    h setTank tank
    h
  }
}
