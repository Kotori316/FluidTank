package com.kotori316.fluidtank.recipes;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import com.kotori316.fluidtank.Config;

public class EasyCondition implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        return () -> Config.content().easyRecipe();
    }
}
