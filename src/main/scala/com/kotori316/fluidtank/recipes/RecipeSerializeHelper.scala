package com.kotori316.fluidtank.recipes

import com.google.gson.JsonObject
import com.kotori316.fluidtank.DynamicSerializable._
import com.kotori316.fluidtank._
import com.kotori316.fluidtank.tiles.Tiers
import com.mojang.serialization.{DynamicOps, JsonOps, Dynamic => SerializeDynamic}
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger
import net.minecraft.data.{IFinishedRecipe, ShapedRecipeBuilder}
import net.minecraft.item.crafting.Ingredient
import net.minecraft.tags.ITag
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.conditions.ICondition

import scala.util.chaining._

case class RecipeSerializeHelper(recipe: IFinishedRecipe,
                                 conditions: List[ICondition] = Nil,
                                 saveName: ResourceLocation = null) {
  def this(c: ShapedRecipeBuilder, saveName: ResourceLocation) = {
    this(RecipeSerializeHelper.getConsumeValue(c), saveName = saveName)
  }

  def addCondition(condition: ICondition): RecipeSerializeHelper =
    copy(conditions = condition :: this.conditions)

  def addTagCondition(tag: ITag.INamedTag[_]): RecipeSerializeHelper =
    addCondition(new TagCondition(tag.func_230234_a_()))

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

  private def getConsumeValue(c: ShapedRecipeBuilder): IFinishedRecipe = {
    c.addCriterion("dummy", RecipeUnlockedTrigger.func_235675_a_(new ResourceLocation("dummy:dummy")))
    var t: IFinishedRecipe = null
    c.build(p => t = p)
    t
  }

  val TierRecipeSerializer: DynamicSerializable[TierRecipe] = TierRecipeSerializerObj

  private object TierRecipeSerializerObj extends DynamicSerializable[TierRecipe] {
    private[this] final val LOGGER = org.apache.logging.log4j.LogManager.getLogger(classOf[TierRecipe])

    import scala.jdk.OptionConverters._

    override def serialize[DataType](t: TierRecipe)(ops: DynamicOps[DataType]): SerializeDynamic[DataType] = {
      val map = ops.emptyMap()
        .pipe(d => ops.set(d, TierRecipe.KEY_TIER, t.getTier.serialize(ops).getValue))
        .pipe(d => if (t.getTier.hasTagRecipe) ops.set(d, TierRecipe.KEY_SUB_ITEM, t.getSubItems.serialize(ops).getValue) else d)

      new SerializeDynamic[DataType](ops, map)
    }

    override def deserialize[DataType](d: SerializeDynamic[DataType]): TierRecipe = {
      val recipeId = d.get(TierRecipe.KEY_ID).asString().result().toScala.map(new ResourceLocation(_)).get
      val tiers = d.get(TierRecipe.KEY_TIER).get().result().toScala
        .map(DynamicSerializable[Tiers].deserialize)
        .getOrElse(Tiers.Invalid)
      val subItem = d.get(TierRecipe.KEY_SUB_ITEM).get().result().toScala
        .map(DynamicSerializable[Ingredient].deserialize)
        .getOrElse(Ingredient.EMPTY)
      if (subItem == Ingredient.EMPTY)
        LOGGER.warn("Empty ingredient was loaded for {}, data: {}", recipeId, d.getValue)
      LOGGER.debug("Serializer loaded {} from data for tier {}.", recipeId, tiers)
      new TierRecipe(recipeId, tiers, subItem)
    }
  }

  implicit val IngredientSerialize: DynamicSerializable[Ingredient] = IngredientSerializeObj

  private object IngredientSerializeObj extends DynamicSerializable[Ingredient] {
    override def serialize[DataType](t: Ingredient)(ops: DynamicOps[DataType]): SerializeDynamic[DataType] =
      new SerializeDynamic(JsonOps.INSTANCE, t.serialize()).convert(ops)

    override def deserialize[DataType](d: SerializeDynamic[DataType]): Ingredient =
      Ingredient.deserialize(d.convert(JsonOps.INSTANCE).getValue)
  }

}
