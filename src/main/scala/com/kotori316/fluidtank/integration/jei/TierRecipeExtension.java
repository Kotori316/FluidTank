package com.kotori316.fluidtank.integration.jei;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Size2i;

import com.kotori316.fluidtank.recipes.TierRecipe;

public class TierRecipeExtension implements ICraftingCategoryExtension {
    private final TierRecipe recipe;

    public TierRecipeExtension(TierRecipe recipe) {
        this.recipe = recipe;
    }

    public int getWidth() {
        return 3;
    }

    public int getHeight() {
        return 3;
    }

    @Override
    public void setIngredients(IIngredients ingredients) {
        Ingredient t = recipe.getTankItems();
        Ingredient s = recipe.getSubItems();
        Ingredient e = Ingredient.EMPTY;

        List<Ingredient> inputs = Arrays.asList(
            t, s, t,
            s, e, s,
            t, s, t
        );

        ingredients.setInputIngredients(inputs);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
    }

    @Override
    public ResourceLocation getRegistryName() {
        return recipe.getId();
    }

    @Nullable
    @Override
    public Size2i getSize() {
        return new Size2i(getWidth(), getHeight());
    }
}
