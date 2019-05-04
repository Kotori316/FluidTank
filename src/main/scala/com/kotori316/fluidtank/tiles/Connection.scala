package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank._
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.{BlockPos, MathHelper}
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.{Capability, CapabilityDispatcher, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent

import scala.collection.mutable.ArrayBuffer

sealed class Connection(s: Seq[TileTankNoDisplay]) extends ICapabilityProvider {
  val seq: Seq[TileTankNoDisplay] = s.sortBy(_.getPos.getY)
  val hasCreative = seq.exists(_.isInstanceOf[TileTankCreative])
  val updateActions: ArrayBuffer[() => Unit] = ArrayBuffer(
    () => seq.foreach(_.markDirty())
  )

  val handler: FluidAmount.Tank = new FluidAmount.Tank {
    /**
      * @return Fluid that was accepted by the tank.
      */
    override def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Int): FluidAmount = {
      if (fluidAmount.isEmpty || fluidAmount.amount < min) return FluidAmount.EMPTY
      var total = 0
      var resource = fluidAmount
      if (hasCreative) {
        val totalLong = tankSeq(fluidAmount).map(_.tank.fill(resource.setAmount(Int.MaxValue), doFill).amount.toLong).sum
        total = Utils.toInt(Math.min(totalLong, resource.amount))
      } else {
        for (tileTank <- tankSeq(fluidAmount)) {
          if (resource.amount > 0) {
            val filled = tileTank.tank.fill(resource, doFill)
            total += filled.amount
            resource = resource - filled
          }
        }
      }
      fluidAmount.setAmount(total)
    }

    /**
      * @param fluidAmount the fluid representing the kind and maximum amount to drain.
      *                    Empty Fluid means fluid type can be anything.
      * @param doDrain     false means simulating.
      * @param min         minimum amount to drain.
      * @return the fluid and amount that is (or will be) drained.
      */
    override def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Int): FluidAmount = {
      if (fluidAmount.amount < min) return FluidAmount.EMPTY

      var resource = fluidAmount
      var total = FluidAmount.EMPTY
      for (tileTank <- tankSeq(fluidType).reverse) {
        if (resource.amount > 0) {
          val drained = tileTank.tank.drain(resource, doDrain)
          if (drained.nonEmpty) {
            resource -= drained
            total += drained
          }
        }
      }
      total
    }
  }

  val capabilities = if (s.nonEmpty) {
    val event = new AttachCapabilitiesEvent[Connection](classOf[Connection], this)
    MinecraftForge.EVENT_BUS.post(event)
    Option(event.getCapabilities).filterNot(_.isEmpty).map(t => new CapabilityDispatcher(t, event.getListeners))
  } else {
    None
  }


  protected def fluidType: FluidAmount = {
    seq.headOption.flatMap(Connection.stackFromTile).orElse(seq.lastOption.flatMap(Connection.stackFromTile)).orNull
  }

  def capacity: Long = if (hasCreative) Tiers.CREATIVE.amount else seq.map(_.tier.amount).sum

  def amount: Long = if (hasCreative && fluidType != null) Tiers.CREATIVE.amount else seq.map(_.tank.getFluidAmount.toLong).sum

  def tankSeq(fluid: FluidAmount): Seq[TileTankNoDisplay] = {
    if (fluid != null && fluid.isGaseous(fluid)) {
      seq.reverse
    } else {
      seq
    }
  }

  def getFluidStack: Option[FluidAmount] = {
    Option(fluidType).filter(_.nonEmpty)
  }

  /**
    * Make connection.
    *
    * @param tileTank The tank added to this connection.
    * @param facing   The facing that the tank should be connected to. UP and DOWN are valid.
    * @return new connection
    */
  def add(tileTank: TileTankNoDisplay, facing: EnumFacing): Connection = {
    val newFluid = tileTank.tank.getFluid
    if (newFluid.isEmpty || fluidType.isEmpty || fluidType.fluidEqual(newFluid)) {
      // You can connect the tank to this connection.
      if (seq.contains(tileTank) || seq.exists(_.getPos == tileTank.getPos)) {
        FluidTank.LOGGER.warn(s"${tileTank.getClass.getName} at ${tileTank.getPos} is already added to connection.")
        return this
      }
      val newSeq = if (facing == EnumFacing.DOWN) {
        tileTank +: seq
      } else {
        seq :+ tileTank
      }
      val connection = new Connection(newSeq)
      val fluidStacks = for (t <- newSeq; i <- Option(t.tank.drain(t.tank.getFluid, doDrain = true))) yield i
      newSeq.foreach(t => {
        t.connection = connection
        t.tank.setFluid(null)
      })
      fluidStacks.foreach(connection.handler.fill(_, doFill = true))
      connection
    } else {
      // You have to make new connection.
      val connection = new Connection(Seq(tileTank))
      tileTank.connection = connection
      connection
    }
  }

  def add(connection: Connection, facing: EnumFacing): Connection = {
    val newFluid = connection.fluidType
    if (newFluid.isEmpty || fluidType.isEmpty || fluidType.fluidEqual(newFluid)) {
      if (seq.exists(connection.seq.contains)) {
        FluidTank.LOGGER.warn(s"Connection($seq) has same block with ${connection.seq}.")
        return connection
      }
      val newSeq = if (facing == EnumFacing.DOWN) {
        connection.seq ++ this.seq
      } else {
        this.seq ++ connection.seq
      }
      val nConnection = new Connection(newSeq)
      val fluidStacks = for (t <- newSeq; i <- Option(t.tank.drain(t.tank.getFluid, doDrain = true))) yield i
      newSeq.foreach(t => {
        t.connection = nConnection
        t.tank.setFluid(null)
      })
      fluidStacks.foreach(nConnection.handler.fill(_, doFill = true))
      nConnection
    } else {
      // Nothing to change.
      connection
    }
  }

  def remove(tileTank: TileTankNoDisplay): Unit = {
    val (s1, s2) = seq.sortBy(_.getPos.getY).span(_ != tileTank)
    s1.foldLeft(Connection.invalid) { case (c, tank) => c.add(tank, EnumFacing.UP) }
    s2.tail.foldLeft(Connection.invalid) { case (c, tank) => c.add(tank, EnumFacing.UP) }
  }

  def getComparatorLevel: Int = {
    if (amount > 0)
      MathHelper.floor(amount.toDouble / capacity.toDouble * 14) + 1
    else 0
  }

  def updateNeighbors(): Unit = {
    updateActions.foreach(_.apply())
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing) = {
    capabilities.map(_.getCapability(capability, facing)).getOrElse(LazyOptional.empty())
    //    CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() => handler))
    //      .or(capabilities.map(_.getCapability(capability, facing)).getOrElse(LazyOptional.empty()))
  }

  override def toString: String = {
    val name = getFluidStack.fold("null")(_.getLocalizedName)
    if (!hasCreative)
      s"Connection of $name : $amount / $capacity mB, Comparator outputs $getComparatorLevel."
    else
      s"Connection of $name in creative. Comparator outputs $getComparatorLevel."
  }
}

