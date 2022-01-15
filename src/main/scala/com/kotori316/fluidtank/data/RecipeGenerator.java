package com.kotori316.fluidtank.data;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipesProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.recipe.TierRecipe;
import com.kotori316.fluidtank.tank.Tiers;

final class RecipeGenerator extends FabricRecipesProvider {
    RecipeGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> exporter) {
        Tiers.stream()
            .filter(Tiers::hasTagRecipe)
            .map(RecipeGenerator::getTierRecipes)
            .forEach(withTierCondition(exporter));
    }

    @SuppressWarnings("unchecked")
    private Consumer<TierFinished> withTierCondition(Consumer<FinishedRecipe> exporter) {
        return recipePair -> {
            if (recipePair.shouldUseTagCondition()) {
                var tag = recipePair.tier.tagName;
                var tagCondition = DefaultResourceConditions.itemTagsPopulated(TagFactory.ITEM.create(new ResourceLocation(tag)));
                withConditions(exporter, tagCondition).accept(recipePair.recipe);
            } else {
                exporter.accept(recipePair.recipe);
            }
        };
    }

    private static TierFinished getTierRecipes(Tiers tier) {
        var recipe = new TierRecipe(new ResourceLocation(ModTank.modID, "tank_" + tier.toString().toLowerCase()), tier);
        return new TierFinished(recipe.getFinishedRecipe(), tier);
    }

    private record TierFinished(FinishedRecipe recipe, Tiers tier) {
        boolean shouldUseTagCondition() {
            return tier.getAlternative().isEmpty();
        }
    }
}
