package com.kotori316.fluidtank.recipes;

import java.util.Arrays;
import java.util.Locale;

import javax.annotation.Nonnull;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.tiles.Tiers;

public class TankRecipe extends ShapedRecipes {

    private final Tiers tiers;
    private final boolean valid;

    public TankRecipe(Tiers tiers) {
        super("", 3, 3, NonNullList.withSize(9, Ingredient.EMPTY), ItemStack.EMPTY);

        setRegistryName(new ResourceLocation(FluidTank.modID + ":tank" + tiers.toString().toLowerCase(Locale.US)));
        this.tiers = tiers;
        OreIngredient oreIngredient = new OreIngredient(tiers.oreName());
        valid = tiers.rank() > 1 && OreDictionary.doesOreNameExist(tiers.oreName());
        if (valid) {
            TierIngredient tierIngredient = new TierIngredient(tiers.rank() - 1);
            Arrays.stream(new int[]{0, 2, 6, 8}).forEach(value -> recipeItems.set(value, oreIngredient));
            Arrays.stream(new int[]{1, 3, 5, 7}).forEach(value -> recipeItems.set(value, tierIngredient));
        }
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
        for (int x = 0; x <= inv.getWidth() - 3; x++) {
            for (int y = 0; y <= inv.getHeight() - 3; ++y) {
                if (checkMatch(inv, x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Based on {@link net.minecraft.item.crafting.ShapedRecipes#checkMatch(InventoryCrafting, int, int, boolean)}
     */
    protected boolean checkMatch(InventoryCrafting inv, int startX, int startY) {
        for (int x = 0; x < inv.getWidth(); x++) {
            for (int y = 0; y < inv.getHeight(); y++) {
                int subX = x - startX;
                int subY = y - startY;
                Ingredient target = Ingredient.EMPTY;

                if (subX >= 0 && subY >= 0 && subX < 3 && subY < 3) {
                    target = recipeItems.get(subX + subY * 3);
                }

                if (!target.apply(inv.getStackInRowAndColumn(x, y))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return getRecipeOutput().copy();
    }

    @Override
    public ItemStack getRecipeOutput() {
        return valid ? new ItemStack(FluidTank.BLOCK_TANKS.get(tiers.rank() - 1), 1, tiers.meta()) : super.getRecipeOutput();
    }

    public boolean isValid() {
        return valid;
    }
}
