package com.kotori316.fluidtank.recipes

import com.google.gson.JsonObject
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger
import net.minecraft.data.recipes.{FinishedRecipe, RecipeBuilder, SpecialRecipeBuilder}
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.crafting.{CraftingRecipe, RecipeSerializer}
import net.minecraftforge.common.crafting.conditions.ICondition

case class RecipeSerializeHelper(recipe: FinishedRecipe,
                                 conditions: List[ICondition] = Nil,
                                 saveName: ResourceLocation = null) {
  def this(c: RecipeBuilder, saveName: ResourceLocation) = {
    this(RecipeSerializeHelper.getConsumeValue(c), saveName = saveName)
  }

  def addCondition(condition: ICondition): RecipeSerializeHelper =
    copy(conditions = condition :: this.conditions)

  def addTagCondition(tag: TagKey[_]): RecipeSerializeHelper =
    addCondition(new TagCondition(tag.location))

  def build: JsonObject = {
    val o = recipe.serializeRecipe()
    if (conditions.nonEmpty)
      o.add("conditions", FluidTankDataProvider.makeConditionArray(conditions))
    o
  }

  def location: ResourceLocation = if (saveName == null) recipe.getId else saveName

}

object RecipeSerializeHelper {
  def by(c: RecipeBuilder, saveName: ResourceLocation = null): RecipeSerializeHelper = new RecipeSerializeHelper(c, saveName)

  def bySpecial(serializer: RecipeSerializer[_ <: CraftingRecipe], recipeId: String, saveName: ResourceLocation = null): RecipeSerializeHelper = {
    val c = SpecialRecipeBuilder.special(serializer)
    var t: FinishedRecipe = null
    c.save(p => t = p, recipeId)
    new RecipeSerializeHelper(t, Nil, saveName)
  }

  private def getConsumeValue(c: RecipeBuilder): FinishedRecipe = {
    val fixed: RecipeBuilder = c.unlockedBy("dummy", RecipeUnlockedTrigger.unlocked(new ResourceLocation("dummy:dummy")))
    var t: FinishedRecipe = null
    fixed.save(p => t = p)
    t
  }

}
