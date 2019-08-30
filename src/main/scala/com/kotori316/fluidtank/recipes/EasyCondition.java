package com.kotori316.fluidtank.recipes;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;

public class EasyCondition implements ICondition {
    public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "easy");
    private static final EasyCondition INSTANCE = new EasyCondition();

    public static EasyCondition getInstance() {
        return INSTANCE;
    }

    @Override
    public ResourceLocation getID() {
        return LOCATION;
    }

    @Override
    public boolean test() {
        return Config.content().easyRecipe().get();
    }

    public static class Serializer implements IConditionSerializer<EasyCondition> {
        @Override
        public void write(JsonObject json, EasyCondition value) {
        }

        @Override
        public EasyCondition read(JsonObject json) {
            return getInstance();
        }

        @Override
        public ResourceLocation getID() {
            return LOCATION;
        }
    }
}