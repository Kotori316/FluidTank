package com.kotori316.fluidtank.recipes

import com.google.gson.JsonObject
import net.minecraft.advancements.criterion._
import net.minecraft.advancements.{Advancement, AdvancementRewards, ICriterionInstance, IRequirementsStrategy}
import net.minecraft.item.Item
import net.minecraft.tags.ITag
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.conditions.ICondition

case class AdvancementSerializeHelper(location: ResourceLocation,
                                      criterionList: List[(String, ICriterionInstance)] = Nil,
                                      conditions: List[ICondition] = Nil) {

  def addCriterion(name: String, criterion: ICriterionInstance): AdvancementSerializeHelper =
    copy(criterionList = (name, criterion) :: criterionList)

  def addItemCriterion(item: Item): AdvancementSerializeHelper =
    addCriterion(s"has_${item.getRegistryName.getPath}", InventoryChangeTrigger.Instance.forItems(item))

  def addItemCriterion(tag: ITag.INamedTag[Item]): AdvancementSerializeHelper =
    addCriterion(s"has_${tag.func_230234_a_().getPath}", InventoryChangeTrigger.Instance.forItems(ItemPredicate.Builder.create().tag(tag).build()))
      .addCondition(new TagCondition(tag.func_230234_a_()))

  def addCondition(condition: ICondition): AdvancementSerializeHelper =
    copy(conditions = condition :: conditions)

  def build: JsonObject = {
    val builder = Advancement.Builder.builder()
    builder.withParentId(new ResourceLocation("recipes/root"))
      .withCriterion("has_the_recipe", RecipeUnlockedTrigger.func_235675_a_(location))
      .withRewards(AdvancementRewards.Builder.recipe(location))
      .withRequirementsStrategy(IRequirementsStrategy.OR)
    val obj = criterionList.foldLeft(builder) { case (b, (s, c)) => b.withCriterion(s, c) }
      .serialize()
    obj.add("conditions", FluidTankDataProvider.makeConditionArray(conditions))
    obj
  }
}
