package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.fluids.{FluidAction, FluidAmount}
import com.kotori316.fluidtank.tiles.Tier
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType

private[items] object ItemUtil {
  def removeOneBucket(stack: ItemStack): ItemStack = {
    Option(stack.getItem)
      .collect {
        case reservoirItem: ReservoirItem => new TankItemFluidHandler(reservoirItem.tier, stack)
        case tankItem: ItemBlockTank => new TankItemFluidHandler(tankItem.blockTank.tier, stack)
      }
      .map { f => f.drain(FluidAmount.AMOUNT_BUCKET.toInt, FluidAction.EXECUTE); f.getContainer }
      .getOrElse(stack)
  }

  def getTankBurnTime(tier: Tier, itemStack: ItemStack, recipeType: RecipeType[_]): Int = {
    val fluid = new TankItemFluidHandler(tier, itemStack).getFluidInTank(0)
    if (fluid.isEmpty) {
      -1 // Use vanilla logic
    } else {
      val time = FuelRegistry.INSTANCE.get(fluid.fluid.getBucket)
      if (time > 0) {
        time * Math.min(1000, fluid.amount).toInt / 1000
      } else {
        -1
      }
    }
  }
}
