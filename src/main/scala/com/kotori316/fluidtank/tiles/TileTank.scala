package com.kotori316.fluidtank.tiles

import cats.implicits.toShow
import com.kotori316.fluidtank._
import com.kotori316.fluidtank.fluids.{FluidAmount, Tank, TankHandler}
import com.kotori316.fluidtank.network.ClientSync
import com.kotori316.fluidtank.render.Box
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.util.Mth
import net.minecraft.world.Nameable
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState

import scala.collection.mutable.ArrayBuffer

class TileTank(var tier: Tier, t: BlockEntityType[_ <: TileTank], p: BlockPos, s: BlockState)
  extends BlockEntity(t, p, s)
    with Nameable
    with ClientSync
    /*with ICustomPipeConnection
    with IDebuggable*/ {
  self =>

  def this(p: BlockPos, s: BlockState) = {
    this(Tier.Invalid, ModObjects.TANK_TYPE, p, s)
  }

  def this(t: Tier, p: BlockPos, s: BlockState) = {
    this(t, ModObjects.TANK_TYPE, p, s)
  }

  val internalTank: TankHandler with TileTank.RealTank = new InternalTank(tier.amount)
  private final var mConnection: Connection = Connection.invalid
  final val connectionAttaches: ArrayBuffer[Connection => Unit] = ArrayBuffer.empty
  var loading = false
  var stackName: Component = _

  def connection: Connection = mConnection

  def connection_=(c: Connection): Unit = {
    mConnection = c
    connectionAttaches.foreach(_.apply(c))
  }

  override def saveAdditional(compound: CompoundTag): Unit = {
    compound.put(TileTank.NBT_Tank, internalTank.writeToNBT(new CompoundTag))
    compound.put(TileTank.NBT_Tier, tier.toNBTTag)
    getStackName.foreach(t => compound.putString(TileTank.NBT_StackName, Component.Serializer.toJson(t)))
    super.saveAdditional(compound)
  }

  def getBlockTag: CompoundTag = saveWithoutMetadata()

  override def getUpdateTag: CompoundTag = saveWithoutMetadata()

  override def getUpdatePacket: ClientboundBlockEntityDataPacket = ClientboundBlockEntityDataPacket.create(this)

  override def load(compound: CompoundTag): Unit = {
    super.load(compound)
    internalTank.readFromNBT(compound.getCompound(TileTank.NBT_Tank))
    tier = Tier.fromNBT(compound.get(TileTank.NBT_Tier))
    if (compound.contains(TileTank.NBT_StackName)) {
      stackName = Component.Serializer.fromJson(compound.getString(TileTank.NBT_StackName))
    }
    loading = true
  }

  def readNBTClient(compound: CompoundTag): Unit = {
    internalTank.readFromNBT(compound.getCompound(TileTank.NBT_Tank))
    tier = Tier.fromNBT(compound.get(TileTank.NBT_Tier))
    if (compound.contains(TileTank.NBT_StackName)) {
      stackName = Component.Serializer.fromJson(compound.getString(TileTank.NBT_StackName))
    } else {
      stackName = null
    }
  }

  override def fromClientTag(tag: CompoundTag): Unit = load(tag)

  override def toClientTag(tag: CompoundTag): CompoundTag = {
    saveAdditional(tag)
    tag
  }

  private def sendPacket(): Unit = {
    sync()
  }

  def hasContent: Boolean = internalTank.getFluid.nonEmpty

  def getComparatorLevel: Int = connection.getComparatorLevel

  def onBlockPlacedBy(): Unit = {
    val downTank = Option(getLevel.getBlockEntity(getBlockPos.below())).collect { case t: TileTank => t }
    val upTank = Option(getLevel.getBlockEntity(getBlockPos.above())).collect { case t: TileTank => t }
    val newSeq = (downTank, upTank) match {
      case (Some(dT), Some(uT)) => dT.connection.seq :+ this :++ uT.connection.seq
      case (None, Some(uT)) => this +: uT.connection.seq
      case (Some(dT), None) => dT.connection.seq :+ this
      case (None, None) => Seq(this)
    }
    if (downTank.exists(_.connection.isDummy) || upTank.exists(_.connection.isDummy)) {
      Connection.load(getLevel, getBlockPos)
    } else {
      Connection.createAndInit(newSeq)
    }
  }

  def onDestroy(): Unit = {
    this.connection.remove(this)
  }

  def getStackName: Option[Component] = Option(stackName)

  override def getName: Component = getStackName
    .getOrElse(Component.literal(tier.toString + " Tank"))

  override def hasCustomName: Boolean = stackName != null

  override def getCustomName: Component = getStackName.orNull

  class InternalTank(initialCapacity: Long) extends TankHandler with TileTank.RealTank {
    initCapacity(initialCapacity)

    override def tile: TileTank = self

    override def toString: String = {
      val fluid = getFluid
      if (fluid == null) "Tank : no fluid : Capacity = " + capacity
      else "Tank : " + fluid.getLocalizedName + " " + getFluidAmount + "mB : Capacity = " + capacity
    }
  }
}

