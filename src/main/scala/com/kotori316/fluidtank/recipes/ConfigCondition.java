package com.kotori316.fluidtank.recipes;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IConditionSerializer;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;

public class ConfigCondition implements IConditionSerializer {
    public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "config");

    @Override
    public BooleanSupplier parse(JsonObject json) {
        return () -> !Config.content().removeRecipe().get();
    }
}
