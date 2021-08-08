package com.kotori316.fluidtank.tank

import java.util
import java.util.Collections

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.{FluidAmount => BCAmount}
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.{FluidKey, FluidVolume}
import alexiil.mc.lib.attributes.fluid.{FluidVolumeUtil, GroupedFluidInv, GroupedFluidInvView}
import com.kotori316.fluidtank._
import net.minecraft.text.{LiteralText, Text, TranslatableText}
import net.minecraft.util.math.{BlockPos, MathHelper}
import net.minecraft.world.BlockView

import scala.collection.mutable.ArrayBuffer

sealed class Connection(s: Seq[TileTank]) {
  val seq: Seq[TileTank] = s.sortBy(_.getPos.getY)
  val hasCreative: Boolean = seq.exists(_.isInstanceOf[TileTankCreative])
  val updateActions: ArrayBuffer[() => Unit] = ArrayBuffer(
    () => seq.foreach(_.markDirty())
  )

  class TankHandler extends GroupedFluidInv {
    type LogType[A] = List[A]

    /**
     * @return Fluid that was accepted by the tank.
     */
    def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Long = 0): FluidAmount = {
      if (hasCreative) {
        val totalLong = tankSeq(fluidAmount).map(_.tank.fill(fluidAmount.setAmount(Int.MaxValue), doFill))
          .map(_.fluidVolume.amount())
          .reduce(_ add _)
        val total = totalLong min fluidAmount.fluidVolume.amount()
        return fluidAmount.setAmount(total)
      }
      val rest = capacity - amount
      if (fluidAmount.isEmpty || fluidAmount.fluidVolume.amount().asLong(FluidAmount.AMOUNT_BUCKET) < min || rest < min) return FluidAmount.EMPTY
      if (!seq.headOption.exists(_.tank.canFillFluidType(fluidAmount))) return FluidAmount.EMPTY

      @scala.annotation.tailrec
      def internal(tanks: List[TileTank], toFill: FluidAmount, filled: FluidAmount): FluidAmount = {
        if (toFill.isEmpty) {
          filled
        } else {
          tanks match {
            case Nil =>
              filled
            case head :: tail =>
              val fill = head.tank.fill(toFill, doFill)
              internal(tail, toFill - fill, filled + fill)
          }
        }
      }

      internal(tankSeq(fluidAmount).toList, fluidAmount, FluidAmount.EMPTY)
    }

