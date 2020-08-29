package com.kotori316.fluidtank.tank

import com.google.gson.{JsonDeserializationContext, JsonObject}
import com.kotori316.fluidtank.ModTank
import net.minecraft.item.ItemStack
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.context.{LootContext, LootContextParameters}
import net.minecraft.loot.function.{ConditionalLootFunction, LootFunctionType}

class ContentTank(cond: Array[LootCondition]) extends ConditionalLootFunction(cond) {
  override def process(stack: ItemStack, context: LootContext): ItemStack = {
    val tile = context.get(LootContextParameters.BLOCK_ENTITY)
    stack.getItem match {
      case tank: TankBlockItem => tank.getBlock.asInstanceOf[TankBlock].saveTankNBT(tile, stack)
      case _ =>
    }
    stack
  }

  override def getType: LootFunctionType = ModTank.Entries.CONTENT_LOOT_FUNCTION_TYPE
}

class ContentTankSerializer extends ConditionalLootFunction.Serializer[ContentTank] {
  override def fromJson(json: JsonObject, context: JsonDeserializationContext, conditions: Array[LootCondition]): ContentTank =
    new ContentTank(conditions)
}
