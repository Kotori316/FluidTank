package com.kotori316.fluidtank.recipes;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;

public class ConfigCondition implements ICondition {
    public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "config");
    private static final ConfigCondition INSTANCE = new ConfigCondition();

    public static ConfigCondition getInstance() {
        return INSTANCE;
    }

    @Override
    public ResourceLocation getID() {
        return LOCATION;
    }

    @Override
    public boolean test() {
        return !Config.content().removeRecipe().get();
    }

    public static class Serializer implements IConditionSerializer<ConfigCondition> {
        @Override
        public void write(JsonObject json, ConfigCondition value) {
        }

        @Override
        public ConfigCondition read(JsonObject json) {
            return getInstance();
        }

        @Override
        public ResourceLocation getID() {
            return LOCATION;
        }
    }
}
