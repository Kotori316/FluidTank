package com.kotori316.fluidtank.recipe;

import java.util.function.Predicate;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.resources.ResourceLocation;

import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.TankConstant;

public final class RecipeConfigCondition implements Predicate<JsonObject> {
    @Override
    public boolean test(JsonObject object) {
        return isUpdateRecipeEnabled();
    }

    public static boolean isUpdateRecipeEnabled() {
        return TankConstant.config.enableUpdateRecipe;
    }

    public static final class Provider implements ConditionJsonProvider {
        public static final ResourceLocation ID = new ResourceLocation(ModTank.modID, "config_update_recipe");

        @Override
        public ResourceLocation getConditionId() {
            return ID;
        }

        @Override
        public void writeParameters(JsonObject object) {
        }
    }
}
