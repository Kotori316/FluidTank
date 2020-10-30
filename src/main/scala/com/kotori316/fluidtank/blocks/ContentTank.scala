package com.kotori316.fluidtank.blocks

import com.google.gson.{JsonDeserializationContext, JsonObject}
import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.items.ItemBlockTank
import net.minecraft.item.ItemStack
import net.minecraft.loot.conditions.ILootCondition
import net.minecraft.loot.{LootContext, LootFunction, LootFunctionType, LootParameters}

class ContentTank(cond: Array[ILootCondition]) extends LootFunction(cond) {
  override def doApply(stack: ItemStack, context: LootContext): ItemStack = {
    val tile = context.get(LootParameters.BLOCK_ENTITY)
    stack.getItem match {
      case tank: ItemBlockTank => tank.blockTank.saveTankNBT(tile, stack)
      case _ =>
    }
    stack
  }

  override def getFunctionType: LootFunctionType = ModObjects.TANK_CONTENT_LOOT
}

class ContentTankSerializer extends LootFunction.Serializer[ContentTank] {
  override def deserialize(json: JsonObject, c: JsonDeserializationContext, conditionsIn: Array[ILootCondition]): ContentTank = new ContentTank(conditionsIn)
}
