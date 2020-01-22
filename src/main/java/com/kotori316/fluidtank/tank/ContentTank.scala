package com.kotori316.fluidtank.tank

import com.google.gson.{JsonDeserializationContext, JsonObject}
import com.kotori316.fluidtank.ModTank
import net.minecraft.item.ItemStack
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.context.{LootContext, LootContextParameters}
import net.minecraft.loot.function.ConditionalLootFunction
import net.minecraft.util.Identifier

class ContentTank(cond: Array[LootCondition]) extends ConditionalLootFunction(cond) {
  override def process(stack: ItemStack, context: LootContext): ItemStack = {
    val tile = context.get(LootContextParameters.BLOCK_ENTITY)
    stack.getItem match {
      case tank: TankBlockItem => tank.getBlock.asInstanceOf[TankBlock].saveTankNBT(tile, stack)
      case _ =>
    }
    stack
  }
}

class ContentTankSerializer extends ConditionalLootFunction.Factory[ContentTank](
  new Identifier(ModTank.modID, "content_tank"),
  classOf[ContentTank]
) {
  override def fromJson(json: JsonObject, context: JsonDeserializationContext, conditions: Array[LootCondition]) = new ContentTank(conditions)
}
