package com.kotori316.fluidtank.recipes

import java.io.IOException

import com.google.gson.{GsonBuilder, JsonArray, JsonObject}
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import net.minecraft.advancements.criterion.{FilledBucketTrigger, InventoryChangeTrigger, ItemPredicate, RecipeUnlockedTrigger}
import net.minecraft.advancements.{Advancement, AdvancementRewards, ICriterionInstance, IRequirementsStrategy}
import net.minecraft.data.{DataGenerator, DirectoryCache, IDataProvider}
import net.minecraft.item.{Item, Items}
import net.minecraft.tags.Tag
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.Tags
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition, TagEmptyCondition}
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent

case class RecipeAdvancements(location: ResourceLocation,
                              criterionList: List[(String, ICriterionInstance)] = Nil,
                              conditions: List[ICondition] = Nil) {

  def addCriterion(name: String, criterion: ICriterionInstance): RecipeAdvancements =
    copy(criterionList = (name, criterion) :: criterionList)

  def addItemCriterion(item: Item): RecipeAdvancements =
    addCriterion(s"has_${item.getRegistryName.getPath}", InventoryChangeTrigger.Instance.forItems(item))

  def addItemCriterion(tag: Tag[Item]): RecipeAdvancements =
    addCriterion(s"has_${tag.getId.getPath}", InventoryChangeTrigger.Instance.forItems(ItemPredicate.Builder.create().tag(tag).build())).
      addCondition(new NotCondition(new TagEmptyCondition(tag.getId)))

  def addCondition(condition: ICondition): RecipeAdvancements =
    copy(conditions = condition :: conditions)

  def build: JsonObject = {
    val builder = Advancement.Builder.builder()
    builder.withParentId(new ResourceLocation("recipes/root"))
      .withCriterion("has_the_recipe", new RecipeUnlockedTrigger.Instance(location))
      .withRewards(AdvancementRewards.Builder.recipe(location))
      .withRequirementsStrategy(IRequirementsStrategy.OR)
    val obj = criterionList.foldLeft(builder) { case (b, (s, c)) => b.withCriterion(s, c) }
      .serialize()
    obj.add("conditions", conditions.foldLeft(new JsonArray) { case (a, c) => a.add(CraftingHelper.serialize(c)); a })
    obj
  }
}

object RecipeAdvancements {
  def gatherData(event: GatherDataEvent): Unit = {
    if (event.includeServer()) {
      event.getGenerator.addProvider(new RecipeProvider(event.getGenerator))
    }
  }

  class RecipeProvider(generatorIn: DataGenerator) extends IDataProvider {
    override def act(cache: DirectoryCache): Unit = {
      val path = generatorIn.getOutputFolder
      val GSON = (new GsonBuilder).setPrettyPrinting().create
      val ID = (s: String) => new ResourceLocation(FluidTank.modID, s)

      val TANK_WOOD = RecipeAdvancements(ID("tank_wood"))
        .addCriterion("has_bucket", FilledBucketTrigger.Instance.forItem(ItemPredicate.Builder.create().item(Items.WATER_BUCKET).build()))
        .addItemCriterion(Tags.Items.GLASS)
      val TANK_WOOD_EASY = RecipeAdvancements(ID("tank_wood_easy"))
        .addCriterion("has_bucket", FilledBucketTrigger.Instance.forItem(ItemPredicate.Builder.create().item(Items.WATER_BUCKET).build()))
        .addItemCriterion(Tags.Items.GLASS)
      val TANKS = ModObjects.blockTanks.collect { case b if b.tier.hasOreRecipe => b.tier }
        .map(tier => RecipeAdvancements(ID("tank_" + tier.toString.toLowerCase)).addItemCriterion(new Tag[Item](new ResourceLocation(tier.oreName))))

      val recipeAdvancements = TANK_WOOD :: TANK_WOOD_EASY :: TANKS

      for (recipe <- recipeAdvancements) {
        val out = path.resolve(s"data/${recipe.location.getNamespace}/advancements/${recipe.location.getPath}.json")
        try {
          IDataProvider.save(GSON, cache, recipe.build, out)
        } catch {
          case e: IOException => FluidTank.LOGGER.error(s"Failed to save recipe ${recipe.location}.", e)
        }
      }
    }

    override def getName = "Recipe of FluidTank"
  }

}
