package com.kotori316.fluidtank.integration.jei;

import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.recipes.TierRecipe;

@JeiPlugin
public class FluidTankJeiPlugin implements IModPlugin {
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager manager = Minecraft.getInstance().world.getRecipeManager();
        List<?> recipeExtensions = manager.getRecipes().stream()
            .filter(TierRecipe.class::isInstance)
            .map(TierRecipe.class::cast)
            .collect(Collectors.toList());

//        registration.addRecipes(recipeExtensions, VanillaRecipeCategoryUid.CRAFTING);
    }


    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        IExtendableRecipeCategory<ICraftingRecipe, ICraftingCategoryExtension> category = registration.getCraftingCategory();
        category.addCategoryExtension(TierRecipe.class, TierRecipeExtension::new);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(FluidTank.modID, "jei_recipe");
    }
}
