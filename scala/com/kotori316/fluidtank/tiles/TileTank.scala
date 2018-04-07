package com.kotori316.fluidtank.tiles

import java.util

import buildcraft.api.tiles.IDebuggable
import buildcraft.api.transport.pipe.ICustomPipeConnection
import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.packet.{PacketHandler, SideProxy, TileMessage}
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.Axis
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fml.common.Optional

@Optional.Interface(modid = "BuildCraftAPI|transport", iface = "buildcraft.api.transport.pipe.ICustomPipeConnection")
@Optional.Interface(modid = "BuildCraftAPI|tiles", iface = "buildcraft.api.tiles.IDebuggable")
class TileTank(var tier: Tiers) extends TileEntity with ICustomPipeConnection with IDebuggable {
    self =>

    def this() {
        this(Tiers.Invalid)
    }

    val tank = new Tank
    private var connection = Connection.invalid
    var f: FluidStack = _

    override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
        compound.setTag("tank", tank.writeToNBT(new NBTTagCompound))
        compound.setTag("tier", tier.toNBTTag)
        Option(connection.getFluidStack).foreach(s => compound.setTag("f", s.writeToNBT(new NBTTagCompound)))
        super.writeToNBT(compound)
    }

    def getBlockTag: NBTTagCompound = {
        val nbt = writeToNBT(new NBTTagCompound)
        Seq("x", "y", "z", "f").foreach(nbt.removeTag)
        nbt
    }

    override def getUpdateTag: NBTTagCompound = writeToNBT(new NBTTagCompound)

    override def getUpdatePacket = new SPacketUpdateTileEntity(getPos, 0, getUpdateTag)

    override def readFromNBT(compound: NBTTagCompound): Unit = {
        super.readFromNBT(compound)
        tank.readFromNBT(compound.getCompoundTag("tank"))
        tier = Tiers.fromNBT(compound.getCompoundTag("tier"))
        f = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("f"))
    }

    override def onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity): Unit = handleUpdateTag(pkt.getNbtCompound)

    override def onLoad(): Unit = {
        super.onLoad()
        updateConnection()
    }

    override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
        val c = connection.getCapability(capability, facing)
        if (c != null) c else super.getCapability(capability, facing)
    }

    override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean =
        connection.hasCapability(capability, facing) || super.hasCapability(capability, facing)

    private def sendPacket(): Unit = {
        if (SideProxy.isServer(this)) PacketHandler.WRAPPER.sendToDimension(TileMessage(this), getWorld.provider.getDimension)
    }

    def hasContent: Boolean = tank.getFluidAmount > 0

    override def hasFastRenderer = true

    def neighborChanged(): Unit = {
        updateConnection()
    }

    def getComparatorLevel: Int = connection.getComparatorLevel

    private def updateConnection(): Unit = {
        if (SideProxy.isServer(this)) {
            var connectionFluid = Option(connection.getFluidStack).orElse(Option(this.tank.getFluid)).getOrElse(f)
            val function: BlockPos => Boolean = { p =>
                getWorld.getTileEntity(p) match {
                    case that: TileTank =>
                        if (connectionFluid == null) {
                            connectionFluid = that.tank.getFluid
                            true
                        } else {
                            (that.tank.getFluid == null || connectionFluid.isFluidEqual(that.tank.getFluid)) &&
                              (that.connection.getFluidStack == null || connectionFluid.isFluidEqual(that.connection.getFluidStack)) &&
                              (that.f == null || connectionFluid.isFluidEqual(that.f))
                        }
                    case _ => false
                }
            }
            val lowest = Iterator.iterate(getPos)(_.down()).takeWhile(function).toList.lastOption.getOrElse({
                FluidTank.LOGGER.fatal("No lowest tank", new IllegalArgumentException("No lowest tank"))
                getPos
            })
            val tanks = Iterator.iterate(lowest)(_.up())
              .takeWhile(function).map(getWorld.getTileEntity(_).asInstanceOf[TileTank]).toIndexedSeq
            Connection.setConnection(tanks, c => tile => tile.connection = c)
        }
    }

    class Tank extends net.minecraftforge.fluids.FluidTank(tier.amount) {
        setTileEntity(self)
        var box: Box = _

        override def onContentsChanged(): Unit = {
            super.onContentsChanged()
            sendPacket()
            connection.updateComparator()
            if (!SideProxy.isServer(self) && getCapacity != 0) {
                val percent = getFluidAmount.toDouble / getCapacity.toDouble
                val a = 0.001
                if (percent > a) {
                    val d = 1d / 16d
                    var maxY = 0d
                    var minY = 0d
                    if (tank.getFluid.getFluid.isGaseous(tank.getFluid)) {
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

        override def readFromNBT(nbt: NBTTagCompound) = {
            setCapacity(nbt.getInteger("capacity"))
            val fluid = FluidStack.loadFluidStackFromNBT(nbt)
            setFluid(fluid)
            onContentsChanged()
            this
        }

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            super.writeToNBT(nbt)
            nbt.setInteger("capacity", getCapacity)
            nbt
        }

        override def toString: String = {
            val fluid = getFluid
            if (fluid == null) "Tank : no fluid : Capacity = " + getCapacity
            else "Tank : " + fluid.getLocalizedName + " " + getFluidAmount + "mB : Capacity = " + getCapacity
        }

        override def canFillFluidType(fluid: FluidStack): Boolean = {
            val fluidType = connection.fluidType
            fluidType == null || fluidType == fluid.getFluid
        }
    }

    @Optional.Method(modid = "BuildCraftAPI|transport")
    override def getExtension(world: World, pos: BlockPos, face: EnumFacing, state: IBlockState): Float =
        if (face.getAxis == Axis.Y) 0 else 2 / 16f

    @Optional.Method(modid = "BuildCraftAPI|tiles")
    override def getDebugInfo(left: util.List[String], right: util.List[String], side: EnumFacing): Unit = {
        if (SideProxy.isServer(this)) {
            left add getClass.getName
            left add connection.toString
        }
        left.add("Tier : " + tier)
        left add tank.toString
    }
}
