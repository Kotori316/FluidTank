package com.kotori316.fluidtank.integration.jei;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Size2i;
import org.apache.commons.lang3.tuple.Pair;

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
        List<Ingredient> inputs = recipe.allSlot().sorted(Comparator.comparingInt(Pair::getKey))
            .map(Pair::getValue)
            .collect(Collectors.toList());

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
