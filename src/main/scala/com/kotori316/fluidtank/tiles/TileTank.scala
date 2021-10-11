package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.fluids.{FluidAmount, Tank, TankHandler}
import com.kotori316.fluidtank.network.{PacketHandler, SideProxy, TileMessage}
import com.kotori316.fluidtank.render.Box
import com.kotori316.fluidtank.{FluidTank, ModObjects, Utils}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.{MinecraftServer, TickTask}
import net.minecraft.world.Nameable
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fmllegacy.LogicalSidedProvider

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
  private final var mConnection: Connection = Connection.invalid
  final val connectionAttaches: ArrayBuffer[Connection => Unit] = ArrayBuffer.empty
  var loading = false
  var stackName: Component = _

  def connection: Connection = mConnection

  def connection_=(c: Connection): Unit = {
    mConnection = c
    connectionAttaches.foreach(_.apply(c))
  }

  override def save(compound: CompoundTag): CompoundTag = {
    compound.put(TileTank.NBT_Tank, internalTank.writeToNBT(new CompoundTag))
    compound.put(TileTank.NBT_Tier, tier.toNBTTag)
    getStackName.foreach(t => compound.putString(TileTank.NBT_StackName, Component.Serializer.toJson(t)))
    super.save(compound)
  }

  def getBlockTag: CompoundTag = {
    val nbt = save(new CompoundTag)
    Seq("x", "y", "z", "id").foreach(nbt.remove)
    nbt
  }

  override def getUpdateTag: CompoundTag = save(new CompoundTag)

  override def getUpdatePacket = new ClientboundBlockEntityDataPacket(getBlockPos, 0, getUpdateTag)

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
    if (loading) {
      loading = false
      if (SideProxy.isServer(this)) {
        val executor: MinecraftServer = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)
        executor.tell(new TickTask(executor.getTickCount, () => {
          getLevel.getProfiler.push("Connection Loading")
          if (Utils.isInDev) FluidTank.LOGGER.debug(ModObjects.MARKER_TileTank,
            "Connection load in delayed task. At={}, connection={}", this.getBlockPos, this.connection)
          if (this.connection.isDummy) {
            Connection.load(getLevel, getBlockPos)
          }
          getLevel.getProfiler.pop()
        }))
      }
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
  final val NBT_Tank = "tank"
  final val NBT_Tier = "tier"
  final val NBT_Capacity = "capacity"
  final val NBT_BlockTag = BlockItem.BLOCK_ENTITY_TAG
  final val NBT_StackName = "stackName"
  final val bcId = "buildcraftcore"
  final val ae2id = "appliedenergistics2"

  trait RealTank {
    self: TankHandler =>
    var box: Box = _

    def tile: TileTank

    // Util methods
    def getFluidAmount: Long = this.getTank.amount

    def getFluid: FluidAmount = this.getTank.fluidAmount

    protected def capacity = this.getTank.capacity

    override def onContentsChanged(): Unit = {
      tile.sendPacket()
      if (!tile.loading)
        tile.connection.updateNeighbors()
      if (!SideProxy.isServer(tile) && capacity != 0) {
        val percent = getFluidAmount.toDouble / capacity.toDouble
        val a = 0.001
        if (getFluidAmount > 0) {
          val d = 1d / 16d
          val (minY, maxY) = {
            val p = percent max a * 3
            if (this.getFluid.isGaseous) {
              (1d - p + a, 1d - a)
            } else {
              (a, p - a)
            }
          }
          box = Box(d * 8, minY, d * 8, d * 8, maxY, d * 8, d * 12 - 0.01, percent, d * 12 - 0.01, firstSide = true, endSide = true)
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

}