object Connection {

  val invalid: Connection = new Connection(Nil) {
    override def fluidType: FluidAmount = FluidAmount.EMPTY

    override def capacity: Long = 0

    override def amount: Long = 0

    override val handler: FluidAmount.Tank = new FluidAmount.Tank {
      override def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Int) = FluidAmount.EMPTY

      override def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Int) = FluidAmount.EMPTY
    }
    override val toString: String = "Connection.Invalid"

    override def getComparatorLevel: Int = 0

    override def remove(tileTank: TileTankNoDisplay): Unit = ()
  }

  val stackFromTile = (t: TileTankNoDisplay) => Option(t.tank.getFluid)

  def load(world: World, pos: BlockPos): Unit = {
    val lowest = Iterator.iterate(pos)(_.down()).takeWhile(p => world.getTileEntity(p).isInstanceOf[TileTankNoDisplay])
      .toList.lastOption.getOrElse({
      FluidTank.LOGGER.fatal("No lowest tank", new IllegalArgumentException("No lowest tank"))
      pos
    })
    val tanks = Iterator.iterate(lowest)(_.up()).map(world.getTileEntity).takeWhile(_.isInstanceOf[TileTankNoDisplay])
      .toList.map(_.asInstanceOf[TileTankNoDisplay])
    tanks.foldLeft(Connection.invalid) { case (c, tank) => c.add(tank, EnumFacing.UP) }
  }
}