    /**
     * @param fluidAmount the fluid representing the kind and maximum amount to drain.
     *                    Empty Fluid means fluid type can be anything.
     * @param doDrain     false means simulating.
     * @param min         minimum amount to drain.
     * @return the fluid and amount that is (or will be) drained.
     */
    def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Long = 0): FluidAmount = {
      if (fluidAmount.fluidVolume.amount().asLong(FluidAmount.AMOUNT_BUCKET) < min || fluidType.isEmpty) return FluidAmount.EMPTY
      if (hasCreative) {
        if (FluidAmount.EMPTY.fluidEqual(fluidAmount) || fluidType.fluidEqual(fluidAmount)) {
          val m = s"Drained $fluidAmount from ${tankSeq(fluidAmount).head.getPos} in creative connection."
          log(doDrain, List(m))
          if (fluidAmount.fluidVolume.amount() == BCAmount.MAX_BUCKETS)
            return fluidType.setAmount(fluidAmount.fluidVolume.amount().sub(1))
          else
            return fluidType.setAmount(fluidAmount.fluidVolume.amount())
        } else {
          return FluidAmount.EMPTY
        }
      }

      @scala.annotation.tailrec
      def internal(tanks: List[TileTank], toDrain: FluidAmount, drained: FluidAmount): FluidAmount = {
        if (toDrain.fluidVolume.amount().asLong(FluidAmount.AMOUNT_BUCKET) <= 0) {
          log(doDrain, List(s"Drained $drained"))
          drained
        } else {
          tanks match {
            case Nil =>
              log(doDrain, List(s"Drained $drained, rest $toDrain"))
              drained
            case ::(head, tl) =>
              val drain = head.tank.drain(toDrain, doDrain)
              internal(tl, toDrain - drain, drained + drain)
          }
        }
      }

      internal(tankSeq(fluidType).reverse.toList, fluidAmount, FluidAmount.EMPTY)
    }

    private def log(real: Boolean, messages: LogType[String]): Unit = {
      import org.apache.logging.log4j.util.Supplier
      if (real) {
        ModTank.LOGGER.debug((() => messages.mkString(", ") + " Real"): Supplier[String])
      } else {
        ModTank.LOGGER.trace((() => messages.mkString(", ") + " Simulate"): Supplier[String])
      }
    }

    override def attemptInsertion(fluidVolume: FluidVolume, simulation: Simulation): FluidVolume = {
      val filled = fill(FluidAmount(fluidVolume), simulation.isAction).fluidVolume
      fluidVolume.withAmount(fluidVolume.amount() sub filled.amount())
    }

    override def attemptExtraction(filter: FluidFilter, maxAmount: BCAmount, simulation: Simulation): FluidVolume = {
      if (fluidType.nonEmpty && filter.matches(fluidType.fluidVolume.getFluidKey)) {
        val volume = drain(FluidAmount(fluidType.fluidVolume.withAmount(maxAmount)), simulation.isAction).fluidVolume
        volume
      } else {
        FluidVolumeUtil.EMPTY
      }
    }

    override def getStoredFluids: util.Set[FluidKey] = Collections.singleton(fluidType.fluidVolume.fluidKey)

    override def getStatistics(fluidFilter: FluidFilter): GroupedFluidInvView.FluidInvStatistic = {
      if (fluidFilter.matches(fluidType.fluidVolume.fluidKey)) {
        val cap = BCAmount.of(capacity, FluidAmount.AMOUNT_BUCKET)
        val am = BCAmount.of(amount, FluidAmount.AMOUNT_BUCKET)
        val rest = cap sub am
        new GroupedFluidInvView.FluidInvStatistic(fluidFilter, am, rest, cap)
      } else {
        GroupedFluidInvView.FluidInvStatistic.emptyOf(fluidFilter)
      }
    }

  }

  val handler = new TankHandler

  protected def fluidType: FluidAmount = {
    seq.headOption.flatMap(Connection.stackFromTile).orElse(seq.lastOption.flatMap(Connection.stackFromTile)).getOrElse(FluidAmount.EMPTY)
  }

  def capacity: Long = if (hasCreative) Tiers.CREATIVE.amount else seq.map(_.tier.amount).sum

  def amount: Long = if (hasCreative && fluidType.nonEmpty) Tiers.CREATIVE.amount else seq.map(_.tank.getFluidAmount).sum

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
    val (s1, s2) = seq.sortBy(_.getPos.getY).span(_ != tileTank)
    val s1Connection = Connection.create(s1)
    val s2Connection = Connection.create(s2.tail)
    s1.foreach(_.connection = s1Connection)
    s2.tail.foreach(_.connection = s2Connection)
  }

  def getComparatorLevel: Int = {
    if (amount > 0)
      MathHelper.floor(amount.toDouble / capacity.toDouble * 14) + 1
    else 0
  }

  def updateNeighbors(): Unit = {
    updateActions.foreach(_.apply())
  }

  override def toString: String = {
    val name = getFluidStack.fold("null")(_.getLocalizedName)
    if (!hasCreative)
      s"$name : $amount / $capacity mB, Comparator outputs $getComparatorLevel."
    else
      s"$name in creative. Comparator outputs $getComparatorLevel."
  }

  def getTextComponent: Text = {
    if (hasCreative)
      new TranslatableText("chat.fluidtank.connection_creative",
        getFluidStack.map(_.getLocalizedName).getOrElse(new TranslatableText("chat.fluidtank.empty")),
        Int.box(getComparatorLevel))
    else
      new TranslatableText("chat.fluidtank.connection",
        getFluidStack.map(_.getLocalizedName).getOrElse(new TranslatableText("chat.fluidtank.empty")),
        Long.box(amount),
        Long.box(capacity),
        Int.box(getComparatorLevel))
  }
}

object Connection {

  def create(s: Seq[TileTank]): Connection = {
    if (s.isEmpty) invalid
    else new Connection(s)
  }

  @scala.annotation.tailrec
  def createAndInit(s: Seq[TileTank]): Unit = {
    if (s.nonEmpty) {
      val fluid = s.map(_.tank.getFluid).find(_.nonEmpty).getOrElse(FluidAmount.EMPTY)
      val (s1, s2) = s.span(t => t.tank.getFluid.fluidEqual(fluid) || t.tank.getFluid.isEmpty)
      // Assert tanks in s1 have the same fluid.
      require(s1.map(_.tank.getFluid).forall(f => f.isEmpty || f.fluidEqual(fluid)))
      val content: FluidAmount = (for (t <- s1) yield t.tank.drain(t.tank.getFluid, doDrain = true)).reduce(_ + _)
      val connection = Connection.create(s1)
      connection.handler.fill(content, doFill = true)
      s1.foreach(_.connection = connection)
      if (s2.nonEmpty) createAndInit(s2)
    }
  }

  val invalid: Connection = new Connection(Nil) {
    override def fluidType: FluidAmount = FluidAmount.EMPTY

    override def capacity: Long = 0

    override def amount: Long = 0

    override val toString: String = "Connection.Invalid"

    override def getComparatorLevel: Int = 0

    override def remove(tileTank: TileTank): Unit = ()

    override def getTextComponent = new LiteralText(toString)
  }

  val stackFromTile: TileTank => Option[FluidAmount] = (t: TileTank) => Option(t.tank.getFluid).filter(_.nonEmpty)

  def load(iBlockReader: BlockView, pos: BlockPos): Unit = {
    val lowest = Iterator.iterate(pos)(_.down()).takeWhile(p => iBlockReader.getBlockEntity(p).isInstanceOf[TileTank])
      .toList.lastOption.getOrElse {
      ModTank.LOGGER.fatal("No lowest tank", new IllegalStateException("No lowest tank"))
      pos
    }
    val tanks = Iterator.iterate(lowest)(_.up()).map(iBlockReader.getBlockEntity).takeWhile(_.isInstanceOf[TileTank])
      .toList.map(_.asInstanceOf[TileTank])
    //    tanks.foldLeft(Connection.invalid) { case (c, tank) => c.add(tank, Direction.UP) }
    createAndInit(tanks)
  }
}
