package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.tiles.Tiers
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeType
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction

private[items] object ItemUtil {
  def removeOneBucket(stack: ItemStack): ItemStack = {
    import com.kotori316.fluidtank._
    FluidUtil.getFluidHandler(stack.copy()).asScala
      .map { f => f.drain(FluidAmount.AMOUNT_BUCKET, FluidAction.EXECUTE); f.getContainer }
      .getOrElse(stack)
      .value
  }

  def getTankBurnTime(tier: Tiers, itemStack: ItemStack, recipeType: IRecipeType[_]): Int = {
    val fluid = new TankItemFluidHandler(tier, itemStack).getFluidInTank(0)
    if (fluid.isEmpty) {
      -1 // Use vanilla logic
    } else {
      val time = ForgeHooks.getBurnTime(new ItemStack(fluid.getFluid.getFilledBucket), recipeType)
      if (time > 0) {
        time * Math.min(1000, fluid.getAmount) / 1000
      } else {
        -1
      }
    }
  }
}
