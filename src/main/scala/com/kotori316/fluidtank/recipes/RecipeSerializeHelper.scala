package com.kotori316.fluidtank.recipes

import com.google.gson.JsonObject
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger
import net.minecraft.data.{IFinishedRecipe, ShapedRecipeBuilder}
import net.minecraft.tags.Tag
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition, TagEmptyCondition}

case class RecipeSerializeHelper(recipe: IFinishedRecipe,
                                 conditions: List[ICondition] = Nil,
                                 saveName: ResourceLocation = null) {
  def this(c: ShapedRecipeBuilder, saveName: ResourceLocation) = {
    this(RecipeSerializeHelper.getConsumeValue(c), saveName = saveName)
  }

  def addCondition(condition: ICondition): RecipeSerializeHelper =
    copy(conditions = condition :: this.conditions)

  def addTagCondition(tag: Tag[_]): RecipeSerializeHelper =
    addCondition(new NotCondition(new TagEmptyCondition(tag.getId)))

  def build: JsonObject = {
    val o = recipe.getRecipeJson
    if (conditions.nonEmpty)
      o.add("conditions", FluidTankDataProvider.makeConditionArray(conditions))
    o
  }

  def location = if (saveName == null) recipe.getID else saveName

}

object RecipeSerializeHelper {
  def by(c: ShapedRecipeBuilder, saveName: ResourceLocation = null): RecipeSerializeHelper = new RecipeSerializeHelper(c, saveName)

  private def getConsumeValue(c: ShapedRecipeBuilder): IFinishedRecipe = {
    c.addCriterion("dummy", new RecipeUnlockedTrigger.Instance(new ResourceLocation("dummy:dummy")))
    var t: IFinishedRecipe = null
    c.build(p => t = p)
    t
  }
}
