package com.kotori316.fluidtank.recipes

import com.google.gson.JsonObject
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger
import net.minecraft.data.{CustomRecipeBuilder, IFinishedRecipe, ShapedRecipeBuilder}
import net.minecraft.item.crafting.SpecialRecipeSerializer
import net.minecraft.tags.ITag
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.conditions.ICondition

case class RecipeSerializeHelper(recipe: IFinishedRecipe,
                                 conditions: List[ICondition] = Nil,
                                 saveName: ResourceLocation = null) {
  def this(c: ShapedRecipeBuilder, saveName: ResourceLocation) = {
    this(RecipeSerializeHelper.getConsumeValue(c), saveName = saveName)
  }

  def addCondition(condition: ICondition): RecipeSerializeHelper =
    copy(conditions = condition :: this.conditions)

  def addTagCondition(tag: ITag.INamedTag[_]): RecipeSerializeHelper =
    addCondition(new TagCondition(tag.getName))

  def build: JsonObject = {
    val o = recipe.getRecipeJson
    if (conditions.nonEmpty)
      o.add("conditions", FluidTankDataProvider.makeConditionArray(conditions))
    o
  }

  def location: ResourceLocation = if (saveName == null) recipe.getID else saveName

}

object RecipeSerializeHelper {
  def by(c: ShapedRecipeBuilder, saveName: ResourceLocation = null): RecipeSerializeHelper = new RecipeSerializeHelper(c, saveName)

  def bySpecial(serializer: SpecialRecipeSerializer[_], recipeId: String, saveName: ResourceLocation = null): RecipeSerializeHelper = {
    val c = CustomRecipeBuilder.customRecipe(serializer)
    var t: IFinishedRecipe = null
    c.build(p => t = p, recipeId)
    new RecipeSerializeHelper(t, Nil, saveName)
  }

  private def getConsumeValue(c: ShapedRecipeBuilder): IFinishedRecipe = {
    c.addCriterion("dummy", RecipeUnlockedTrigger.create(new ResourceLocation("dummy:dummy")))
    var t: IFinishedRecipe = null
    c.build(p => t = p)
    t
  }

}
