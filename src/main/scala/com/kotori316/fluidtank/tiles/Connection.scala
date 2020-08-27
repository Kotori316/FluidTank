package com.kotori316.fluidtank.tiles

import cats._
import cats.data._
import cats.implicits._
import com.kotori316.fluidtank._
import com.kotori316.fluidtank.tiles.CapabilityFluidTank.EmptyTank
import net.minecraft.util.Direction
import net.minecraft.util.math.{BlockPos, MathHelper}
import net.minecraft.util.text.{ITextComponent, StringTextComponent, TranslationTextComponent}
import net.minecraft.world.IBlockReader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.{Capability, CapabilityDispatcher, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler

import scala.collection.mutable.ArrayBuffer

sealed class Connection(s: Seq[TileTankNoDisplay]) extends ICapabilityProvider {
  val seq: Seq[TileTankNoDisplay] = s.sortBy(_.getPos.getY)
  val hasCreative: Boolean = seq.exists(_.isInstanceOf[TileTankCreative])
  val hasVoid: Boolean = seq.exists(_.isInstanceOf[TileTankVoid])
  val updateActions: ArrayBuffer[() => Unit] = ArrayBuffer(
    () => seq.foreach(_.markDirty())
  )

  class TankHandler extends FluidAmount.Tank {
    type LogType[A] = Chain[A]

    /**
     * @return Fluid that was accepted by the tank.
     */
    override def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Int): FluidAmount = {
      if (hasCreative) {
        val totalLong = tankSeq(fluidAmount).map(_.tank.fill(fluidAmount.setAmount(Int.MaxValue), doFill).amount).sum
        val total = Utils.toInt(Math.min(totalLong, fluidAmount.amount))
        return fluidAmount.setAmount(total)
      }
      val rest = capacity - amount
      if (rest == 0 && !hasVoid) return FluidAmount.EMPTY
      if (fluidAmount.isEmpty || fluidAmount.amount < min || rest < min) return FluidAmount.EMPTY
      if (!seq.headOption.exists(_.tank.canFillFluidType(fluidAmount))) return FluidAmount.EMPTY

      def internal(tanks: List[TileTankNoDisplay], toFill: FluidAmount, filled: FluidAmount): Writer[LogType[String], FluidAmount] = {
        if (toFill.isEmpty) {
          Writer.tell("Filled".pure[LogType]).map(_ => filled)
        } else {
          tanks match {
            case Nil =>
              val message = if (filled.isEmpty) s"Filling $toFill failed." else s"Filled, Amount: ${filled.show}"
              Writer.apply(message.pure[LogType], filled)
            case head :: tail =>
              val fill = head.tank.fill(toFill, doFill)
              Writer.tell(s"Filled ${fill.show} to ${head.getPos.show}".pure[LogType]) >>= { _ => internal(tail, toFill - fill, filled + fill) }
          }
        }
      }

      internal(tankSeq(fluidAmount).toList, fluidAmount, FluidAmount.EMPTY).run match {
        case (messages, filled) =>
          log(doFill, messages)
          filled
      }

    }

    /**
     * @param fluidAmount the fluid representing the kind and maximum amount to drain.
     *                    Empty Fluid means fluid type can be anything.
     * @param doDrain     false means simulating.
     * @param min         minimum amount to drain.
     * @return the fluid and amount that is (or will be) drained.
     */
    override def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Int): FluidAmount = {
      if (fluidAmount.amount < min || fluidType.isEmpty) return FluidAmount.EMPTY
      if (hasCreative) {
        if (FluidAmount.EMPTY.fluidEqual(fluidAmount) || fluidType.fluidEqual(fluidAmount)) {
          val m = s"Drained $fluidAmount from ${tankSeq(fluidAmount).head.getPos.show} in creative connection."
          log(doDrain, m.pure[LogType])
          return fluidType.setAmount(fluidAmount.amount)
        } else {
          return FluidAmount.EMPTY
        }
      }

      def internal(tanks: List[TileTankNoDisplay], toDrain: FluidAmount, drained: FluidAmount): Writer[LogType[String], FluidAmount] = {
        if (toDrain.amount <= 0) {
          val message = if (drained.isEmpty) "Drain failed." else "Drain Finished."
          Writer.apply(message.pure[LogType], drained)
        } else {
          tanks match {
            case Nil => for (_ <- Writer.tell(s"Drain Finished. Total amount is ${drained.show}".pure[LogType])) yield drained
            case head :: tl =>
              val drain = head.tank.drain(toDrain, doDrain)
              for {
                _ <- Writer.tell(s"Drained ${drain.show} from ${head.getPos.show}".pure[LogType])
                fluid <- internal(tl, toDrain - drain, drained + drain)
              } yield fluid
          }
        }
      }

      internal(tankSeq(fluidType).reverse.toList, fluidAmount, FluidAmount.EMPTY).run match {
        case (messages, drained) =>
          log(doDrain, messages)
          drained
      }
    }

    private def log(real: Boolean, messages: LogType[String]): Unit = {
      import org.apache.logging.log4j.util.Supplier
      val s = if (real) " Real" else " Simulate"
      if (real && Utils.isInDev) {
        FluidTank.LOGGER.debug(ModObjects.MARKER_Connection, (() => messages.mkString_(", ") + s): Supplier[String])
      } else {
        FluidTank.LOGGER.trace(ModObjects.MARKER_Connection, (() => messages.mkString_(", ") + s): Supplier[String])
      }
    }

    override def getFluidInTank(tank: Int): FluidStack = fluidType.setAmount(Utils.toInt(amount)).toStack

    override def getTankCapacity(tank: Int): Int = Utils.toInt(amount)
  }

  val handler: FluidAmount.Tank = new TankHandler
  private[tiles] final var isValid = true

  val capabilities: Cap[CapabilityDispatcher] = if (s.nonEmpty) {
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

  def capacity: Long = if (hasCreative) Tiers.CREATIVE.amount else seq.map(_.tier.amount).sum

  def amount: Long = if (hasCreative && fluidType.nonEmpty) Tiers.CREATIVE.amount else seq.map(_.tank.getFluidAmount).sum

  def tankSeq(fluid: FluidAmount): Seq[TileTankNoDisplay] = {
    if (fluid != null && fluid.isGaseous) {
      seq.reverse
    } else {
      seq
    }
  }

  def getFluidStack: Option[FluidAmount] = {
    Option(fluidType).filter(_.nonEmpty)
  }

  def remove(tileTank: TileTankNoDisplay): Unit = {
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

  override def getCapability[T](capability: Capability[T], facing: Direction): LazyOptional[T] = {
    Cap.asJava(
      Cap.empty[T]
        .orElse(if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) OptionT(Eval.always(Option.when(isValid)(handler.asInstanceOf[T]))) else Cap.empty)
        .orElse(CapabilityFluidTank.cap.make[T](capability, handler))
        .orElse(capabilities.flatMap(_.getCapability(capability, facing).asScala))
    )
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

  def create(s: Seq[TileTankNoDisplay]): Connection = {
    if (s.isEmpty) invalid
    else new Connection(s)
  }

  @scala.annotation.tailrec
  def createAndInit(s: Seq[TileTankNoDisplay]): Unit = {
    if (s.nonEmpty) {
      val fluid = LazyList.from(s).map(_.tank.getFluid).find(_.nonEmpty).getOrElse(FluidAmount.EMPTY)
      val (s1, s2) = s.span(t => t.tank.getFluid.fluidEqual(fluid) || t.tank.getFluid.isEmpty)
      // Assert tanks in s1 have the same fluid.
      require(s1.map(_.tank.getFluid).forall(f => f.isEmpty || f.fluidEqual(fluid)))
      val content: FluidAmount = (for (t <- s1) yield t.tank.drain(t.tank.getFluid, doDrain = true)).reduce(_ + _)
      val connection = Connection.create(s1)
      connection.handler.fill(content, doFill = true)
      s1.foreach { t =>
        t.connection.isValid = false
        t.connection = connection
      }
      if (s2.nonEmpty) createAndInit(s2)
    }
  }

  val invalid: Connection = new Connection(Nil) {
    override def fluidType: FluidAmount = FluidAmount.EMPTY

    override def capacity: Long = 0

    override def amount: Long = 0

    override val handler: FluidAmount.Tank = EmptyTank.INSTANCE
    override val toString: String = "Connection.Invalid"

    override def getCapability[T](capability: Capability[T], facing: Direction): LazyOptional[T] = {
      if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        LazyOptional.of(() => handler).cast()
      else
        super.getCapability(capability, facing)
    }

    override def getComparatorLevel: Int = 0

    override def remove(tileTank: TileTankNoDisplay): Unit = ()

    override def getTextComponent = new StringTextComponent(toString)
  }

  val stackFromTile: TileTankNoDisplay => Option[FluidAmount] = (t: TileTankNoDisplay) => Option(t.tank.getFluid).filter(_.nonEmpty)

  def load(iBlockReader: IBlockReader, pos: BlockPos): Unit = {
    val lowest = Iterator.iterate(pos)(_.down()).takeWhile(p => iBlockReader.getTileEntity(p).isInstanceOf[TileTankNoDisplay])
      .toList.lastOption.getOrElse {
      FluidTank.LOGGER.fatal(ModObjects.MARKER_Connection, "No lowest tank", new IllegalStateException("No lowest tank"))
      pos
    }
    val tanks = Iterator.iterate(lowest)(_.up()).map(iBlockReader.getTileEntity).takeWhile(_.isInstanceOf[TileTankNoDisplay])
      .toList.map(_.asInstanceOf[TileTankNoDisplay])
    //    tanks.foldLeft(Connection.invalid) { case (c, tank) => c.add(tank, Direction.UP) }
    createAndInit(tanks)
  }
}
