package com.kotori316.fluidtank.tiles

import cats.data._
import cats.implicits._
import com.kotori316.fluidtank._
import com.kotori316.fluidtank.blocks.TankPos
import com.kotori316.fluidtank.fluids.{FluidAction, FluidAmount, FluidTransferLog, ListTankHandler, TankHandler, fillAll}
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraft.world.level.BlockGetter

import scala.collection.mutable.ArrayBuffer

sealed class Connection private(s: Seq[TileTank]) {
  val seq: Seq[TileTank] = s.sortBy(_.getBlockPos.getY)
  val hasCreative: Boolean = seq.exists(_.isInstanceOf[TileTankCreative])
  val hasVoid: Boolean = seq.exists(_.isInstanceOf[TileTankVoid])
  val updateActions: ArrayBuffer[() => Unit] = ArrayBuffer(
    () => seq.foreach(_.setChanged())
  )

  val handler: ListTankHandler = new Connection.ConnectionTankHandler(Chain.fromSeq(seq.map(_.internalTank)), hasCreative)
  val isDummy: Boolean = false
  private[tiles] final var mIsValid = true

  protected def fluidType: FluidAmount = {
    seq.headOption.flatMap(Connection.stackFromTile).orElse(seq.lastOption.flatMap(Connection.stackFromTile)).getOrElse(FluidAmount.EMPTY)
  }

  def capacity: Long = if (hasCreative) Tier.CREATIVE.amount else handler.getSumOfCapacity

  def amount: Long = if (hasCreative && fluidType.nonEmpty) Tier.CREATIVE.amount else seq.map(_.internalTank.getFluidAmount).sum

  def tankSeq(fluid: FluidAmount): Seq[TileTank] = {
    if (fluid != null && fluid.isGaseous) {
      seq.reverse
    } else {
      seq
    }
  }

  def getFluidStack: Option[FluidAmount] = {
    Option(fluidType).filter(_.nonEmpty).map(_.setAmount(amount))
  }

  def remove(tileTank: TileTank): Unit = {
    val (s1, s2) = seq.sortBy(_.getBlockPos.getY).span(_ != tileTank)
    val s1Connection = Connection.create(s1)
    val s2Connection = Connection.create(s2.tail)
    // Connection updated
    this.isValid = false
    s1.foreach { t =>
      t.connection = s1Connection
    }
    s2.tail.foreach { t =>
      t.connection = s2Connection
    }
  }

  def getComparatorLevel: Int = {
    if (amount > 0)
      Mth.floor(amount.toDouble / capacity.toDouble * 14) + 1
    else 0
  }

  def updateNeighbors(): Unit = {
    updateActions.foreach(_.apply())
  }

  private[tiles] def isValid = mIsValid

  //noinspection AccessorLikeMethodIsUnit
  private[tiles] def isValid_=(newValue: Boolean): Unit = {
    mIsValid = newValue
  }

  override def toString: String = {
    val name = getFluidStack.fold("null")(_.getLocalizedName)
    if (!hasCreative)
      s"Connection of $name : $amount / $capacity mB, Comparator outputs $getComparatorLevel."
    else
      s"Connection of $name in creative. Comparator outputs $getComparatorLevel."
  }

  def getTextComponent: Component = {
    if (hasCreative)
      Component.translatable("chat.fluidtank.connection_creative",
        getFluidStack.map(_.getDisplayName).getOrElse(Component.translatable("chat.fluidtank.empty")),
        Int.box(getComparatorLevel))
    else
      Component.translatable("chat.fluidtank.connection",
        getFluidStack.map(_.getDisplayName).getOrElse(Component.translatable("chat.fluidtank.empty")),
        Long.box(amount),
        Long.box(capacity),
        Int.box(getComparatorLevel))
  }
}

object Connection {

  def create(s: Seq[TileTank]): Connection = {
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
      new Connection(seq)
    }
  }

  @scala.annotation.tailrec
  def createAndInit(tankSeq: Seq[TileTank]): Unit = {
    if (tankSeq.nonEmpty) {
      val s = tankSeq.sortBy(_.getBlockPos.getY)
      val fluid = s.map(_.internalTank.getFluid).find(_.nonEmpty).getOrElse(FluidAmount.EMPTY)
      val (s1, s2) = s.span(t => t.internalTank.getFluid.fluidEqual(fluid) || t.internalTank.getFluid.isEmpty)
      // Assert tanks in s1 have the same fluid.
      require(s1.map(_.internalTank.getFluid).forall(f => f.isEmpty || f.fluidEqual(fluid)))
      val content: FluidAmount = s1.foldMap(t => t.internalTank.drain(t.internalTank.getFluid, FluidAction.EXECUTE))
      val connection = Connection.create(s1)
      connection.handler.fill(content, FluidAction.EXECUTE)
      s1.foreach { t =>
        t.connection.isValid = false
        t.connection = connection
      }
      if (s2.nonEmpty) createAndInit(s2)
    }
  }

  def invalid: Connection = new InvalidConnection

  private class InvalidConnection extends Connection(Nil) {
    override val isDummy: Boolean = true

    override def fluidType: FluidAmount = FluidAmount.EMPTY

    override def capacity: Long = 0

    override def amount: Long = 0

    override val toString: String = "Connection.Invalid"

    override def getComparatorLevel: Int = 0

    override def remove(tileTank: TileTank): Unit = ()

    override def getTextComponent = Component.literal(toString)
  }

  val stackFromTile: TileTank => Option[FluidAmount] = (t: TileTank) => Option(t.internalTank.getFluid).filter(_.nonEmpty)

  private class ConnectionTankHandler(tankHandlers: Chain[TankHandler], hasCreative: Boolean) extends ListTankHandler(tankHandlers, true) {

    override protected def outputLog(logs: Chain[FluidTransferLog], action: FluidAction): Unit = {
      import org.apache.logging.log4j.util.Supplier
      if (action.execute() && Utils.isInDev) {
        FluidTank.LOGGER.debug(ModObjects.MARKER_Connection, (() => logs.mkString_(action.toString + " ", ", ", "")): Supplier[String])
      } else {
        FluidTank.LOGGER.trace(ModObjects.MARKER_Connection, (() => logs.mkString_(action.toString + " ", ", ", "")): Supplier[String])
      }
    }

    override def fill(resource: FluidAmount, action: FluidAction): FluidAmount =
      if (hasCreative) super.action(fillAll(getTankList), resource, action) else super.fill(resource, action)

    override def drain(toDrain: FluidAmount, action: FluidAction): FluidAmount =
      if (hasCreative) super.drain(toDrain, FluidAction.SIMULATE) else super.drain(toDrain, action)
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
    createAndInit(tanks)
  }
}
