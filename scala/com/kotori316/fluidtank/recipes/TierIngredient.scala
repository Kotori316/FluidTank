package com.kotori316.fluidtank.recipes

import com.kotori316.fluidtank.FluidTank
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient

class TierIngredient(val rank: Int) extends Ingredient(
    FluidTank.BLOCK_TANKS.get(rank - 1).itemBlock.itemList.flatMap {
        case (i, m) if i.hasRecipe => Seq(new ItemStack(i, 1, m), new ItemStack(i, 1, m | 8))
    }: _*
)
