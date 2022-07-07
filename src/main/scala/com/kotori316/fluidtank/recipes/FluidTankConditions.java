package com.kotori316.fluidtank.recipes;

import java.util.function.Predicate;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.resources.ResourceLocation;

import com.kotori316.fluidtank.FluidTank;

public abstract class FluidTankConditions<T extends FluidTankConditions<T>> implements Predicate<JsonObject> {
    public FluidTankConditions(ResourceLocation location) {
        this.location = location;
    }

    private final ResourceLocation location;
    public final ConditionJsonProvider serializer = new Serializer();

    public ResourceLocation getID() {
        return this.location;
    }

    @Override
    public abstract boolean test(JsonObject context);

    private class Serializer implements ConditionJsonProvider {
        @Override
        public void writeParameters(JsonObject object) {
        }

        @Override
        public ResourceLocation getConditionId() {
            return FluidTankConditions.this.getID();
        }

    }

    public static final class ConfigCondition extends FluidTankConditions<ConfigCondition> {

        public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "config");

        public ConfigCondition() {
            super(LOCATION);
        }

        @Override
        public boolean test(JsonObject context) {
            return FluidTank.config.enableUpdateRecipe;
        }
    }

    public static final class EasyCondition extends FluidTankConditions<EasyCondition> {

        public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "easy");

        public EasyCondition() {
            super(LOCATION);
        }

        @Override
        public boolean test(JsonObject context) {
            return FluidTank.config.easyRecipe;
        }
    }

}
