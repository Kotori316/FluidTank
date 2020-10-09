package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.fluids.{FluidAmount, Tank, TankHandler}
import com.kotori316.fluidtank.network.{PacketHandler, SideProxy, TileMessage}
import com.kotori316.fluidtank.render.Box
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SUpdateTileEntityPacket
import net.minecraft.tileentity.{ITickableTileEntity, TileEntity, TileEntityType}
import net.minecraft.util.text.{ITextComponent, StringTextComponent}
import net.minecraft.util.{Direction, INameable}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional

import scala.collection.mutable.ArrayBuffer
//import net.minecraftforge.fml.common.Optional

//@Optional.Interface(modid = TileTankNoDisplay.bcId, iface = "buildcraft.api.transport.pipe.ICustomPipeConnection")
//@Optional.Interface(modid = TileTankNoDisplay.bcId, iface = "buildcraft.api.tiles.IDebuggable")
class TileTankNoDisplay(var tier: Tiers, t: TileEntityType[_ <: TileTankNoDisplay])
  extends TileEntity(t)
    with INameable
    with ITickableTileEntity
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
      stackName = ITextComponent.Serializer.func_240643_a_(compound.getString(TileTankNoDisplay.NBT_StackName))
    }
    loading = true
  }

  def readNBTClient(compound: CompoundNBT): Unit = {
    internalTank.readFromNBT(compound.getCompound(TileTankNoDisplay.NBT_Tank))
    tier = Tiers.fromNBT(compound.get(TileTankNoDisplay.NBT_Tier))
    if (compound.contains(TileTankNoDisplay.NBT_StackName)) {
      stackName = ITextComponent.Serializer.func_240643_a_(compound.getString(TileTankNoDisplay.NBT_StackName))
    } else {
      stackName = null
    }
  }

  override def onDataPacket(net: NetworkManager, pkt: SUpdateTileEntityPacket): Unit = () //handleUpdateTag(pkt.getNbtCompound) // No way to get state

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
  override def tick(): Unit = {
    if (loading) {
      loading = false
      if (SideProxy.isServer(this)) {
        getWorld.getProfiler.startSection("Connection Loading")
        if (this.connection == Connection.invalid)
          Connection.load(getWorld, getPos)
        getWorld.getProfiler.endSection()
      }
    }
  }
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
        if (percent > a) {
          val d = 1d / 16d
          var maxY = 0d
          var minY = 0d
          if (this.getFluid.isGaseous) {
            maxY = 1d - a
            minY = 1d - percent + a
          } else {
            minY = a
            maxY = percent - a
          }
          box = Box(d * 8, minY, d * 8, d * 8, maxY, d * 8, d * 12 - 0.01, percent, d * 12 - 0.01, firstSide = true, endSide = true)
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

}
