package com.kotori316.fluidtank.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import com.kotori316.fluidtank.FluidTank;

public class TierIngredient extends Ingredient {

    public TierIngredient(int rank) {
        super(FluidTank.BLOCK_TANKS.get(rank - 1).itemBlock().itemList().stream()
            .map(t -> new ItemStack(t._1, 1, t._2)).toArray(ItemStack[]::new));
    }
}
