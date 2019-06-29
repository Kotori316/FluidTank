package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.Utils
import com.kotori316.fluidtank.tiles.TileTankNoDisplay
import javax.annotation.Nullable
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, FluidTankProperties, IFluidHandlerItem, IFluidTankProperties}

class TankItemFluidHandler(item: ItemBlockTank, stack: ItemStack) extends IFluidHandlerItem with ICapabilityProvider {
  val tiers = item.blockTank.tier

  def nbt: CompoundNBT = stack.getChildTag(TileTankNoDisplay.NBT_BlockTag)

  @Nullable
  def tankNbt: CompoundNBT = if (nbt == null) null else nbt.getCompound(TileTankNoDisplay.NBT_Tank)

  var initialized = false
  var fluid: FluidStack = _

  override def getContainer: ItemStack = stack

  override def getTankProperties: Array[IFluidTankProperties] = {
    val fluid = FluidStack.loadFluidStackFromNBT(tankNbt)
    Array(new FluidTankProperties(fluid, Utils.toInt(tiers.amount)))
  }

  override def fill(resource: FluidStack, doFill: Boolean): Int = {
    if (resource == null || resource.amount <= 0 || stack.getCount > 1) {
      return 0
    }
    init()
    if (fluid == null) {
      val amount = math.min(resource.amount, Utils.toInt(tiers.amount))
      if (doFill) {
        fluid = resource.copy()
        fluid.amount = amount
        updateTag()
      }
      amount
    } else if (fluid.isFluidEqual(resource)) {
      val move = math.min(resource.amount, Utils.toInt(tiers.amount) - fluid.amount)
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
    if (fluid == null || resource == null || resource.amount <= 0 || stack.getCount > 1) {
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

  override def getCapability[T](capability: Capability[T], facing: Direction): LazyOptional[T] = {
    CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() => this))
  }

  def init(): Unit = {
    if (!initialized) {
      initialized = true
      fluid = FluidStack.loadFluidStackFromNBT(tankNbt)
    }
  }

  def updateTag(): Unit = {
    if (stack.getCount > 1) {
      return
    }
    if (fluid.amount <= 0) {
      fluid = null
    }
    if (fluid != null) {
      val compound = stack.getOrCreateTag()
      compound.put(TileTankNoDisplay.NBT_BlockTag, createTag)
      stack.setTag(compound)
    } else {
      stack.removeChildTag(TileTankNoDisplay.NBT_BlockTag)
    }
  }

  def createTag: CompoundNBT = {
    val tag = new CompoundNBT
    tag.put(TileTankNoDisplay.NBT_Tier, tiers.toNBTTag)
    val tankTag = new CompoundNBT
    tankTag.putInt(TileTankNoDisplay.NBT_Capacity, Utils.toInt(tiers.amount))
    fluid.writeToNBT(tankTag)
    tag.put(TileTankNoDisplay.NBT_Tank, tankTag)
    tag
  }
}
