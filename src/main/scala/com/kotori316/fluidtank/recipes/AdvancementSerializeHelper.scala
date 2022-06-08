package com.kotori316.fluidtank.recipes

import com.google.gson.JsonObject
import net.minecraft.advancements.critereon.{InventoryChangeTrigger, ItemPredicate, RecipeUnlockedTrigger}
import net.minecraft.advancements.{Advancement, AdvancementRewards, CriterionTriggerInstance, RequirementsStrategy}
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraftforge.common.crafting.conditions.ICondition
import net.minecraftforge.registries.ForgeRegistries

case class AdvancementSerializeHelper(location: ResourceLocation,
                                      criterionList: List[(String, CriterionTriggerInstance)] = Nil,
                                      conditions: List[ICondition] = Nil) {

  def addCriterion(name: String, criterion: CriterionTriggerInstance): AdvancementSerializeHelper =
    copy(criterionList = (name, criterion) :: criterionList)

  def addItemCriterion(item: Item): AdvancementSerializeHelper =
    addCriterion(s"has_${ForgeRegistries.ITEMS.getKey(item).getPath}", InventoryChangeTrigger.TriggerInstance.hasItems(item))

  def addItemCriterion(tag: TagKey[Item]): AdvancementSerializeHelper =
    addCriterion(s"has_${tag.location.getPath}", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build()))
      .addCondition(new TagCondition(tag.location))

  def addCondition(condition: ICondition): AdvancementSerializeHelper =
    copy(conditions = condition :: conditions)

  def build: JsonObject = {
    val builder = Advancement.Builder.advancement()
    builder.parent(new ResourceLocation("recipes/root"))
      .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(location))
      .rewards(AdvancementRewards.Builder.recipe(location))
      .requirements(RequirementsStrategy.OR)
    val obj = criterionList.foldLeft(builder) { case (b, (s, c)) => b.addCriterion(s, c) }
      .serializeToJson()
    obj.add("conditions", FluidTankDataProvider.makeConditionArray(conditions))
    obj
  }
}
