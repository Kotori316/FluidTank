package com.kotori316.fluidtank.recipes

import com.google.gson.JsonObject
import com.kotori316.fluidtank.DynamicSerializable._
import com.kotori316.fluidtank._
import com.kotori316.fluidtank.tiles.Tiers
import com.mojang.datafixers
import com.mojang.datafixers.types.{DynamicOps, JsonOps}
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger
import net.minecraft.data.{IFinishedRecipe, ShapedRecipeBuilder}
import net.minecraft.item.crafting.Ingredient
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

  val TierRecipeSerializer: DynamicSerializable[TierRecipe] = new DynamicSerializable[TierRecipe] {
    private[this] final val LOGGER = org.apache.logging.log4j.LogManager.getLogger(classOf[TierRecipe])

    import cats._
    import cats.implicits._

    override def serialize[DataType](t: TierRecipe)(ops: DynamicOps[DataType]): datafixers.Dynamic[DataType] = {
      val map = ops.emptyMap().pure[Id]
        .map(d => ops.set(d, TierRecipe.KEY_TIER, t.getTier.serialize(ops).getValue))
        .map(d => if (t.getTier.hasTagRecipe) ops.set(d, TierRecipe.KEY_SUB_ITEM, t.getSubItems.serialize(ops).getValue) else d)

      new datafixers.Dynamic[DataType](ops, map)
    }

    override def deserialize[DataType](d: datafixers.Dynamic[DataType]): TierRecipe = {
      val recipeId = d.get(TierRecipe.KEY_ID).asString().asScala.map(new ResourceLocation(_)).get
      val tiers = d.get(TierRecipe.KEY_TIER).get().asScala
        .map(DynamicSerializable[Tiers].deserialize)
        .getOrElse(Tiers.Invalid)
      val subItem = d.get(TierRecipe.KEY_SUB_ITEM).get().asScala
        .map(DynamicSerializable[Ingredient].deserialize)
        .getOrElse(Ingredient.EMPTY)
      if (subItem == Ingredient.EMPTY)
        LOGGER.warn("Empty ingredient was loaded for {}, data: {}", recipeId, d.getValue)
      LOGGER.debug("Serializer loaded {} from data for tier {}.", recipeId, tiers)
      new TierRecipe(recipeId, tiers, subItem)
    }
  }

  implicit val IngredientSerialize: DynamicSerializable[Ingredient] = new DynamicSerializable[Ingredient] {
    override def serialize[DataType](t: Ingredient)(ops: DynamicOps[DataType]): datafixers.Dynamic[DataType] =
      new datafixers.Dynamic(JsonOps.INSTANCE, t.serialize()).convert(ops)

    override def deserialize[DataType](d: datafixers.Dynamic[DataType]): Ingredient =
      Ingredient.deserialize(d.convert(JsonOps.INSTANCE).getValue)
  }
}