object TileTank {
  final val NBT_Tank = "tank" // Tag map
  final val NBT_Tier = "tier" // Tag map provided in Tier class (Actually, String)
  final val NBT_Capacity = "capacity" // Long
  @deprecated("The use should be removed and use utility method in BlockItem or Utils", since = "1.18")
  final val NBT_BlockTag = Utils.BLOCK_ENTITY_TAG
  final val NBT_StackName = "stackName" // String parsed in Text
  final val bcId = "buildcraftcore"
  final val ae2id = "ae2"

  trait RealTank {
    self: TankHandler =>
    var box: Box = _

    def tile: TileTank

    lazy val lowerBound: Double = 0.001d
    lazy val upperBound: Double = 1d - 0.001d

    // Util methods
    def getFluidAmount: Long = this.getTank.amount

    def getFluid: FluidAmount = this.getTank.fluidAmount

    protected def capacity = this.getTank.capacity

    override def onContentsChanged(): Unit = {
      tile.sendPacket()
      if (!tile.loading)
        tile.connection.updateNeighbors()
      if (!Utils.isServer(tile) && capacity != 0) {
        if (getFluidAmount > 0) {
          val d = 1d / 16d
          val (minY, maxY) = getFluidHeight(capacity.toDouble, getFluidAmount.toDouble, lowerBound, upperBound, 0.003, getFluid.isGaseous)
          box = Box(startX = d * 8, startY = minY, startZ = d * 8, endX = d * 8, endY = maxY, endZ = d * 8,
            sizeX = d * 12 - 0.01, sizeY = maxY - minY, sizeZ = d * 12 - 0.01,
            firstSide = true, endSide = true)
        } else {
          box = null
        }
      }
    }

    def readFromNBT(nbt: CompoundTag): TankHandler with TileTank.RealTank = {
      val newTank: Tank = Tank(FluidAmount.fromNBT(nbt), nbt.getLong(TileTank.NBT_Capacity))
      setTank(newTank)
      self
    }

    def writeToNBT(nbt: CompoundTag): CompoundTag = {
      getFluid.write(nbt)
      nbt.putLong(TileTank.NBT_Capacity, self.capacity)
      nbt
    }

  }

  def tick(world: Level, pos: BlockPos, state: BlockState, tile: TileTank): Unit = {
    world.getProfiler.push("Connection Loading")
    if (Utils.isInDev) FluidTank.LOGGER.debug(ModObjects.MARKER_TileTank,
      "Connection load in delayed task. At={}, connection={}", pos.show, tile.connection)
    if (tile.connection.isDummy) {
      Connection.load(world, pos)
    }
    world.getProfiler.pop()
  }

  /**
   *
   * @param capacity   the capacity of tank. Must not be 0.
   * @param amount     the amount in the tank, assumed to be grater than 0. (amount > 0)
   * @param lowerBound the minimum of fluid position.
   * @param upperBound the maximum of fluid position.
   * @param isGaseous  whether the fluid is gas or not.
   * @return (minY, maxY)
   */
  def getFluidHeight(capacity: Double, amount: Double, lowerBound: Double, upperBound: Double, minRatio: Double, isGaseous: Boolean): (Double, Double) = {
    val ratio = Mth.clamp(amount / capacity, minRatio, 1)
    val height = (upperBound - lowerBound) * ratio
    if (isGaseous) {
      (upperBound - height, upperBound)
    } else {
      (lowerBound, lowerBound + height)
    }
  }
}
