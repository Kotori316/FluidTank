package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.Utils
import com.kotori316.fluidtank.fluids.{FluidAction, FluidAmount}
import com.kotori316.fluidtank.tiles.{Tier, TileTank}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.{BlockItem, ItemStack}
import org.jetbrains.annotations.{NotNull, Nullable}

class TankItemFluidHandler(val tier: Tier, stack: ItemStack) {

  def nbt: CompoundTag = BlockItem.getBlockEntityData(stack)

  @Nullable
  def tankNbt: CompoundTag = if (nbt == null) null else nbt.getCompound(TileTank.NBT_Tank)

  private[this] var initialized = false
  @NotNull
  private[this] var fluid: FluidAmount = FluidAmount.EMPTY

  def getContainer: ItemStack = stack

  def fill(resource: FluidAmount, action: FluidAction): FluidAmount = {
    if (resource.isEmpty || stack.getCount > 1) {
      return FluidAmount.EMPTY
    }
    init()
    if (fluid.isEmpty) {
      val amount = math.min(resource.amount, getCapacity)
      if (action.execute()) {
        fluid = resource.copy()
        fluid.setAmount(amount)
        updateTag()
      }
      resource.setAmount(amount)
    } else if (fluid.fluidEqual(resource)) {
      val move = math.min(resource.amount, getCapacity - fluid.amount)
      if (action.execute()) {
        fluid.setAmount(fluid.amount + move)
        updateTag()
      }
      fluid.setAmount(move)
    } else {
      FluidAmount.EMPTY
    }
  }

  @NotNull
  def drain(resource: FluidAmount, action: FluidAction): FluidAmount = {
    init()
    if (fluid.isEmpty || resource.isEmpty || stack.getCount > 1) {
      return FluidAmount.EMPTY
    }
    if (fluid.fluidEqual(resource)) {
      val move = math.min(resource.amount, fluid.amount)
      if (action.execute()) {
        fluid.setAmount(fluid.amount - move)
        updateTag()
      }
      resource.setAmount(move)
    } else
      FluidAmount.EMPTY
  }

  @NotNull
  def drain(maxDrain: Int, action: FluidAction): FluidAmount = {
    init()
    if (fluid.isEmpty || maxDrain <= 0 || stack.getCount > 1) {
      return FluidAmount.EMPTY
    }
    val copy = fluid.copy()
    copy.setAmount(maxDrain)
    drain(copy, action)
  }

  private def init(): Unit = {
    if (!initialized) {
      initialized = true
      fluid = FluidAmount.fromNBT(tankNbt)
    }
  }

  private def updateTag(): Unit = {
    if (stack.getCount > 1) {
      return
    }
    Utils.setTileTag(stack, if (!fluid.isEmpty) createTag else null)
  }

  def createTag: CompoundTag = {
    val tag = new CompoundTag
    tag.put(TileTank.NBT_Tier, tier.toNBTTag)
    val tankTag = new CompoundTag
    tankTag.putLong(TileTank.NBT_Capacity, getCapacity)
    getFluid.write(tankTag)
    tag.put(TileTank.NBT_Tank, tankTag)
    tag
  }

  def getTanks = 1

  /**
   * Get the content of tank. If the stack has 2 or more items, returns empty.
   *
   * @return The content if stack size = 1, else empty fluid.
   */
  @NotNull
  def getFluidInTank(tank: Int): FluidAmount = {
    init()
    if (stack.getCount == 1) fluid else FluidAmount.EMPTY
  }

  def getTankCapacity(tank: Int): Int = if (stack.getCount == 1) Utils.toInt(getCapacity) else 0

  def isFluidValid(tank: Int, stack: FluidAmount) = true

  /**
   * Get the fluid, ignoring the stack size.
   *
   * @return the fluid in the tank.
   */
  def getFluid: FluidAmount = {
    init()
    fluid
  }

  def getCapacity: Long = if (tankNbt == null) tier.amount else tankNbt.getLong(TileTank.NBT_Capacity)
}
