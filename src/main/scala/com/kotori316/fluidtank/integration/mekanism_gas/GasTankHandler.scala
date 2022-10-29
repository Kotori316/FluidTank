package com.kotori316.fluidtank.integration.mekanism_gas

import cats.data.Chain
import cats.implicits.catsSyntaxFoldOps
import com.kotori316.fluidtank.fluids.{FluidTransferLog, Tank, TankOperation, drainOp, fillOp}
import com.kotori316.fluidtank.tiles.TileTank
import com.kotori316.fluidtank.{FluidTank, ModObjects, Utils}
import mekanism.api.chemical.IChemicalTank
import mekanism.api.chemical.gas.{Gas, GasStack}
import mekanism.api.{AutomationType, NBTConstants}
import net.minecraft.nbt.CompoundTag

/**
 * Mutable
 */
class GasTankHandler extends IChemicalTank[Gas, GasStack] {
  private[this] var tank: Tank[Gas] = GasTankHandler.emptyTank

  private final def setTank(tank: Tank[Gas]): Unit = {
    if (this.tank != tank) {
      this.tank = tank
      onContentsChanged()
    }
  }

  final def getTank: Tank[Gas] = this.tank

  private final def action(op: TankOperation[Gas], resource: GasAmount, action: mekanism.api.Action): GasAmount = {
    val (log, left, newTank) = op.run((), resource)
    val moved: GasAmount = resource - left
    if (action.execute())
      setTank(newTank)
    outputLog(log, action)
    moved
  }

  def getFillOperation(tank: Tank[Gas]): TankOperation[Gas] = fillOp(tank)

  def getDrainOperation(tank: Tank[Gas]): TankOperation[Gas] = drainOp(tank)

  final def fill(resource: GasAmount, action: mekanism.api.Action): GasAmount = {
    this.action(getFillOperation(this.tank), resource, action)
  }

  final def drain(toDrain: GasAmount, action: mekanism.api.Action): GasAmount = {
    this.action(getDrainOperation(this.tank), toDrain, action)
  }

  override final def insert(stack: GasStack, action: mekanism.api.Action, automationType: AutomationType): GasStack = {
    this.fill(GasAmount.fromStack(stack), action).toStack
  }

  override final def extract(amount: Long, action: mekanism.api.Action, automationType: AutomationType): GasStack = {
    this.drain(GasAmount.EMPTY.setAmount(amount), action).toStack
  }

  override def setStackSize(amount: Long, action: mekanism.api.Action): Long = {
    if (this.isEmpty) {
      0
    } else {
      if (amount > this.tank.amount) {
        // Fill
        val fillAmount = amount - this.tank.amount
        this.fill(this.tank.genericAmount.setAmount(fillAmount), action)
        amount
      } else {
        // Drain
        val drainAmount = this.tank.amount - amount
        this.drain(this.tank.genericAmount.setAmount(drainAmount), action)
        amount
      }
    }
  }

  override final def createStack(stored: GasStack, size: Long): GasStack = new GasStack(stored, size)

  override final def getStack: GasStack = this.tank.genericAmount.toStack

  override final def setStack(stack: GasStack): Unit = setTank(this.tank.copy(genericAmount = GasAmount.fromStack(stack)))

  override final def setStackUnchecked(stack: GasStack): Unit = setStack(stack) // We have no limitation for content.

  override final def getCapacity: Long = this.tank.capacity

  override final def isEmpty: Boolean = this.tank.genericAmount.isEmpty

  override final def setEmpty(): Unit = this.setTank(this.tank.copy(genericAmount = GasAmount.EMPTY))

  override final def getStored: Long = this.tank.amount

  override def isValid(stack: GasStack): Boolean = true

  override def onContentsChanged(): Unit = ()

  override def deserializeNBT(nbt: CompoundTag): Unit = {
    val stack = GasStack.readFromNBT(nbt.getCompound(NBTConstants.STORED))
    val capacity = nbt.getLong(TileTank.NBT_Capacity)

    val tank = new Tank(GasAmount.fromStack(stack), capacity)
    this.tank = tank
  }

  override def serializeNBT(): CompoundTag = {
    val tag = super.serializeNBT()
    tag.putLong(TileTank.NBT_Capacity, this.tank.capacity)
    tag
  }

  override final def getEmptyStack: GasStack = GasStack.EMPTY

  protected def outputLog(logs: Chain[FluidTransferLog], action: mekanism.api.Action): Unit = {
    if (Utils.isInDev) {
      FluidTank.LOGGER.debug(ModObjects.MARKER_GasHandler, logs.mkString_(action.toString + " ", ", ", ""))
    }
  }

}

object GasTankHandler {
  val emptyTank: Tank[Gas] = new Tank(GasAmount.EMPTY, 0)

  def apply(tank: Tank[Gas]): GasTankHandler = {
    val handler = new GasTankHandler
    handler.setTank(tank)
    handler
  }

  def apply(capacity: Long): GasTankHandler = {
    val handler = new GasTankHandler
    handler.setTank(handler.getTank.copy(capacity = capacity))
    handler
  }
}
