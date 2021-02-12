package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.Utils
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.tiles.{Tiers, TileTankNoDisplay}
import javax.annotation.{Nonnull, Nullable}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler, IFluidHandlerItem}

class TankItemFluidHandler(item: ItemBlockTank, stack: ItemStack) extends IFluidHandlerItem with ICapabilityProvider {
  val tiers: Tiers = item.blockTank.tier

  def nbt: CompoundNBT = stack.getChildTag(TileTankNoDisplay.NBT_BlockTag)

  @Nullable
  def tankNbt: CompoundNBT = if (nbt == null) null else nbt.getCompound(TileTankNoDisplay.NBT_Tank)

  private[this] var initialized = false
  @Nonnull
  private[this] var fluid: FluidStack = FluidStack.EMPTY

  override def getContainer: ItemStack = stack

  override def fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int = {
    if (resource.isEmpty || stack.getCount > 1) {
      return 0
    }
    init()
    if (fluid.isEmpty) {
      val amount = math.min(resource.getAmount, Utils.toInt(getCapacity))
      if (action.execute()) {
        fluid = resource.copy()
        fluid.setAmount(amount)
        updateTag()
      }
      amount
    } else if (fluid.isFluidEqual(resource)) {
      val move = math.min(resource.getAmount, Utils.toInt(getCapacity) - fluid.getAmount)
      if (action.execute()) {
        fluid.setAmount(fluid.getAmount + move)
        updateTag()
      }
      move
    } else {
      0
    }
  }

  @Nonnull
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
      FluidStack.EMPTY
  }

  @Nonnull
  override def drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack = {
    init()
    if (fluid.isEmpty || maxDrain <= 0 || stack.getCount > 1) {
      return FluidStack.EMPTY
    }
    val copy = fluid.copy()
    copy.setAmount(maxDrain)
    drain(copy, action)
  }

  override def getCapability[T](capability: Capability[T], facing: Direction): LazyOptional[T] = {
    if (capability == null) return LazyOptional.of(() => this).cast() // Cap is null when testing with JUnit and Data gen.
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
    tankTag.putInt(TileTankNoDisplay.NBT_Capacity, Utils.toInt(getCapacity))
    getFluid.write(tankTag)
    tag.put(TileTankNoDisplay.NBT_Tank, tankTag)
    tag
  }

  override def getTanks = 1

  @Nonnull
  override def getFluidInTank(tank: Int): FluidStack = {
    init()
    if (stack.getCount == 1) fluid else FluidStack.EMPTY
  }

  override def getTankCapacity(tank: Int): Int = if (stack.getCount == 1) Utils.toInt(getCapacity) else 0

  override def isFluidValid(tank: Int, stack: FluidStack) = true

  def getFluid: FluidAmount = FluidAmount.fromStack(fluid)

  def getCapacity: Long = if (tankNbt == null) tiers.amount else tankNbt.getLong(TileTankNoDisplay.NBT_Capacity)
}
