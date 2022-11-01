package com.kotori316.fluidtank.tiles

import cats.data.Chain
import cats.implicits.catsSyntaxFoldOps
import com.kotori316.fluidtank.blocks.TankPos
import com.kotori316.fluidtank.fluids.{DebugFluidHandler, FluidAmount, FluidTransferLog, ListTankHandler, TankHandler, fillAll}
import com.kotori316.fluidtank.{FluidTank, ModObjects, Utils}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.{Component, TranslatableComponent}
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler}

class FluidConnection(s: Seq[TileTank]) extends Connection2[TileTank](s) {
  override implicit val helper: ConnectionHelper.Aux[TileTank, Fluid, ListTankHandler] = TileTank.fluidConnectionHelper

  private final val fluidHandler = LazyOptional.of(() => if (this.sortedTanks.nonEmpty) handler
  else if (Utils.isInDev) DebugFluidHandler.INSTANCE else EmptyFluidHandler.INSTANCE)

  override protected def invalidate(): Unit = {
    super.invalidate()
    this.fluidHandler.invalidate()
  }

  override def getCapability[T](capability: Capability[T], side: Direction): LazyOptional[T] = {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
      fluidHandler.cast()
    } else {
      super.getCapability(capability, side)
    }
  }

  def getTextComponent: Component = {
    if (hasCreative)
      new TranslatableComponent("chat.fluidtank.connection_creative",
        getFluidStack.map(_.toStack.getDisplayName).getOrElse(new TranslatableComponent("chat.fluidtank.empty")),
        Int.box(getComparatorLevel))
    else
      new TranslatableComponent("chat.fluidtank.connection",
        getFluidStack.map(_.toStack.getDisplayName).getOrElse(new TranslatableComponent("chat.fluidtank.empty")),
        Long.box(amount),
        Long.box(capacity),
        Int.box(getComparatorLevel))
  }

  def getFluidHandler: ListTankHandler = handler

  def getFluidStack: Option[FluidAmount] = this.getContent

  def fluidType: FluidAmount = this.contentType
}

object FluidConnection {
  def invalid: FluidConnection = new InvalidFluidConnection

  private class InvalidFluidConnection extends FluidConnection(Nil) {
    override val isDummy = true

    override protected def contentType: FluidAmount = FluidAmount.EMPTY

    override def capacity: Long = 0

    override def amount: Long = 0

    override val toString: String = "FluidConnection.Invalid"

    override def getComparatorLevel: Int = 0

    override def remove(tileTank: TileTank): Unit = ()
  }

  class ConnectionTankHandler(tankHandlers: Chain[TankHandler], hasCreative: Boolean) extends ListTankHandler(tankHandlers, true) {

    override protected def outputLog(logs: Chain[FluidTransferLog], action: IFluidHandler.FluidAction): Unit = {
      import org.apache.logging.log4j.util.Supplier
      if (action.execute() && Utils.isInDev) {
        FluidTank.LOGGER.debug(ModObjects.MARKER_Connection, (() => logs.mkString_(action.toString + " ", ", ", "")): Supplier[String])
      } else {
        FluidTank.LOGGER.trace(ModObjects.MARKER_Connection, (() => logs.mkString_(action.toString + " ", ", ", "")): Supplier[String])
      }
    }

    override def fill(resource: FluidAmount, action: IFluidHandler.FluidAction): FluidAmount =
      if (hasCreative) super.action(fillAll(getTankList), resource, action) else super.fill(resource, action)

    override def drain(toDrain: FluidAmount, action: IFluidHandler.FluidAction): FluidAmount =
      if (hasCreative) super.drain(toDrain, IFluidHandler.FluidAction.SIMULATE) else super.drain(toDrain, action)
  }

  def create(s: Seq[TileTank]): FluidConnection = {
    if (s.isEmpty) {
      invalid
    } else {
      val seq = s.sortBy(_.getBlockPos.getY)
      // Property update
      if (seq.lengthIs > 1) {
        // HEAD
        val head = seq.head
        head.getLevel.setBlockAndUpdate(head.getBlockPos, head.getBlockState.setValue(TankPos.TANK_POS_PROPERTY, TankPos.BOTTOM))
        // LAST
        val last = seq.last
        last.getLevel.setBlockAndUpdate(last.getBlockPos, last.getBlockState.setValue(TankPos.TANK_POS_PROPERTY, TankPos.TOP))
        // MIDDLE
        seq.tail.init.foreach(t => t.getLevel.setBlockAndUpdate(t.getBlockPos, t.getBlockState.setValue(TankPos.TANK_POS_PROPERTY, TankPos.MIDDLE)))
      } else {
        // SINGLE
        seq.foreach(t => t.getLevel.setBlockAndUpdate(t.getBlockPos, t.getBlockState.setValue(TankPos.TANK_POS_PROPERTY, TankPos.SINGLE)))
      }
      new FluidConnection(seq)
    }
  }

  def load(level: BlockGetter, pos: BlockPos): Unit = {
    val lowest = Iterator.iterate(pos)(_.below()).takeWhile(p => level.getBlockEntity(p).isInstanceOf[TileTank])
      .toList.lastOption.getOrElse {
      FluidTank.LOGGER.fatal(ModObjects.MARKER_Connection, "No lowest tank", new IllegalStateException("No lowest tank"))
      pos
    }
    val tanks = Iterator.iterate(lowest)(_.above()).map(level.getBlockEntity).takeWhile(_.isInstanceOf[TileTank])
      .toList.map(_.asInstanceOf[TileTank])
    //    tanks.foldLeft(Connection.invalid) { case (c, tank) => c.add(tank, Direction.UP) }
    Connection2.createAndInit(tanks)
  }
}
