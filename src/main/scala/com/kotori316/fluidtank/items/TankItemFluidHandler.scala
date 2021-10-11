package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.Utils
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.tiles.{Tier, TileTank}
import javax.annotation.{Nonnull, Nullable}
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler, IFluidHandlerItem}

class TankItemFluidHandler(val tiers: Tier, stack: ItemStack) extends IFluidHandlerItem with ICapabilityProvider {

  def nbt: CompoundTag = stack.getTagElement(TileTank.NBT_BlockTag)

  @Nullable
  def tankNbt: CompoundTag = if (nbt == null) null else nbt.getCompound(TileTank.NBT_Tank)

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

  private def init(): Unit = {
    if (!initialized) {
      initialized = true
      fluid = FluidAmount.fromNBT(tankNbt).toStack
    }
  }

  private def updateTag(): Unit = {
    if (stack.getCount > 1) {
      return
    }
    if (!fluid.isEmpty) {
      val compound = stack.getOrCreateTag()
      compound.put(TileTank.NBT_BlockTag, createTag)
      stack.setTag(compound)
    } else {
      stack.removeTagKey(TileTank.NBT_BlockTag)
    }
  }

  def createTag: CompoundTag = {
    val tag = new CompoundTag
    tag.put(TileTank.NBT_Tier, tiers.toNBTTag)
    val tankTag = new CompoundTag
    tankTag.putInt(TileTank.NBT_Capacity, Utils.toInt(getCapacity))
    getFluid.write(tankTag)
    tag.put(TileTank.NBT_Tank, tankTag)
    tag
  }

  override def getTanks = 1

  /**
   * Get the content of tank. If the stack has 2 or more items, returns empty.
   *
   * @return The content if stack size = 1, else empty fluid.
   */
  @Nonnull
  override def getFluidInTank(tank: Int): FluidStack = {
    init()
    if (stack.getCount == 1) fluid else FluidStack.EMPTY
  }

  override def getTankCapacity(tank: Int): Int = if (stack.getCount == 1) Utils.toInt(getCapacity) else 0

  override def isFluidValid(tank: Int, stack: FluidStack) = true

  /**
   * Get the fluid, ignoring the stack size.
   *
   * @return the fluid in the tank.
   */
  def getFluid: FluidAmount = {
    init()
    FluidAmount.fromStack(fluid)
  }

  def getCapacity: Long = if (tankNbt == null) tiers.amount else tankNbt.getLong(TileTank.NBT_Capacity)
}
