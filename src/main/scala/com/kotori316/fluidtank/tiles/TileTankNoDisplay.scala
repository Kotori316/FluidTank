package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.network.{PacketHandler, SideProxy, TileMessage}
import com.kotori316.fluidtank.render.Box
import com.kotori316.fluidtank.{FluidAmount, ModObjects, Utils}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.{TileEntity, TileEntityType}
import net.minecraft.util.text.{ITextComponent, TextComponentString}
import net.minecraft.util.{EnumFacing, INameable}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
//import net.minecraftforge.fml.common.Optional

//@Optional.Interface(modid = TileTankNoDisplay.bcId, iface = "buildcraft.api.transport.pipe.ICustomPipeConnection")
//@Optional.Interface(modid = TileTankNoDisplay.bcId, iface = "buildcraft.api.tiles.IDebuggable")
class TileTankNoDisplay(var tier: Tiers, t: TileEntityType[_ <: TileTankNoDisplay])
  extends TileEntity(t)
    with INameable
    /*with ICustomPipeConnection
    with IDebuggable*/ {
  self =>

  def this() {
    this(Tiers.Invalid, ModObjects.TANK_NO_DISPLAY_TYPE)
  }

  def this(t: Tiers) {
    this(t, ModObjects.TANK_NO_DISPLAY_TYPE)
  }

  val tank = new Tank
  var connection = Connection.invalid
  var loading = false
  var stackName: ITextComponent = _

  override def write(compound: NBTTagCompound): NBTTagCompound = {
    compound.setTag(TileTankNoDisplay.NBT_Tank, tank.writeToNBT(new NBTTagCompound))
    compound.setTag(TileTankNoDisplay.NBT_Tier, tier.toNBTTag)
    getStackName.foreach(t => compound.setString(TileTankNoDisplay.NBT_StackName, ITextComponent.Serializer.toJson(t)))
    super.write(compound)
  }

  def getBlockTag: NBTTagCompound = {
    val nbt = write(new NBTTagCompound)
    Seq("x", "y", "z", "id").foreach(nbt.removeTag)
    nbt
  }

  override def getUpdateTag: NBTTagCompound = write(new NBTTagCompound)

  override def getUpdatePacket = new SPacketUpdateTileEntity(getPos, 0, getUpdateTag)

  override def read(compound: NBTTagCompound): Unit = {
    super.read(compound)
    tank.readFromNBT(compound.getCompound(TileTankNoDisplay.NBT_Tank))
    tier = Tiers.fromNBT(compound.getCompound(TileTankNoDisplay.NBT_Tier))
    if (compound.hasKey(TileTankNoDisplay.NBT_StackName)) {
      stackName = ITextComponent.Serializer.fromJson(compound.getString(TileTankNoDisplay.NBT_StackName))
    }
    loading = true
  }

  def readNBTClient(compound: NBTTagCompound): Unit = {
    tank.readFromNBT(compound.getCompound(TileTankNoDisplay.NBT_Tank))
    tier = Tiers.fromNBT(compound.getCompound(TileTankNoDisplay.NBT_Tier))
    if (compound.hasKey(TileTankNoDisplay.NBT_StackName)) {
      stackName = ITextComponent.Serializer.fromJson(compound.getString(TileTankNoDisplay.NBT_StackName))
    } else {
      stackName = null
    }
  }

  override def onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity): Unit = handleUpdateTag(pkt.getNbtCompound)

  override def onLoad(): Unit = {
    super.onLoad()
    if (loading && SideProxy.isServer(this)) {
      Connection.load(getWorld, getPos)
      loading = false
    }
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): LazyOptional[T] = {
    val c = connection.getCapability(capability, facing)
    if (c.isPresent) c else super.getCapability(capability, facing)
  }

  private def sendPacket(): Unit = {
    if (SideProxy.isServer(this)) PacketHandler.sendToClient(TileMessage(this), getWorld)
  }

  def hasContent: Boolean = tank.getFluidAmount > 0

  override def hasFastRenderer = true

  def getComparatorLevel: Int = connection.getComparatorLevel

  def onBlockPlacedBy(): Unit = {
    val downTank = Option(getWorld.getTileEntity(getPos.down())).collect { case t: TileTankNoDisplay => t }
    val upTank = Option(getWorld.getTileEntity(getPos.up())).collect { case t: TileTankNoDisplay => t }
    (downTank, upTank) match {
      case (Some(dT), Some(uT)) => dT.connection.add(this, EnumFacing.UP).add(uT.connection, EnumFacing.UP)
      case (None, Some(uT)) => uT.connection.add(this, EnumFacing.UP.getOpposite)
      case (Some(dT), None) => dT.connection.add(this, EnumFacing.DOWN.getOpposite)
      case (None, None) => this.connection = new Connection(Seq(this))
    }
  }

  def onDestroy(): Unit = {
    this.connection.remove(this)
  }

  def getStackName: Option[ITextComponent] = Option(stackName)

  override def getName = getStackName
    .getOrElse(new TextComponentString(tier.toString + " Tank"))

  override def hasCustomName = stackName != null

  override def getCustomName = getStackName.orNull

  class Tank extends FluidAmount.Tank /*extends net.minecraftforge.fluids.FluidTank(Utils.toInt(tier.amount))*/ {
    var box: Box = _
    var fluid = FluidAmount.EMPTY
    var capacity = Utils.toInt(tier.amount)

    def onContentsChanged(): Unit = {
      sendPacket()
      connection.updateNeighbors()
      if (!SideProxy.isServer(self) && capacity != 0) {
        val percent = getFluidAmount.toDouble / capacity.toDouble
        val a = 0.001
        if (percent > a) {
          val d = 1d / 16d
          var maxY = 0d
          var minY = 0d
          if (tank.getFluid.isGaseous(tank.getFluid)) {
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

    def readFromNBT(nbt: NBTTagCompound) = {
      capacity = nbt.getInt(TileTankNoDisplay.NBT_Capacity)
      val fluid = FluidAmount.fromNBT(nbt)
      setFluid(fluid)
      onContentsChanged()
      this
    }

    def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
      fluid.write(nbt)
      nbt.setInt(TileTankNoDisplay.NBT_Capacity, capacity)
      nbt
    }

    override def toString: String = {
      val fluid = getFluid
      if (fluid == null) "Tank : no fluid : Capacity = " + capacity
      else "Tank : " + fluid.getLocalizedName + " " + getFluidAmount + "mB : Capacity = " + capacity
    }

    def canFillFluidType(fluid: FluidAmount): Boolean = {
      val fluidType = connection.getFluidStack
      fluidType.isEmpty || fluidType.exists(fluid.fluidEqual)
    }

    // Util methods
    def getFluidAmount = fluid.amount

    def getFluid = fluid

    def setFluid(fluidAmount: FluidAmount): Unit = {
      if (fluidAmount == null) fluid = FluidAmount.EMPTY
      else fluid = fluidAmount
    }

    // Change content
    /**
      * @return Fluid that was accepted by the tank.
      */
    override def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Int = 0): FluidAmount = {
      if (canFillFluidType(fluidAmount)) {
        val newAmount = fluid.amount + fluidAmount.amount
        if (capacity >= newAmount) {
          if (doFill) {
            fluid = fluidAmount.setAmount(newAmount)
            onContentsChanged()
          }
          fluidAmount
        } else {
          val accept = capacity - fluid.amount
          if (accept >= min) {
            if (doFill) {
              fluid = fluidAmount.setAmount(capacity)
              onContentsChanged()
            }
            fluidAmount.setAmount(accept)
          } else {
            FluidAmount.EMPTY
          }
        }
      } else {
        FluidAmount.EMPTY
      }
    }

    /**
      * @param fluidAmount the fluid representing the kind and maximum amount to drain.
      *                    Empty Fluid means fluid type can be anything.
      * @param doDrain     false means simulating.
      * @param min         minimum amount to drain.
      * @return the fluid and amount that is (or will be) drained.
      */
    override def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Int = 0): FluidAmount = {
      if (canFillFluidType(fluidAmount) || FluidAmount.EMPTY.fluidEqual(fluidAmount)) {
        val drain = math.min(fluid.amount, fluidAmount.amount)
        if (drain >= min) {
          val newAmount = fluid.amount - drain
          if (doDrain) {
            fluid = fluid.setAmount(newAmount)
            onContentsChanged()
          }
          fluid.setAmount(drain)
        } else {
          FluidAmount.EMPTY
        }
      } else {
        FluidAmount.EMPTY
      }
    }
  }

  /*@Optional.Method(modid = TileTankNoDisplay.bcId)
  override def getExtension(world: World, pos: BlockPos, face: EnumFacing, state: IBlockState): Float =
    if (face.getAxis == Axis.Y) 0 else 2 / 16f

  @Optional.Method(modid = TileTankNoDisplay.bcId)
  override def getDebugInfo(left: util.List[String], right: util.List[String], side: EnumFacing): Unit = {
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
}