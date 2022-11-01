package com.kotori316.fluidtank.tiles

import cats.data.Chain
import cats.implicits.toShow
import com.kotori316.fluidtank._
import com.kotori316.fluidtank.fluids.{FluidAmount, GenericAmount, ListTankHandler, Tank, TankHandler}
import com.kotori316.fluidtank.network.{PacketHandler, SideProxy, TileMessage}
import com.kotori316.fluidtank.render.Box
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.TickTask
import net.minecraft.util.Mth
import net.minecraft.world.Nameable
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.{LazyOptional, LogicalSidedProvider}
import net.minecraftforge.fml.LogicalSide

import scala.collection.mutable.ArrayBuffer

class TileTank(var tier: Tier, t: BlockEntityType[_ <: TileTank], p: BlockPos, s: BlockState)
  extends BlockEntity(t, p, s)
    with Nameable
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
  private final var mConnection: FluidConnection = FluidConnection.invalid
  final val connectionAttaches: ArrayBuffer[FluidConnection => Unit] = ArrayBuffer.empty
  var loading = false
  var stackName: Component = _

  def connection: FluidConnection = mConnection

  def connection_=(c: FluidConnection): Unit = {
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

  override def onDataPacket(n: net.minecraft.network.Connection, pkt: ClientboundBlockEntityDataPacket): Unit = handleUpdateTag(pkt.getTag)

  override def onLoad(): Unit = {
    super.onLoad()
    if (SideProxy.isServer(this)) {
      val executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)
      executor.tell(new TickTask(0, () => {
        getLevel.getProfiler.push("Connection Loading")
        if (Utils.isInDev) {
          FluidTank.LOGGER.debug(ModObjects.MARKER_TileTank,
            "Connection {} loaded in delayed task. At={}, connection={}",
            if (this.connection.isDummy) "will be" else "won't",
            this.getBlockPos.show, this.connection)
        }
        if (this.connection.isDummy) {
          Connection2.load(getLevel, getBlockPos, classOf[TileTank])
        }
        getLevel.getProfiler.pop()
      }))
    }
  }

  override def getCapability[T](capability: Capability[T], facing: Direction): LazyOptional[T] = {
    val c = connection.getCapability(capability, facing)
    if (c.isPresent) c else super.getCapability(capability, facing)
  }

  private def sendPacket(): Unit = {
    if (SideProxy.isServer(this)) PacketHandler.sendToClient(TileMessage(this), getLevel)
  }

  def hasContent: Boolean = internalTank.getFluid.nonEmpty

  def getComparatorLevel: Int = connection.getComparatorLevel

  def onBlockPlacedBy(): Unit = {
    if (Utils.isInDev) {
      FluidTank.LOGGER.debug(ModObjects.MARKER_TileTank,
        "Connection {} loaded in onBlockPlacedBy. At={}, connection={}",
        if (this.connection.isDummy) "will be" else "won't",
        this.getBlockPos.show, this.connection)
    }
    // Do nothing if the connection is already created.
    if (!this.connection.isDummy) return
    val downTank = Option(getLevel.getBlockEntity(getBlockPos.below())).collect { case t: TileTank => t }
    val upTank = Option(getLevel.getBlockEntity(getBlockPos.above())).collect { case t: TileTank => t }
    val newSeq = (downTank, upTank) match {
      case (Some(dT), Some(uT)) => dT.connection.sortedTanks :+ this :++ uT.connection.sortedTanks
      case (None, Some(uT)) => this +: uT.connection.sortedTanks
      case (Some(dT), None) => dT.connection.sortedTanks :+ this
      case (None, None) => Seq(this)
    }
    if (downTank.exists(_.connection.isDummy) || upTank.exists(_.connection.isDummy)) {
      Connection2.load(getLevel, getBlockPos, classOf[TileTank])
    } else {
      Connection2.createAndInit(newSeq)
    }
  }

  def onDestroy(): Unit = {
    this.connection.remove(this)
  }

  def getStackName: Option[Component] = Option(stackName)

  override def getName: Component = getStackName
    .getOrElse(new TextComponent(tier.toString + " Tank"))

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

    lazy val lowerBound: Double = Config.content.renderLowerBound.get().doubleValue()
    lazy val upperBound: Double = Config.content.renderUpperBound.get().doubleValue()

    // Util methods
    def getFluidAmount: Long = this.getTank.amount

    def getFluid: FluidAmount = this.getTank.genericAmount

    protected def capacity = this.getTank.capacity

    override def onContentsChanged(): Unit = {
      tile.sendPacket()
      if (!tile.loading)
        tile.connection.updateNeighbors()
      if (!SideProxy.isServer(tile) && capacity != 0) {
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
      val newTank = Tank(FluidAmount.fromNBT(nbt), nbt.getLong(TileTank.NBT_Capacity))
      setTank(newTank)
      self
    }

    def writeToNBT(nbt: CompoundTag): CompoundTag = {
      getFluid.write(nbt)
      nbt.putLong(TileTank.NBT_Capacity, self.capacity)
      nbt
    }

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

  implicit final val fluidConnectionHelper: ConnectionHelper.Aux[TileTank, Fluid, ListTankHandler] = FluidConnectionHelperImpl

  private final object FluidConnectionHelperImpl extends ConnectionHelper[TileTank] {
    override type Content = Fluid
    override type Handler = ListTankHandler
    override type ConnectionType = FluidConnection

    override def getPos(t: TileTank): BlockPos = t.getBlockPos

    override def isCreative(t: TileTank): Boolean = t.isInstanceOf[TileTankCreative]

    override def isVoid(t: TileTank): Boolean = t.isInstanceOf[TileTankVoid]

    override def setChanged(t: TileTank): Unit = t.setChanged()

    override def getContentRaw(t: TileTank): GenericAmount[Fluid] = t.internalTank.getFluid

    override def defaultAmount: GenericAmount[Fluid] = FluidAmount.EMPTY

    override def createHandler(s: Seq[TileTank]): ListTankHandler =
      new FluidConnection.ConnectionTankHandler(Chain.fromSeq(s.map(_.internalTank)), s.exists(isCreative))

    override def createConnection(s: Seq[TileTank]): FluidConnection = FluidConnection.create(s)

    override def connectionSetter(connection: FluidConnection): TileTank => Unit =
      t => t.connection = connection
  }
}
