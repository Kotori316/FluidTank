package com.kotori316.fluidtank.tank

import com.google.gson.{JsonDeserializationContext, JsonObject}
import com.kotori316.fluidtank.ModTank
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.functions.{LootItemConditionalFunction, LootItemFunctionType}
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition

class ContentTank(cond: Array[LootItemCondition]) extends LootItemConditionalFunction(cond) {
  override def run(stack: ItemStack, context: LootContext): ItemStack = {
    val tile = context.getParam(LootContextParams.BLOCK_ENTITY)
    stack.getItem match {
      case tank: TankBlockItem => tank.getBlock.asInstanceOf[TankBlock].saveTankNBT(tile, stack)
      case _ =>
    }
    stack
  }

  override def getType: LootItemFunctionType = ModTank.Entries.CONTENT_LOOT_FUNCTION_TYPE
}

class ContentTankSerializer extends LootItemConditionalFunction.Serializer[ContentTank] {
  override def deserialize(json: JsonObject, context: JsonDeserializationContext, conditions: Array[LootItemCondition]): ContentTank =
    new ContentTank(conditions)
}
