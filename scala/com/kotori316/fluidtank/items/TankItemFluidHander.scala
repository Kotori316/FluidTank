package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.tiles.TileTank
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, FluidTankProperties, IFluidHandlerItem, IFluidTankProperties}

class TankItemFluidHander(item: ItemBlockTank, stack: ItemStack) extends IFluidHandlerItem with ICapabilityProvider {
    val tiers = item.blockTank.getTierByMeta(stack.getItemDamage)

    def nbt = stack.getSubCompound(TileTank.NBT_BlockTag)

    def tankNbt = if (nbt == null) null else nbt.getCompoundTag(TileTank.NBT_Tank)

    var inited = false
    var fluid: FluidStack = _

    override def getContainer: ItemStack = stack

    override def getTankProperties: Array[IFluidTankProperties] = {
        val fluid = FluidStack.loadFluidStackFromNBT(tankNbt)
        Array(new FluidTankProperties(fluid, tiers.amount))
    }

    override def fill(resource: FluidStack, doFill: Boolean): Int = {
        if (resource == null || resource.amount <= 0) {
            return 0
        }
        init()
        if (fluid == null) {
            val amount = math.min(resource.amount, tiers.amount)
            if (doFill) {
                fluid = resource.copy()
                fluid.amount = amount
                updateTag()
            }
            amount
        } else if (fluid.isFluidEqual(resource)) {
            val move = math.min(resource.amount, tiers.amount - fluid.amount)
            if (doFill) {
                fluid.amount += move
                updateTag()
            }
            move
        } else {
            0
        }
    }

    override def drain(resource: FluidStack, doDrain: Boolean): FluidStack = {
        init()
        if (fluid == null || resource == null || resource.amount <= 0) {
            return null
        }
        if (fluid.isFluidEqual(resource)) {
            val move = math.min(resource.amount, fluid.amount)
            if (doDrain) {
                fluid.amount -= move
                updateTag()
            }
            new FluidStack(resource, move)
        } else
            null
    }

    override def drain(maxDrain: Int, doDrain: Boolean): FluidStack = {
        init()
        if (fluid == null || maxDrain <= 0) {
            return null
        }
        val copy = fluid.copy()
        val move = math.min(maxDrain, fluid.amount)
        if (doDrain) {
            fluid.amount -= move
            updateTag()
        }
        new FluidStack(copy, move)
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

    def init(): Unit = {
        if (!inited) {
            fluid = FluidStack.loadFluidStackFromNBT(tankNbt)
        }
    }

    def updateTag(): Unit = {
        if (fluid.amount <= 0) {
            fluid = null
        }
        if (fluid != null) {
            val compound = Option(stack.getTagCompound).getOrElse(new NBTTagCompound)
            compound.setTag(TileTank.NBT_BlockTag, createTag)
            stack.setTagCompound(compound)
        } else {
            stack.removeSubCompound(TileTank.NBT_BlockTag)
            if (stack.hasTagCompound && stack.getTagCompound.hasNoTags) {
                stack.setTagCompound(null)
            }
        }
    }

    def createTag = {
        val tag = new NBTTagCompound
        tag.setTag(TileTank.NBT_Tier, tiers.toNBTTag)
        val tanktag = new NBTTagCompound
        tanktag.setInteger(TileTank.NBT_Capacity, tiers.amount)
        fluid.writeToNBT(tanktag)
        tag.setTag(TileTank.NBT_Tank, tanktag)
        tag
    }
}
