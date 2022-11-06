package com.kotori316.fluidtank.integration.mekanism_gas

import com.kotori316.fluidtank.Utils
import com.kotori316.fluidtank.tiles.{Tier, TileTank}
import mekanism.api.Action
import mekanism.api.chemical.IChemicalHandler
import mekanism.api.chemical.gas.{Gas, GasStack, IGasHandler}
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.{BlockItem, ItemStack}
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional

class TankItemGasHandler(tier: Tier, stack: ItemStack) extends IChemicalHandler[Gas, GasStack] with IGasHandler {
  private[this] var initialized = false
  private[this] var content: GasAmount = GasAmount.EMPTY
  private[this] var capacity: Long = tier.amount()

  override def getTanks: Int = 1

  override def getChemicalInTank(tank: Int): GasStack = {
    init()
    content.toStack
  }

  override def setChemicalInTank(tank: Int, stack: GasStack): Unit = {
    if (this.stack.getCount > 1) return
    this.content = GasAmount.fromStack(stack)
    updateTag()
  }

  override def getTankCapacity(tank: Int): Long = {
    init()
    capacity
  }

  override def isValid(tank: Int, stack: GasStack): Boolean = true

  override def insertChemical(tank: Int, stack: GasStack, action: Action): GasStack = {
    if (stack.isEmpty || this.stack.getCount > 1) return stack

    init()
    if (this.content.isEmpty) {
      val inserted = math.min(stack.getAmount, this.capacity)
      if (action.execute()) {
        this.content = GasAmount.fromStack(stack).setAmount(inserted)
        updateTag()
      }
      new GasStack(stack, stack.getAmount - inserted)
    } else if (stack.isTypeEqual(this.content.c)) {
      val inserted = math.min(stack.getAmount, this.capacity - this.content.amount)
      if (action.execute()) {
        this.content = this.content.setAmount(this.content.amount + inserted)
        updateTag()
      }
      new GasStack(stack, stack.getAmount - inserted)
    } else {
      stack
    }
  }

  override def extractChemical(tank: Int, amount: Long, action: Action): GasStack = {
    init()
    if (this.content.isEmpty || amount <= 0 || this.stack.getCount > 1) return getEmptyStack

    val drained = math.min(amount, this.content.amount)
    if (action.execute()) {
      this.content = this.content.setAmount(this.content.amount - drained)
      updateTag()
    }
    this.content.setAmount(drained).toStack
  }

  private[this] def init(): Unit = {
    if (!initialized) {
      initialized = true
      val tankNbt = BlockItem.getBlockEntityData(stack)
      if (tankNbt == null) {
        this.capacity = tier.amount()
        this.content = GasAmount.EMPTY
      } else {
        this.capacity = tankNbt.getLong(TileTank.NBT_Capacity)
        this.content = GasAmount.fromTag(tankNbt.getCompound("stored"))
      }
    }
  }

  private[this] def updateTag(): Unit = {
    if (stack.getCount > 1) return
    Utils.setTileTag(stack, if (content.nonEmpty) createTag else null)
  }

  def createTag: CompoundTag = {
    val tag = new CompoundTag()
    tag.putLong(TileTank.NBT_Capacity, this.capacity)
    tag.put("stored", content.toStack.write(new CompoundTag()))
    tag
  }
}

private[mekanism_gas] class GasCapProvider(tier: Tier, stack: ItemStack) extends ICapabilityProvider {
  val gasHandler = LazyOptional.of(() => new TankItemGasHandler(tier, stack).asInstanceOf[IGasHandler])

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    Constant.GAS_HANDLER_CAPABILITY.orEmpty(cap, gasHandler)
  }
}
