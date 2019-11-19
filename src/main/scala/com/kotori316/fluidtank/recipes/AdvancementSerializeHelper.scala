package com.kotori316.fluidtank.recipes

import com.google.gson.JsonObject
import net.minecraft.advancements.criterion._
import net.minecraft.advancements.{Advancement, AdvancementRewards, ICriterionInstance, IRequirementsStrategy}
import net.minecraft.item.Item
import net.minecraft.tags.Tag
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition, TagEmptyCondition}

case class AdvancementSerializeHelper(location: ResourceLocation,
                                      criterionList: List[(String, ICriterionInstance)] = Nil,
                                      conditions: List[ICondition] = Nil) {

  def addCriterion(name: String, criterion: ICriterionInstance): AdvancementSerializeHelper =
    copy(criterionList = (name, criterion) :: criterionList)

  def addItemCriterion(item: Item): AdvancementSerializeHelper =
    addCriterion(s"has_${item.getRegistryName.getPath}", InventoryChangeTrigger.Instance.forItems(item))

  def addItemCriterion(tag: Tag[Item]): AdvancementSerializeHelper =
    addCriterion(s"has_${tag.getId.getPath}", InventoryChangeTrigger.Instance.forItems(ItemPredicate.Builder.create().tag(tag).build()))
      .addCondition(new NotCondition(new TagEmptyCondition(tag.getId)))

  def addCondition(condition: ICondition): AdvancementSerializeHelper =
    copy(conditions = condition :: conditions)

  def build: JsonObject = {
    val builder = Advancement.Builder.builder()
    builder.withParentId(new ResourceLocation("recipes/root"))
      .withCriterion("has_the_recipe", new RecipeUnlockedTrigger.Instance(location))
      .withRewards(AdvancementRewards.Builder.recipe(location))
      .withRequirementsStrategy(IRequirementsStrategy.OR)
    val obj = criterionList.foldLeft(builder) { case (b, (s, c)) => b.withCriterion(s, c) }
      .serialize()
    obj.add("conditions", FluidTankDataProvider.makeConditionArray(conditions))
    obj
  }
}
