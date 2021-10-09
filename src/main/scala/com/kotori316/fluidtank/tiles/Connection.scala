package com.kotori316.fluidtank.tiles

import cats.data._
import cats.implicits._
import com.kotori316.fluidtank._
import com.kotori316.fluidtank.blocks.TankPos
import com.kotori316.fluidtank.fluids.{DebugFluidHandler, FluidAmount, FluidTransferLog, ListTankHandler, TankHandler, fillAll}
import net.minecraft.util.Direction
import net.minecraft.util.math.{BlockPos, MathHelper}
import net.minecraft.util.text.{ITextComponent, StringTextComponent, TranslationTextComponent}
import net.minecraft.world.IBlockReader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.{Capability, CapabilityDispatcher, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler}

import scala.collection.mutable.ArrayBuffer

sealed class Connection private(s: Seq[TileTank]) extends ICapabilityProvider {
  val seq: Seq[TileTank] = s.sortBy(_.getPos.getY)
  val hasCreative: Boolean = seq.exists(_.isInstanceOf[TileTankCreative])
  val hasVoid: Boolean = seq.exists(_.isInstanceOf[TileTankVoid])
  val updateActions: ArrayBuffer[() => Unit] = ArrayBuffer(
    () => seq.foreach(_.markDirty())
  )

  val handler: ListTankHandler = new Connection.ConnectionTankHandler(Chain.fromSeq(seq.map(_.internalTank)), hasCreative)
  val isDummy: Boolean = false
  private[tiles] final var mIsValid = true

  val capabilities: Cap[CapabilityDispatcher] = if (seq.nonEmpty) {
    val event = new AttachCapabilitiesEvent[Connection](classOf[Connection], this)
    MinecraftForge.EVENT_BUS.post(event)
    if (event.getCapabilities.isEmpty) {
      Cap.empty
    } else {
      new CapabilityDispatcher(event.getCapabilities, event.getListeners).pure[Cap]
    }
  } else {
    Cap.empty
  }

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
    Option(fluidType).filter(_.nonEmpty)
  }

  def remove(tileTank: TileTank): Unit = {
    val (s1, s2) = seq.sortBy(_.getPos.getY).span(_ != tileTank)
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
      MathHelper.floor(amount.toDouble / capacity.toDouble * 14) + 1
    else 0
  }

  def updateNeighbors(): Unit = {
    updateActions.foreach(_.apply())
  }

  // ----- START DEPRECATED REMOVE IN 1.17 -----
  private[this] final val lazyOptional = LazyOptional.of(() => if (seq.nonEmpty) handler
  else if (Utils.isInDev) DebugFluidHandler.INSTANCE else EmptyFluidHandler.INSTANCE)

  private[tiles] def isValid = mIsValid

  //noinspection AccessorLikeMethodIsUnit
  private[tiles] def isValid_=(newValue: Boolean): Unit = {
    mIsValid = newValue
    if (!newValue) lazyOptional.invalidate()
  }

  // ----- END DEPRECATED REMOVE IN 1.17 -----

  //noinspection ComparingUnrelatedTypes
  override def getCapability[T](capability: Capability[T], facing: Direction): LazyOptional[T] = {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
      lazyOptional.cast()
    } else {
      capabilities.map(_.getCapability(capability, facing))
        .getOrElse(LazyOptional.empty())
        .value
    }
    /*Cap.asJava(
      Cap.empty[T]
        .orElse(if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) OptionT(Eval.always(Option.when(isValid)(handler.asInstanceOf[T]))) else Cap.empty)
        .orElse(capabilities.flatMap(_.getCapability(capability, facing).asScala))
    )*/
  }

  override def toString: String = {
    val name = getFluidStack.fold("null")(_.getLocalizedName)
    if (!hasCreative)
      s"Connection of $name : $amount / $capacity mB, Comparator outputs $getComparatorLevel."
    else
      s"Connection of $name in creative. Comparator outputs $getComparatorLevel."
  }

  def getTextComponent: ITextComponent = {
    if (hasCreative)
      new TranslationTextComponent("chat.fluidtank.connection_creative",
        getFluidStack.map(_.toStack.getDisplayName).getOrElse(new TranslationTextComponent("chat.fluidtank.empty")),
        Int.box(getComparatorLevel))
    else
      new TranslationTextComponent("chat.fluidtank.connection",
        getFluidStack.map(_.toStack.getDisplayName).getOrElse(new TranslationTextComponent("chat.fluidtank.empty")),
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
      val seq = s.sortBy(_.getPos.getY)
      // Property update
      if (seq.lengthIs > 1) {
        // HEAD
        val head = seq.head
        head.getWorld.setBlockState(head.getPos, head.getBlockState.`with`(TankPos.TANK_POS_PROPERTY, TankPos.BOTTOM))
        // LAST
        val last = seq.last
        last.getWorld.setBlockState(last.getPos, last.getBlockState.`with`(TankPos.TANK_POS_PROPERTY, TankPos.TOP))
        // MIDDLE
        seq.tail.init.foreach(t => t.getWorld.setBlockState(t.getPos, t.getBlockState.`with`(TankPos.TANK_POS_PROPERTY, TankPos.MIDDLE)))
      } else {
        // SINGLE
        seq.foreach(t => t.getWorld.setBlockState(t.getPos, t.getBlockState.`with`(TankPos.TANK_POS_PROPERTY, TankPos.SINGLE)))
      }
      new Connection(seq)
    }
  }

  @scala.annotation.tailrec
  def createAndInit(tankSeq: Seq[TileTank]): Unit = {
    if (tankSeq.nonEmpty) {
      val s = tankSeq.sortBy(_.getPos.getY)
      val fluid = s.map(_.internalTank.getFluid).find(_.nonEmpty).getOrElse(FluidAmount.EMPTY)
      val (s1, s2) = s.span(t => t.internalTank.getFluid.fluidEqual(fluid) || t.internalTank.getFluid.isEmpty)
      // Assert tanks in s1 have the same fluid.
      require(s1.map(_.internalTank.getFluid).forall(f => f.isEmpty || f.fluidEqual(fluid)))
      val content: FluidAmount = s1.foldMap(t => t.internalTank.drain(t.internalTank.getFluid, IFluidHandler.FluidAction.EXECUTE))
      val connection = Connection.create(s1)
      connection.handler.fill(content, IFluidHandler.FluidAction.EXECUTE)
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

    override def getTextComponent = new StringTextComponent(toString)
  }

  val stackFromTile: TileTank => Option[FluidAmount] = (t: TileTank) => Option(t.internalTank.getFluid).filter(_.nonEmpty)

  private class ConnectionTankHandler(tankHandlers: Chain[TankHandler], hasCreative: Boolean) extends ListTankHandler(tankHandlers, true) {

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

  def load(iBlockReader: IBlockReader, pos: BlockPos): Unit = {
    val lowest = Iterator.iterate(pos)(_.down()).takeWhile(p => iBlockReader.getTileEntity(p).isInstanceOf[TileTank])
      .toList.lastOption.getOrElse {
      FluidTank.LOGGER.fatal(ModObjects.MARKER_Connection, "No lowest tank", new IllegalStateException("No lowest tank"))
      pos
    }
    val tanks = Iterator.iterate(lowest)(_.up()).map(iBlockReader.getTileEntity).takeWhile(_.isInstanceOf[TileTank])
      .toList.map(_.asInstanceOf[TileTank])
    //    tanks.foldLeft(Connection.invalid) { case (c, tank) => c.add(tank, Direction.UP) }
    createAndInit(tanks)
  }
}
