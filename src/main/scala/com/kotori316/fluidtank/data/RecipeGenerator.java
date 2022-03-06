package com.kotori316.fluidtank.data;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.minecraft.advancements.critereon.FilledBucketTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.recipe.RecipeConfigCondition;
import com.kotori316.fluidtank.recipe.TierRecipe;
import com.kotori316.fluidtank.tank.Tiers;

final class RecipeGenerator extends FabricRecipeProvider {
    RecipeGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> exporter) {
        Tiers.stream()
            .filter(Tiers::hasTagRecipe)
            .map(RecipeGenerator::getTierRecipes)
            .forEach(withTierCondition(exporter));
        ShapedRecipeBuilder.shaped(ModTank.Entries.WOOD_TANK)
            .pattern("x x")
            .pattern("xpx")
            .pattern("xxx")
            .define('x', Items.GLASS)
            .define('p', ItemTags.LOGS)
            .unlockedBy("has_glass", FabricRecipeProvider.has(Items.GLASS))
            .unlockedBy("has_bucket", FilledBucketTrigger.TriggerInstance.filledBucket(ItemPredicate.Builder.item().of(Items.WATER_BUCKET).build()))
            .save(withConditions(exporter, new RecipeConfigCondition.Provider()));
        ShapedRecipeBuilder.shaped(ModTank.Entries.VOID_TANK)
            .pattern("ooo")
            .pattern("oto")
            .pattern("ooo")
            .define('o', Items.OBSIDIAN)
            .define('t', ModTank.Entries.WOOD_TANK)
            .unlockedBy("has_obsidian", FabricRecipeProvider.has(Items.OBSIDIAN))
            .unlockedBy("has_tank", FabricRecipeProvider.has(ModTank.Entries.WOOD_TANK))
            .save(exporter);
    }

    private Consumer<TierFinished> withTierCondition(Consumer<FinishedRecipe> exporter) {
        var configCondition = new RecipeConfigCondition.Provider();
        return recipePair -> {
            ConditionJsonProvider[] conditions;
            if (recipePair.shouldUseTagCondition()) {
                var tag = recipePair.tier.tagName;
                var tagCondition = tagProvider(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(tag)));
                conditions = new ConditionJsonProvider[]{configCondition, tagCondition};
            } else {
                conditions = new ConditionJsonProvider[]{configCondition};
            }
            withConditions(exporter, conditions).accept(recipePair.recipe);
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

    @SuppressWarnings("unchecked")
    private static ConditionJsonProvider tagProvider(TagKey<Item> tag) {
        return DefaultResourceConditions.itemTagsPopulated(tag);
    }

    @Override
    public String getName() {
        return "%s of %s".formatted(super.getName(), ModTank.modID);
    }
}
