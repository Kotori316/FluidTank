package com.kotori316.fluidtank.recipes;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;

public abstract class FluidTankConditions<T extends FluidTankConditions<T>> implements ICondition {
    public FluidTankConditions(ResourceLocation location) {
        this.location = location;
    }

    private final ResourceLocation location;
    public final IConditionSerializer<FluidTankConditions<T>> serializer = new Serializer();

    @Override
    public ResourceLocation getID() {
        return this.location;
    }

    @Override
    public abstract boolean test();

    private class Serializer implements IConditionSerializer<FluidTankConditions<T>> {
        @Override
        public void write(JsonObject json, FluidTankConditions<T> value) {
        }

        @Override
        public FluidTankConditions<T> read(JsonObject json) {
            return FluidTankConditions.this;
        }

        @Override
        public ResourceLocation getID() {
            return FluidTankConditions.this.getID();
        }
    }

    public static final class ConfigCondition extends FluidTankConditions<ConfigCondition> {

        public ConfigCondition() {
            super(new ResourceLocation(FluidTank.modID, "config"));
        }

        @Override
        public boolean test() {
            return !Config.content().removeRecipe().get();
        }
    }

    public static final class EasyCondition extends FluidTankConditions<EasyCondition> {

        public EasyCondition() {
            super(new ResourceLocation(FluidTank.modID, "easy"));
        }

        @Override
        public boolean test() {
            return Config.content().easyRecipe().get();
        }
    }

    public static final class InvisibleCondition extends FluidTankConditions<InvisibleCondition> {

        public InvisibleCondition() {
            super(new ResourceLocation(FluidTank.modID, "invisible_ok"));
        }

        @Override
        public boolean test() {
            return Config.content().usableInvisibleInRecipe().get();
        }
    }
}
