package com.kotori316.fluidtank.integration.jei;

import java.util.Arrays;
import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.IShapedCraftingCategoryExtension;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import com.kotori316.fluidtank.recipes.TierRecipe;

public class TierRecipeExtension implements IShapedCraftingCategoryExtension {
    private final TierRecipe recipe;

    public TierRecipeExtension(TierRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
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

}
