package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.packet.{PacketHandler, SideProxy, TileMessage}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.{FluidStack, FluidTank}

/**
  * TODO implement [[buildcraft.api.transport.pipe.ICustomPipeConnection]]
  */

class TileTank(var tier: Tiers) extends TileEntity {
    self =>

    def this() {
        this(Tiers.Invalid)
    }

    val tank = new Tank
    private var connection = new Connection(this, Seq(this))

    override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
        compound.setTag("tank", tank.writeToNBT(new NBTTagCompound))
        compound.setTag("tier", tier.toNBTTag)
        super.writeToNBT(compound)
    }

    def getBlockTag: NBTTagCompound = {
        val nbt = writeToNBT(new NBTTagCompound)
        Seq("x", "y", "z").foreach(nbt.removeTag)
        nbt
    }

    override def getUpdateTag: NBTTagCompound = writeToNBT(new NBTTagCompound)

    override def getUpdatePacket = new SPacketUpdateTileEntity(getPos, 0, getUpdateTag)

    override def readFromNBT(compound: NBTTagCompound): Unit = {
        super.readFromNBT(compound)
        tank.readFromNBT(compound.getCompoundTag("tank"))
        tier = Tiers.fromNBT(compound.getCompoundTag("tier"))
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

    override def hasCapability(capability: Capability[_], facing: EnumFacing) =
        connection.hasCapability(capability, facing) || super.hasCapability(capability, facing)

    private def sendPacket() = {
        if (SideProxy.isServer(this)) PacketHandler.WRAPPER.sendToDimension(TileMessage(this), getWorld.provider.getDimension)
    }

    def hasContent: Boolean = tank.getFluidAmount > 0

    override def hasFastRenderer = true

    def neighborChanged(): Unit = {
        updateConnection()
    }

    def updateConnection(): Unit = {
        if (SideProxy.isServer(this)) {
            val function: BlockPos => Boolean = getWorld.getTileEntity(_).isInstanceOf[TileTank]
            val lowest = Iterator.iterate(getPos)(_.down()).takeWhile(function).toList.last
            val tanks = Iterator.iterate(lowest)(_.up())
              .takeWhile(function).map(getWorld.getTileEntity(_).asInstanceOf[TileTank]).toList
            val newConnection = Connection(tanks.headOption, tanks)
            tanks.foreach(_.connection = newConnection)
        }
    }

    class Tank extends net.minecraftforge.fluids.FluidTank(tier.amount) {
        setTileEntity(self)

        override def onContentsChanged(): Unit = {
            super.onContentsChanged()
            sendPacket()
        }

        override def readFromNBT(nbt: NBTTagCompound): FluidTank = {
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

}
