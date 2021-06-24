package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.fluids.{FluidAmount, Tank, TankHandler}
import com.kotori316.fluidtank.network.{PacketHandler, SideProxy, TileMessage}
import com.kotori316.fluidtank.render.Box
import com.kotori316.fluidtank.{Config, ModObjects}
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SUpdateTileEntityPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.tileentity.{TileEntity, TileEntityType}
import net.minecraft.util.concurrent.TickDelayedTask
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.{ITextComponent, StringTextComponent}
import net.minecraft.util.{Direction, INameable}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fml.{LogicalSide, LogicalSidedProvider}

import scala.collection.mutable.ArrayBuffer
//import net.minecraftforge.fml.common.Optional

//@Optional.Interface(modid = TileTankNoDisplay.bcId, iface = "buildcraft.api.transport.pipe.ICustomPipeConnection")
//@Optional.Interface(modid = TileTankNoDisplay.bcId, iface = "buildcraft.api.tiles.IDebuggable")
class TileTankNoDisplay(var tier: Tiers, t: TileEntityType[_ <: TileTankNoDisplay])
  extends TileEntity(t)
    with INameable
    /*with ICustomPipeConnection
    with IDebuggable*/ {
  self =>

  def this() = {
    this(Tiers.Invalid, ModObjects.TANK_NO_DISPLAY_TYPE)
  }

  def this(t: Tiers) = {
    this(t, ModObjects.TANK_NO_DISPLAY_TYPE)
  }

  val internalTank: TankHandler with TileTankNoDisplay.RealTank = new InternalTank(tier.amount)
  private final var mConnection: Connection = Connection.invalid
  final val connectionAttaches: ArrayBuffer[Connection => Unit] = ArrayBuffer.empty
  var loading = false
  var stackName: ITextComponent = _

  def connection: Connection = mConnection

  def connection_=(c: Connection): Unit = {
    mConnection = c
    connectionAttaches.foreach(_.apply(c))
  }

  override def write(compound: CompoundNBT): CompoundNBT = {
    compound.put(TileTankNoDisplay.NBT_Tank, internalTank.writeToNBT(new CompoundNBT))
    compound.put(TileTankNoDisplay.NBT_Tier, tier.toNBTTag)
    getStackName.foreach(t => compound.putString(TileTankNoDisplay.NBT_StackName, ITextComponent.Serializer.toJson(t)))
    super.write(compound)
  }

  def getBlockTag: CompoundNBT = {
    val nbt = write(new CompoundNBT)
    Seq("x", "y", "z", "id").foreach(nbt.remove)
    nbt
  }

  override def getUpdateTag: CompoundNBT = write(new CompoundNBT)

  override def getUpdatePacket = new SUpdateTileEntityPacket(getPos, 0, getUpdateTag)

  override def read(state: BlockState, compound: CompoundNBT): Unit = {
    super.read(state, compound)
    internalTank.readFromNBT(compound.getCompound(TileTankNoDisplay.NBT_Tank))
    tier = Tiers.fromNBT(compound.get(TileTankNoDisplay.NBT_Tier))
    if (compound.contains(TileTankNoDisplay.NBT_StackName)) {
      stackName = ITextComponent.Serializer.getComponentFromJson(compound.getString(TileTankNoDisplay.NBT_StackName))
    }
    loading = true
  }

  def readNBTClient(compound: CompoundNBT): Unit = {
    internalTank.readFromNBT(compound.getCompound(TileTankNoDisplay.NBT_Tank))
    tier = Tiers.fromNBT(compound.get(TileTankNoDisplay.NBT_Tier))
    if (compound.contains(TileTankNoDisplay.NBT_StackName)) {
      stackName = ITextComponent.Serializer.getComponentFromJson(compound.getString(TileTankNoDisplay.NBT_StackName))
    } else {
      stackName = null
    }
  }

  override def onDataPacket(net: NetworkManager, pkt: SUpdateTileEntityPacket): Unit = () //handleUpdateTag(pkt.getNbtCompound) // No way to get state

  override def onLoad(): Unit = {
    super.onLoad()
    if (loading) {
      loading = false
      if (SideProxy.isServer(this)) {
        val executor: MinecraftServer = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)
        executor.enqueue(new TickDelayedTask(executor.getTickCounter, () => {
          getWorld.getProfiler.startSection("Connection Loading")
          if (this.connection.isDummy)
            Connection.load(getWorld, getPos)
          getWorld.getProfiler.endSection()
        }))
      }
    }
  }

  override def getCapability[T](capability: Capability[T], facing: Direction): LazyOptional[T] = {
    val c = connection.getCapability(capability, facing)
    if (c.isPresent) c else super.getCapability(capability, facing)
  }

  private def sendPacket(): Unit = {
    if (SideProxy.isServer(this)) PacketHandler.sendToClient(TileMessage(this), getWorld)
  }

  def hasContent: Boolean = internalTank.getFluid.nonEmpty

  def getComparatorLevel: Int = connection.getComparatorLevel

  def onBlockPlacedBy(): Unit = {
    val downTank = Option(getWorld.getTileEntity(getPos.down())).collect { case t: TileTankNoDisplay => t }
    val upTank = Option(getWorld.getTileEntity(getPos.up())).collect { case t: TileTankNoDisplay => t }
    val newSeq = (downTank, upTank) match {
      case (Some(dT), Some(uT)) => dT.connection.seq :+ this :++ uT.connection.seq
      case (None, Some(uT)) => this +: uT.connection.seq
      case (Some(dT), None) => dT.connection.seq :+ this
      case (None, None) => Seq(this)
    }
    Connection.createAndInit(newSeq)
  }

  def onDestroy(): Unit = {
    this.connection.remove(this)
  }

  def getStackName: Option[ITextComponent] = Option(stackName)

  override def getName: ITextComponent = getStackName
    .getOrElse(new StringTextComponent(tier.toString + " Tank"))

  override def hasCustomName: Boolean = stackName != null

  override def getCustomName: ITextComponent = getStackName.orNull

  class InternalTank(initialCapacity: Long) extends TankHandler with TileTankNoDisplay.RealTank {
    initCapacity(initialCapacity)

    override def tile: TileTankNoDisplay = self

    override def toString: String = {
      val fluid = getFluid
      if (fluid == null) "Tank : no fluid : Capacity = " + capacity
      else "Tank : " + fluid.getLocalizedName + " " + getFluidAmount + "mB : Capacity = " + capacity
    }
  }

  /*@Optional.Method(modid = TileTankNoDisplay.bcId)
  override def getExtension(world: World, pos: BlockPos, face: Direction, state: IBlockState): Float =
    if (face.getAxis == Axis.Y) 0 else 2 / 16f

  @Optional.Method(modid = TileTankNoDisplay.bcId)
  override def getDebugInfo(left: util.List[String], right: util.List[String], side: Direction): Unit = {
    if (SideProxy.isServer(this)) {
      left add getClass.getName
      left add connection.toString
    }
    left.add("Tier : " + tier)
    left add tank.toString
  }*/
}

