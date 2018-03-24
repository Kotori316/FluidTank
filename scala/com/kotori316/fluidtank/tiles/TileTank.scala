package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.packet.{PacketHandler, TileMessage}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.{FluidStack, FluidTank}

class TileTank(var tier: Tiers) extends TileEntity {
    self =>

    def this() {
        this(Tiers.Invalid)
    }

    val tank = new Tank

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

    override def getUpdatePacket: SPacketUpdateTileEntity = new SPacketUpdateTileEntity(getPos, 0, getUpdateTag)

    override def readFromNBT(compound: NBTTagCompound): Unit = {
        super.readFromNBT(compound)
        tank.readFromNBT(compound.getCompoundTag("tank"))
        tier = Tiers.fromNBT(compound.getCompoundTag("tier"))
    }

    override def onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity): Unit = handleUpdateTag(pkt.getNbtCompound)

    override def getCapability[T](capability: Capability[T], facing: EnumFacing) = {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank)
        else super.getCapability(capability, facing)
    }

    override def hasCapability(capability: Capability[_], facing: EnumFacing) =
        capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing)

    private def sendPacket() = {
        if (hasWorld && !getWorld.isRemote) {
            PacketHandler.WRAPPER.sendToDimension(TileMessage(this), getWorld.provider.getDimension)
        }
    }

    def hasContent: Boolean = tank.getFluidAmount > 0

    override def hasFastRenderer: Boolean = true

    class Tank extends net.minecraftforge.fluids.FluidTank(tier.amount * 1000) {
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
            if (fluid == null) {
                "Tank : no fluid : Capacity = " + getCapacity
            } else {
                "Tank : " + fluid.getLocalizedName + " " + getFluidAmount + "mB : Capacity = " + getCapacity
            }
        }
    }

}
