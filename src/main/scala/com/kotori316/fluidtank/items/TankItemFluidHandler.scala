package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.tiles.TileTankNoDisplay
import com.kotori316.fluidtank.{FluidAmount, Utils}
import javax.annotation.{Nonnull, Nullable}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler, IFluidHandlerItem}

class TankItemFluidHandler(item: ItemBlockTank, stack: ItemStack) extends IFluidHandlerItem with ICapabilityProvider {
  val tiers = item.blockTank.tier

  def nbt: CompoundNBT = stack.getChildTag(TileTankNoDisplay.NBT_BlockTag)

  @Nullable
  def tankNbt: CompoundNBT = if (nbt == null) null else nbt.getCompound(TileTankNoDisplay.NBT_Tank)

  var initialized = false
  @Nonnull
  var fluid: FluidStack = FluidStack.EMPTY

  override def getContainer: ItemStack = stack

  override def fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int = {
    if (resource.isEmpty || stack.getCount > 1) {
      return 0
    }
    init()
    if (fluid.isEmpty) {
      val amount = math.min(resource.getAmount, Utils.toInt(tiers.amount))
      if (action.execute()) {
        fluid = resource.copy()
        fluid.setAmount(amount)
        updateTag()
      }
      amount
    } else if (fluid.isFluidEqual(resource)) {
      val move = math.min(resource.getAmount, Utils.toInt(tiers.amount) - fluid.getAmount)
      if (action.execute()) {
        fluid.setAmount(fluid.getAmount + move)
        updateTag()
      }
      move
    } else {
      0
    }
  }

  override def drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack = {
    init()
    if (fluid.isEmpty || resource.isEmpty || stack.getCount > 1) {
      return FluidStack.EMPTY
    }
    if (fluid.isFluidEqual(resource)) {
      val move = math.min(resource.getAmount, fluid.getAmount)
      if (action.execute()) {
        fluid.setAmount(fluid.getAmount - move)
        updateTag()
      }
      new FluidStack(resource, move)
    } else
      null
  }

  override def drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack = {
    init()
    if (fluid.isEmpty || maxDrain <= 0) {
      return FluidStack.EMPTY
    }
    val copy = fluid.copy()
    val move = math.min(maxDrain, fluid.getAmount)
    if (action.execute()) {
      fluid.setAmount(fluid.getAmount - move)
      updateTag()
    }
    new FluidStack(copy, move)
  }

  override def getCapability[T](capability: Capability[T], facing: Direction): LazyOptional[T] = {
    CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.orEmpty(capability, LazyOptional.of(() => this))
  }

  def init(): Unit = {
    if (!initialized) {
      initialized = true
      fluid = FluidAmount.fromNBT(tankNbt).toStack
    }
  }

  def updateTag(): Unit = {
    if (stack.getCount > 1) {
      return
    }
    if (!fluid.isEmpty) {
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
    FluidAmount.fromStack(fluid).write(tankTag)
    tag.put(TileTankNoDisplay.NBT_Tank, tankTag)
    tag
  }

  override def getTanks = 1

  override def getFluidInTank(tank: Int) = fluid

  override def getTankCapacity(tank: Int) = Utils.toInt(tiers.amount)

  override def isFluidValid(tank: Int, stack: FluidStack) = true
}
