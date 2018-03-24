package com.kotori316.fluidtank.tiles

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.FluidTank
import net.minecraftforge.fluids.capability.CapabilityFluidHandler

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

    override def readFromNBT(compound: NBTTagCompound): Unit = {
        super.readFromNBT(compound)
        tank.readFromNBT(compound.getCompoundTag("tank"))
        tier = Tiers.fromNBT(compound.getCompoundTag("tier"))
    }

    override def getCapability[T](capability: Capability[T], facing: EnumFacing) = {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank)
        else super.getCapability(capability, facing)
    }

    override def hasCapability(capability: Capability[_], facing: EnumFacing) =
        capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing)

    def shouldSaveToNBT: Boolean = tank.getFluidAmount > 0

    class Tank extends net.minecraftforge.fluids.FluidTank(tier.amount * 1000) {
        setTileEntity(self)

        override def readFromNBT(nbt: NBTTagCompound): FluidTank = {
            setCapacity(nbt.getInteger("capacity"))
            super.readFromNBT(nbt)
        }

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            nbt.setInteger("capacity", getCapacity)
            super.writeToNBT(nbt)
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
