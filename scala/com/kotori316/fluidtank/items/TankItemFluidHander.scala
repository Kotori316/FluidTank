package com.kotori316.fluidtank.items

import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, FluidTankProperties, IFluidHandlerItem, IFluidTankProperties}

class TankItemFluidHander(stack: ItemStack) extends IFluidHandlerItem with ICapabilityProvider {
    val tiers = stack.getItem.asInstanceOf[ItemBlockTank].blockTank.getTierByMeta(stack.getItemDamage)

    def nbt = stack.getSubCompound("BlockEntityTag")

    def tankNbt = if (nbt == null) null else nbt.getCompoundTag("tank")

    override def getContainer: ItemStack = stack

    override def getTankProperties: Array[IFluidTankProperties] = {
        val fluid = FluidStack.loadFluidStackFromNBT(nbt)
        if (fluid == null) EmptyFluidHandler.EMPTY_TANK_PROPERTIES_ARRAY
        else Array(new FluidTankProperties(fluid, tiers.amount))
    }

    override def fill(resource: FluidStack, doFill: Boolean): Int = {
        val fluid = FluidStack.loadFluidStackFromNBT(nbt)
        // TODO: filling
        0
    }

    override def drain(resource: FluidStack, doDrain: Boolean): FluidStack = {
        val fluid = FluidStack.loadFluidStackFromNBT(nbt)
        // TODO: draining
        null
    }

    override def drain(maxDrain: Int, doDrain: Boolean): FluidStack = {
        val fluid = FluidStack.loadFluidStackFromNBT(nbt)
        // TODO: draining
        null
    }

    override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
        capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
    }

    override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
            CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(this)
        } else {
            null.asInstanceOf[T]
        }
    }
}
