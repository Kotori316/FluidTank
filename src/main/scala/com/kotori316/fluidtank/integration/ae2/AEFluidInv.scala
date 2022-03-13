package com.kotori316.fluidtank.integration.ae2

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.{AEFluidKey, AEKey, KeyCounter}
import appeng.api.storage.MEStorage
import com.kotori316.fluidtank.Utils
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidKey}
import com.kotori316.fluidtank.tiles.TileTank
import net.minecraft.network.chat.Component
import net.minecraftforge.fluids.FluidAttributes

case class AEFluidInv(tank: TileTank) extends MEStorage {
  override def insert(what: AEKey, amount: Long, actionable: Actionable, source: IActionSource): Long = {
    val fluidAmount = fromAEStack(what, amount)
    val filled = tank.connection.handler.fill(fluidAmount, actionable.getFluidAction)
    filled.amount
  }

  override def extract(what: AEKey, amount: Long, actionable: Actionable, source: IActionSource): Long = {
    val fluidAmount = fromAEStack(what, amount)
    val drained = tank.connection.handler.drain(fluidAmount, actionable.getFluidAction)
    drained.amount
  }

  override def getAvailableStacks(out: KeyCounter): Unit = {
    val amount = tank.connection.getFluidStack
    amount.foreach(fluidAmount =>
      out.add(AEFluidKey.of(fluidAmount.fluid, fluidAmount.nbt.orNull), Math.min(fluidAmount.amount, Long.MaxValue - Integer.MAX_VALUE * 2L)))
  }

  private def fromAEStack(what: AEKey, amount: Long): FluidAmount =
    what match {
      case fluidKey: AEFluidKey => FluidAmount.fromStack(fluidKey.toStack(Utils.toInt(amount))).setAmount(amount)
      case _ => FluidAmount.EMPTY
    }

  override def getDescription: Component = this.tank.getName

  override def isPreferredStorageFor(what: AEKey, source: IActionSource): Boolean = {
    what match {
      case fluidKey: AEFluidKey =>
        val tankKey = FluidKey.from(tank.connection.fluidType)
        if (tankKey.fluid.isSame(fluidKey.getFluid))
          tankKey.tag.isEmpty && !fluidKey.hasTag // No NBT is in stack.
        else
          fluidKey.matches(tankKey.createStack(FluidAttributes.BUCKET_VOLUME)) // Check NBT as FluidStack
      case _ => false
    }
  }
}