object TileTankNoDisplay {
  final val NBT_Tank = "tank"
  final val NBT_Tier = "tier"
  final val NBT_Capacity = "capacity"
  final val NBT_BlockTag = "BlockEntityTag"
  final val NBT_StackName = "stackName"
  final val bcId = "buildcraftcore"
  final val ae2id = "appliedenergistics2"

  /*import net.minecraftforge.event.world.ChunkEvent

  def makeConnectionOnChunkLoad(event: ChunkEvent.Load): Unit = {
    val chunk = event.getChunk
    if (event.getWorld != null && !event.getWorld.isRemote && !chunk.getTileEntitiesPos.isEmpty) {
      import scala.jdk.CollectionConverters
      val poses = JavaConverters.asScalaSet(chunk.getTileEntitiesPos)
      val tanks = poses.map(chunk.getTileEntity).collect { case tank: TileTankNoDisplay => tank }
      // Getting tiles via world IS NOT AVAILABLE.
      tanks.foreach { t => Connection.load(chunk, t.getPos) }
      // Loading finished. Don't turn off the flag with above call.
      tanks.foreach(_.loading = false)
    }
  }*/

  trait RealTank {
    self: TankHandler =>
    var box: Box = _

    def tile: TileTankNoDisplay

    lazy val lowerBound: Double = Config.content.renderLowerBound.get().doubleValue()
    lazy val upperBound: Double = Config.content.renderUpperBound.get().doubleValue()

    // Util methods
    def getFluidAmount: Long = this.getTank.amount

    def getFluid: FluidAmount = this.getTank.fluidAmount

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

    def readFromNBT(nbt: CompoundNBT): TankHandler with TileTankNoDisplay.RealTank = {
      val newTank: Tank = Tank(FluidAmount.fromNBT(nbt), nbt.getLong(TileTankNoDisplay.NBT_Capacity))
      setTank(newTank)
      self
    }

    def writeToNBT(nbt: CompoundNBT): CompoundNBT = {
      import scala.util.chaining._
      nbt
        .tap(getFluid.write)
        .tap(_.putLong(TileTankNoDisplay.NBT_Capacity, self.capacity))
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
    val ratio = MathHelper.clamp(amount / capacity, minRatio, 1)
    val height = (upperBound - lowerBound) * ratio
    if (isGaseous) {
      (upperBound - height, upperBound)
    } else {
      (lowerBound, lowerBound + height)
    }
  }
}
