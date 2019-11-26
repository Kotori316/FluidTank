package com.kotori316.fluidtank.recipes

import java.io.IOException

import cats.syntax.eq._
import com.google.gson.{GsonBuilder, JsonArray}
import com.kotori316.fluidtank.tiles.Tiers
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import net.minecraft.advancements.criterion._
import net.minecraft.block.Blocks
import net.minecraft.data._
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.{Item, Items}
import net.minecraft.tags.{ItemTags, Tag}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.Tags
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition, TagEmptyCondition}
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent
import net.minecraftforge.registries.ForgeRegistries

object FluidTankDataProvider {
  def gatherData(event: GatherDataEvent): Unit = {
    if (event.includeServer()) {
      event.getGenerator.addProvider(new AdvancementProvider(event.getGenerator))
      event.getGenerator.addProvider(new RecipeProvider(event.getGenerator))
    }
  }

  private[this] final val ID = (s: String) => new ResourceLocation(FluidTank.modID, s)

  class AdvancementProvider(generatorIn: DataGenerator) extends IDataProvider {
    override def act(cache: DirectoryCache): Unit = {
      val path = generatorIn.getOutputFolder
      val GSON = (new GsonBuilder).setPrettyPrinting().create

      val woodLocation = ID("tank_wood")
      val TANK_WOOD = AdvancementSerializeHelper(woodLocation)
        .addCriterion("has_bucket", FilledBucketTrigger.Instance.forItem(ItemPredicate.Builder.create().item(Items.WATER_BUCKET).build()))
        .addItemCriterion(Tags.Items.GLASS)
      val TANK_WOOD_EASY = AdvancementSerializeHelper(ID("tank_wood_easy"))
        .addCriterion("has_bucket", FilledBucketTrigger.Instance.forItem(ItemPredicate.Builder.create().item(Items.WATER_BUCKET).build()))
        .addItemCriterion(Tags.Items.GLASS)
      val TANKS = ModObjects.blockTanks.collect { case b if b.tier.hasOreRecipe => b.tier }
        .map(tier => AdvancementSerializeHelper(ID("tank_" + tier.toString.toLowerCase)).addItemCriterion(new Tag[Item](new ResourceLocation(tier.oreName))))
      val CAT = AdvancementSerializeHelper(ID("chest_as_tank"))
        .addItemCriterion(ForgeRegistries.ITEMS.getValue(woodLocation))
        .addCriterion("has_lots_of_items", new InventoryChangeTrigger.Instance(MinMaxBounds.IntBound.atLeast(10),
          MinMaxBounds.IntBound.UNBOUNDED, MinMaxBounds.IntBound.UNBOUNDED, Array(ItemPredicate.Builder.create().item(Items.WATER_BUCKET).build())))
      val PIPE = AdvancementSerializeHelper(ID("pipe"))
        .addCriterion("has_pearl", InventoryChangeTrigger.Instance.forItems(
          ItemPredicate.Builder.create().item(Items.ENDER_EYE).build(),
          ItemPredicate.Builder.create().tag(Tags.Items.ENDER_PEARLS).build()))
        .addCondition(new NotCondition(new TagEmptyCondition(Tags.Items.ENDER_PEARLS.getId)))
        .addItemCriterion(ForgeRegistries.ITEMS.getValue(woodLocation))
      val recipeAdvancements = PIPE :: CAT :: TANK_WOOD :: TANK_WOOD_EASY :: TANKS

      for (advancement <- recipeAdvancements) {
        val out = path.resolve(s"data/${advancement.location.getNamespace}/advancements/${advancement.location.getPath}.json")
        try {
          IDataProvider.save(GSON, cache, advancement.build, out)
        } catch {
          case e: IOException => FluidTank.LOGGER.error(s"Failed to save advancement ${advancement.location}.", e)
        }
      }
    }

    override def getName = "Advancement of FluidTank"
  }

  class RecipeProvider(generatorIn: DataGenerator) extends IDataProvider {
    override def act(cache: DirectoryCache): Unit = {
      val path = generatorIn.getOutputFolder
      val GSON = (new GsonBuilder).setPrettyPrinting().create

      val tankWoodItem = ForgeRegistries.ITEMS.getValue(ID("tank_wood"))
      val woodTanks = Ingredient.fromItems(ModObjects.blockTanks.filter(_.tier === Tiers.WOOD) ::: ModObjects.blockTanksInvisible.filter(_.tier === Tiers.WOOD): _*)
      val WOOD = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(tankWoodItem)
          .key('x', Tags.Items.GLASS).key('p', ItemTags.LOGS)
          .patternLine("x x")
          .patternLine("xpx")
          .patternLine("xxx"))
        .addCondition(ConfigCondition.getInstance())
        .addCondition(new NotCondition(EasyCondition.getInstance()))
      val EASY_WOOD = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(tankWoodItem)
          .key('x', Tags.Items.GLASS).key('p', ItemTags.PLANKS)
          .patternLine("p p")
          .patternLine("p p")
          .patternLine("xpx"), saveName = ID("tank_wood_easy"))
        .addCondition(ConfigCondition.getInstance())
        .addCondition(EasyCondition.getInstance())
      val CAT = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(ModObjects.blockCat)
          .key('x', woodTanks)
          .key('p', ingredientArray(Ingredient.fromTag(Tags.Items.CHESTS), Ingredient.fromItems(Blocks.BARREL)))
          .patternLine("x x")
          .patternLine("xpx")
          .patternLine("xxx"))
      val PIPE = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(ModObjects.blockPipe)
          .key('t', woodTanks)
          .key('g', Tags.Items.GLASS)
          .key('e', ingredientArray(Ingredient.fromTag(Tags.Items.ENDER_PEARLS), Ingredient.fromItems(Items.ENDER_EYE)))
          .patternLine("gtg")
          .patternLine(" e ")
          .patternLine("gtg"))
      val TANKS = ModObjects.blockTanks.collect { case b if b.tier.hasOreRecipe => b.tier }
        .map(tier => RecipeSerializeHelper(new TierRecipe.FinishedRecipe(ID("tank_" + tier.toString.toLowerCase), tier))
          .addTagCondition(new Tag[Item](new ResourceLocation(tier.oreName)))
          .addCondition(ConfigCondition.getInstance()))

      val recipes = PIPE :: CAT :: WOOD :: EASY_WOOD :: TANKS

      for (recipe <- recipes) {
        val out = path.resolve(s"data/${recipe.location.getNamespace}/recipes/${recipe.location.getPath}.json")
        try {
          IDataProvider.save(GSON, cache, recipe.build, out)
        } catch {
          case e: IOException => FluidTank.LOGGER.error(s"Failed to save recipe ${recipe.location}.", e)
        }
      }
    }

    override def getName = "Recipe of FluidTank"

  }

  def makeConditionArray(conditions: List[ICondition]): JsonArray = {
    conditions.foldLeft(new JsonArray) { case (a, c) => a.add(CraftingHelper.serialize(c)); a }
  }

  def ingredientArray(i1: Ingredient, is: Ingredient*): Ingredient = {
    Ingredient.merge(java.util.Arrays.asList(i1 +: is: _*))
  }
}
