package com.kotori316.fluidtank.recipes

import com.google.gson.{JsonArray, JsonObject}
import net.minecraft.data.IFinishedRecipe
import net.minecraft.tags.Tag
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition, TagEmptyCondition}

case class RecipeSerializeHelper(recipe: IFinishedRecipe,
                                 conditions: List[ICondition] = Nil,
                                 saveName: ResourceLocation = null) {
  def addCondition(condition: ICondition): RecipeSerializeHelper =
    copy(conditions = condition :: this.conditions)

  def addTagCondition(tag: Tag[_]): RecipeSerializeHelper =
    addCondition(new NotCondition(new TagEmptyCondition(tag.getId)))

  def build: JsonObject = {
    val o = recipe.getRecipeJson
    if (conditions.nonEmpty)
      o.add("conditions", conditions.foldLeft(new JsonArray) { case (a, c) => a.add(CraftingHelper.serialize(c)); a })
    o
  }

  def location = if (saveName == null) recipe.getID else saveName
}
