package com.kotori316.fluidtank.blocks

import com.google.gson.{JsonDeserializationContext, JsonObject}
import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.items.ItemBlockTank
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.conditions.ILootCondition
import net.minecraft.world.storage.loot.{LootContext, LootFunction, LootParameters}

class ContentTank(cond: Array[ILootCondition]) extends LootFunction(cond) {
  override def doApply(stack: ItemStack, context: LootContext): ItemStack = {
    val tile = context.get(LootParameters.BLOCK_ENTITY)
    stack.getItem match {
      case tank: ItemBlockTank => tank.blockTank.saveTankNBT(tile, stack)
      case _ =>
    }
    stack
  }
}

class ContentTankSerializer extends LootFunction.Serializer[ContentTank](
  new ResourceLocation(FluidTank.modID, "content_tank"),
  classOf[ContentTank]
) {
  override def deserialize(json: JsonObject, c: JsonDeserializationContext, conditionsIn: Array[ILootCondition]): ContentTank = new ContentTank(conditionsIn)
}
