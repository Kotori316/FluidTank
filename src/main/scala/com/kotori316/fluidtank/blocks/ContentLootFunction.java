package com.kotori316.fluidtank.blocks;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.items.ItemBlockTank;

public final class ContentLootFunction extends LootItemConditionalFunction {
    public static final String NAME = "content_tank";

    public ContentLootFunction(LootItemCondition[] pConditions) {
        super(pConditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var tile = context.getParam(LootContextParams.BLOCK_ENTITY);
        if (stack.getItem() instanceof ItemBlockTank tank) {
            tank.blockTank().saveTankNBT(tile, stack);
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return ModObjects.TANK_CONTENT_LOOT();
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return LootItemConditionalFunction.simpleBuilder(ContentLootFunction::new);
    }

    public static class ContentTankSerializer extends LootItemConditionalFunction.Serializer<ContentLootFunction> {

        @Override
        public ContentLootFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
            return new ContentLootFunction(pConditions);
        }
    }
}
