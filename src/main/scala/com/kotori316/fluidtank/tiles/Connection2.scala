package com.kotori316.fluidtank.tiles

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEq}
import com.kotori316.fluidtank.fluids.{GenericAmount, ListHandler}
import com.kotori316.fluidtank.tiles.ConnectionHelper._
import com.kotori316.fluidtank.{Cap, FluidTank, ModObjects}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.{Capability, CapabilityDispatcher, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent

import scala.collection.mutable.ArrayBuffer

abstract class Connection2[TankType] protected(val sortedTanks: Seq[TankType]) extends ICapabilityProvider {
  implicit val helper: ConnectionHelper[TankType]

  val hasCreative: Boolean = sortedTanks.exists(_.isCreative)
  val hasVoid: Boolean = sortedTanks.exists(_.isVoid)
  val updateActions: ArrayBuffer[() => Unit] = ArrayBuffer(
    () => this.sortedTanks.foreach(_.setChanged())
  )
  val isDummy: Boolean = false
  protected val handler: helper.Handler = helper.createHandler(this.sortedTanks)

  val capabilities: Cap[CapabilityDispatcher] = if (sortedTanks.nonEmpty) {
    val event = new AttachCapabilitiesEvent[Connection2[_]](classOf[Connection2[_]], this)
    MinecraftForge.EVENT_BUS.post(event)
    if (event.getCapabilities.isEmpty) {
      Cap.empty
    } else {
      new CapabilityDispatcher(event.getCapabilities, event.getListeners).pure[Cap]
    }
  } else {
    Cap.empty
  }

  protected def contentType: GenericAmount[helper.Content] =
    this.sortedTanks.headOption.flatMap(t => helper.getContent(t))
      .orElse(this.sortedTanks.lastOption.flatMap(t => helper.getContent(t)))
      .getOrElse(helper.defaultAmount)

  def capacity: Long = if (hasCreative) Tier.CREATIVE.amount else handler.getSumOfCapacity

  def amount: Long = if (hasCreative && contentType.nonEmpty) Tier.CREATIVE.amount else
    this.sortedTanks.flatMap(_.getContent(helper.asInstanceOf[Aux[TankType, Any, ListHandler[Any]]])).map(_.amount).sum

  def getContent: Option[GenericAmount[helper.Content]] =
    Option(contentType).filter(_.nonEmpty).map(_.setAmount(this.amount))

  protected var isValid: Boolean = true

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    capabilities.map(_.getCapability(cap, side))
      .getOrElse(LazyOptional.empty())
      .value
  }

  def remove(tank: TankType): Unit = {
    val (s1, s2) = this.sortedTanks.span(_ != tank)
    val s1Connection = this.helper.createConnection(s1)
    val s2Connection = this.helper.createConnection(s2.tail)
    this.invalidate()

    s1.foreach(this.helper.connectionSetter(s1Connection))
    s2.tail.foreach(this.helper.connectionSetter(s2Connection))
  }

  protected def invalidate(): Unit = {
    this.isValid = false
  }

  def getComparatorLevel: Int = {
    if (amount > 0)
      Mth.floor(amount.toDouble / capacity.toDouble * 14) + 1
    else 0
  }

  def updateNeighbors(): Unit = {
    updateActions.foreach(_.apply())
  }

  override def toString: String = {
    val name = getContent.fold("null")(_.getLocalizedName)
    if (!hasCreative)
      s"Connection of $name : $amount / $capacity mB, Comparator outputs $getComparatorLevel."
    else
      s"Connection of $name in creative. Comparator outputs $getComparatorLevel."
  }

  def getTextComponent: Component
}

object Connection2 {

  import net.minecraftforge.fluids.capability.IFluidHandler

  @scala.annotation.tailrec
  def createAndInit[TankType, ContentType, HandlerType <: ListHandler[ContentType]]
  (tankSeq: Seq[TankType])(implicit helper: ConnectionHelper.Aux[TankType, ContentType, HandlerType]): Unit = {
    if (tankSeq.nonEmpty) {
      val sorted = tankSeq.sortBy(_.getPos.getY)
      val kind = sorted.flatMap(_.getContent).find(_.nonEmpty)
      val (s1, s2) = sorted.span { t =>
        val c = t.getContent
        c.isEmpty || c === kind
      }
      require(s1.map(_.getContent).forall(c => c.isEmpty || c === kind))
      val connection = helper.createConnection(s1)
      val content = connection.handler.drain(connection.helper.defaultAmount.setAmount(Long.MaxValue), IFluidHandler.FluidAction.EXECUTE)
      connection.handler.fill(content, IFluidHandler.FluidAction.EXECUTE)
      s1 foreach helper.connectionSetter(connection)

      if (s2.nonEmpty) createAndInit(s2)
    }
  }

  def load[TankType <: BlockEntity, ContentType, HandlerType <: ListHandler[ContentType]]
  (level: BlockGetter, pos: BlockPos, tankClass: Class[TankType])(implicit helper: ConnectionHelper.Aux[TankType, ContentType, HandlerType]): Unit = {
    val lowest = Iterator.iterate(pos)(_.below()).takeWhile(p => tankClass.isInstance(level.getBlockEntity(p)))
      .toList.lastOption.getOrElse {
      FluidTank.LOGGER.fatal(ModObjects.MARKER_Connection, "No lowest tank", new IllegalStateException("No lowest tank"))
      pos
    }
    val tanks = Iterator.iterate(lowest)(_.above()).map(level.getBlockEntity).takeWhile(tankClass.isInstance)
      .toList.map(tankClass.cast)
    //    tanks.foldLeft(Connection.invalid) { case (c, tank) => c.add(tank, Direction.UP) }
    createAndInit(tanks)
  }
}
