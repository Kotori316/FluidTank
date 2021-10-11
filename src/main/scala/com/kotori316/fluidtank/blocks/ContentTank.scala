package com.kotori316.fluidtank.blocks

import com.google.gson.{JsonDeserializationContext, JsonObject}
import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.items.ItemBlockTank
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.functions.{LootItemConditionalFunction, LootItemFunctionType}
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition

class ContentTank(cond: Array[LootItemCondition]) extends LootItemConditionalFunction(cond) {
  override def run(stack: ItemStack, context: LootContext): ItemStack = {
    val tile = context.getParam(LootContextParams.BLOCK_ENTITY)
    stack.getItem match {
      case tank: ItemBlockTank => tank.blockTank.saveTankNBT(tile, stack)
      case _ =>
    }
    stack
  }

  override def getType: LootItemFunctionType = ModObjects.TANK_CONTENT_LOOT
}

class ContentTankSerializer extends LootItemConditionalFunction.Serializer[ContentTank] {
  override def deserialize(json: JsonObject, c: JsonDeserializationContext, conditionsIn: Array[LootItemCondition]): ContentTank = new ContentTank(conditionsIn)
}
