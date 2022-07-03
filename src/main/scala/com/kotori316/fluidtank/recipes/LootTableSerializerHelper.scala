package com.kotori316.fluidtank.recipes

import com.google.gson.JsonElement
import com.kotori316.fluidtank.blocks.{BlockTank, ContentLootFunction}
import net.minecraft.data.loot.BlockLoot
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.entries.{LootItem, LootPoolSingletonContainer}
import net.minecraft.world.level.storage.loot.functions.LootItemFunction
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.{LootPool, LootTable, LootTables}
import net.minecraftforge.registries.ForgeRegistries

case class LootTableSerializerHelper(block: Block, functions: List[LootItemFunction.Builder]) extends BlockLoot {
  def location: ResourceLocation = ForgeRegistries.BLOCKS.getKey(block)

  def build: JsonElement = {
    val value: LootPoolSingletonContainer.Builder[_] = LootItem.lootTableItem(block)
    this.functions.foreach(value.apply)
    val builder = LootTable.lootTable.withPool(BlockLoot.applyExplosionCondition(block, LootPool.lootPool.setRolls(ConstantValue.exactly(1)).add(value)))
    builder.setParamSet(LootContextParamSets.BLOCK)

    LootTables.serialize(builder.build)
  }

  def add(function: LootItemFunction.Builder): LootTableSerializerHelper =
    this.copy(functions = function :: functions)
}

object LootTableSerializerHelper {
  def withDrop(block: Block): LootTableSerializerHelper = new LootTableSerializerHelper(block, List.empty)

  def withTankContent(tankBlock: BlockTank): LootTableSerializerHelper =
    new LootTableSerializerHelper(tankBlock, List(ContentLootFunction.builder))
}
