package com.kotori316.fluidtank.integration.mekanism_gas;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import com.kotori316.fluidtank.ModObjects;

public final class GasContentLootFunction extends LootItemConditionalFunction {
    public static final String NAME = "content_gas_tank";

    public GasContentLootFunction(LootItemCondition[] pConditions) {
        super(pConditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var tile = context.getParam(LootContextParams.BLOCK_ENTITY);
        if (stack.getItem() instanceof ItemBlockGasTank tank) {
            tank.blockGasTank().saveTankTag(tile, stack, tile.getBlockPos());
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return ModObjects.GAS_TANK_CONTENT_LOOT();
    }

    public static Builder<?> builder() {
        return LootItemConditionalFunction.simpleBuilder(GasContentLootFunction::new);
    }

    public static class GasContentTankSerializer extends Serializer<GasContentLootFunction> {

        @Override
        public GasContentLootFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
            return new GasContentLootFunction(pConditions);
        }
    }
}
