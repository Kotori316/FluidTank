package com.kotori316.fluidtank.recipes

import java.io.IOException

import cats.syntax.eq._
import com.google.gson.{GsonBuilder, JsonArray}
import com.kotori316.fluidtank._
import com.kotori316.fluidtank.recipes.ReservoirRecipe.ReservoirFinishedRecipe
import com.kotori316.fluidtank.recipes.TierRecipe.TierFinishedRecipe
import com.kotori316.fluidtank.tiles.Tier
import net.minecraft.advancements.critereon.{EntityPredicate, FilledBucketTrigger, InventoryChangeTrigger, ItemPredicate, MinMaxBounds}
import net.minecraft.core.Registry
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.data.{DataGenerator, DataProvider, HashCache}
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.{ItemTags, TagKey}
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.{Item, Items}
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.common.Tags
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition}
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.MarkerManager

import scala.collection.mutable
import scala.jdk.javaapi.CollectionConverters

object FluidTankDataProvider {
  private final val MARKER = MarkerManager.getMarker("FluidTankDataProvider")

  def gatherData(event: GatherDataEvent): Unit = {
    if (event.includeServer()) {
      event.getGenerator.addProvider(new AdvancementProvider(event.getGenerator))
      event.getGenerator.addProvider(new RecipeProvider(event.getGenerator))
    }
    if (event.includeClient()) {
      event.getGenerator.addProvider(new ModelProvider(event.getGenerator))
    }
  }

  private[this] final def ID(s: String) = new ResourceLocation(FluidTank.modID, s)

  private def tag(name: ResourceLocation): TagKey[Item] = TagKey.create(Registry.ITEM_REGISTRY, name)

  class AdvancementProvider(generatorIn: DataGenerator) extends DataProvider {
    override def run(cache: HashCache): Unit = {
      val path = generatorIn.getOutputFolder
      val GSON = (new GsonBuilder).setPrettyPrinting().create

      val easyCondition = new FluidTankConditions.EasyCondition()
      val woodLocation = ID("tank_wood")
      val TANK_WOOD = AdvancementSerializeHelper(woodLocation)
        .addCriterion("has_bucket", FilledBucketTrigger.TriggerInstance.filledBucket(ItemPredicate.Builder.item().of(Items.WATER_BUCKET).build()))
        .addItemCriterion(Tags.Items.GLASS)
      val TANK_WOOD_EASY = TANK_WOOD.copy(location = ID("tank_wood_easy"))
      val TANKS = ModObjects.blockTanks.collect { case b if b.tier.hasTagRecipe => b.tier }
        .map(tier => AdvancementSerializeHelper(ID("tank_" + tier.toString.toLowerCase)).addItemCriterion(tag(new ResourceLocation(tier.tagName))))
      val VOID_TANK = AdvancementSerializeHelper(ID("tank_void"))
        .addItemCriterion(ForgeRegistries.ITEMS.getValue(woodLocation))
      val CAT = AdvancementSerializeHelper(ID("chest_as_tank"))
        .addItemCriterion(ForgeRegistries.ITEMS.getValue(woodLocation))
        .addCriterion("has_lots_of_items", new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.atLeast(10),
          MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Array(ItemPredicate.Builder.item().of(Items.WATER_BUCKET).build())))
      val PIPE = AdvancementSerializeHelper(ID("pipe"))
        .addCriterion("has_pearl", InventoryChangeTrigger.TriggerInstance.hasItems(
          ItemPredicate.Builder.item().of(Items.ENDER_EYE).build(),
          ItemPredicate.Builder.item().of(Tags.Items.ENDER_PEARLS).build()))
        .addCondition(new NotCondition(easyCondition))
        .addItemCriterion(ForgeRegistries.ITEMS.getValue(woodLocation))
      val PIPE_EASY = AdvancementSerializeHelper(ID("pipe_easy"))
        .addCondition(easyCondition)
        .addItemCriterion(ForgeRegistries.ITEMS.getValue(woodLocation))
      val ITEM_PIPE = PIPE.copy(location = ID("item_pipe"))
      val ITEM_PIPE_EASY = PIPE_EASY.copy(location = ID("item_pipe_easy"))
      val FLUID_SOURCE = AdvancementSerializeHelper(ID("fluid_source"))
        .addItemCriterion(Items.WATER_BUCKET)
      val recipeAdvancements = FLUID_SOURCE :: PIPE :: PIPE_EASY :: ITEM_PIPE :: ITEM_PIPE_EASY ::
        CAT :: TANK_WOOD :: TANK_WOOD_EASY :: VOID_TANK :: TANKS

      for (advancement <- recipeAdvancements) {
        val out = path.resolve(s"data/${advancement.location.getNamespace}/advancements/recipes/tank/${advancement.location.getPath}.json")
        try {
          DataProvider.save(GSON, cache, advancement.build, out)
        } catch {
          case e: IOException => FluidTank.LOGGER.error(MARKER, s"Failed to save advancement ${advancement.location}.", e)
        }
      }
    }

    override def getName = "Advancement of FluidTank"
  }

  class RecipeProvider(generatorIn: DataGenerator) extends DataProvider {
    override def run(cache: HashCache): Unit = {
      val path = generatorIn.getOutputFolder
      val GSON = (new GsonBuilder).setPrettyPrinting().create

      val tankWoodItem = ForgeRegistries.ITEMS.getValue(ID("tank_wood"))
      val woodTanks = Ingredient.of(ModObjects.blockTanks.filter(_.tier === Tier.WOOD): _*)
      val configCondition = new FluidTankConditions.ConfigCondition()
      val easyCondition = new FluidTankConditions.EasyCondition()
      val WOOD = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(tankWoodItem)
          .define('x', Tags.Items.GLASS).define('p', ItemTags.LOGS)
          .pattern("x x")
          .pattern("xpx")
          .pattern("xxx"))
        .addCondition(configCondition)
        .addCondition(new NotCondition(easyCondition))
        .addCondition(new TagCondition(Tags.Items.GLASS.location))
      val EASY_WOOD = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(tankWoodItem)
          .define('x', Tags.Items.GLASS).define('p', ItemTags.PLANKS)
          .pattern("p p")
          .pattern("p p")
          .pattern("xpx"), saveName = ID("tank_wood_easy"))
        .addCondition(configCondition)
        .addCondition(easyCondition)
        .addCondition(new TagCondition(Tags.Items.GLASS.location))
      val VOID = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(ForgeRegistries.ITEMS.getValue(ID("tank_void")))
          .define('o', Tags.Items.OBSIDIAN).define('t', woodTanks)
          .pattern("ooo")
          .pattern("oto")
          .pattern("ooo"))
        .addCondition(configCondition)
      val CAT = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(ModObjects.blockCat)
          .define('x', woodTanks)
          .define('p', ingredientArray(Ingredient.of(Tags.Items.CHESTS), Ingredient.of(Blocks.BARREL)))
          .pattern("x x")
          .pattern("xpx")
          .pattern("xxx"))
      val PIPE = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(ModObjects.blockFluidPipe)
          .define('t', woodTanks)
          .define('g', Tags.Items.GLASS)
          .define('e', ingredientArray(Ingredient.of(Tags.Items.ENDER_PEARLS), Ingredient.of(Items.ENDER_EYE)))
          .pattern("gtg")
          .pattern(" e ")
          .pattern("gtg"))
        .addCondition(new NotCondition(easyCondition))
      val PIPE_EASY = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(ModObjects.blockFluidPipe, 2)
          .define('t', woodTanks)
          .define('g', Tags.Items.GLASS)
          .pattern("gtg")
          .pattern("   ")
          .pattern("gtg"), saveName = ID("pipe_easy"))
        .addCondition(easyCondition)
      val ITEM_PIPE = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(ModObjects.blockItemPipe, 4)
          .define('t', woodTanks)
          .define('g', Tags.Items.GLASS)
          .define('e', ingredientArray(Ingredient.of(Tags.Items.ENDER_PEARLS), Ingredient.of(Items.ENDER_EYE)))
          .pattern("g g")
          .pattern("tet")
          .pattern("g g"))
        .addCondition(new NotCondition(easyCondition))
      val ITEM_PIPE_EASY = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(ModObjects.blockItemPipe, 8)
          .define('t', woodTanks)
          .define('g', Tags.Items.GLASS)
          .pattern("g g")
          .pattern("t t")
          .pattern("g g"), saveName = ID("item_pipe_easy"))
        .addCondition(easyCondition)
      val TANKS = ModObjects.blockTanks.collect { case b if b.tier.hasTagRecipe => b.tier }
        .map(tier => RecipeSerializeHelper(new TierFinishedRecipe(ID("tank_" + tier.toString.toLowerCase), tier))
          .addTagCondition(tag(new ResourceLocation(tier.tagName)))
          .addCondition(configCondition))
      val FLUID_SOURCE = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(ModObjects.blockSource)
          .pattern("wiw")
          .pattern("gIg")
          .pattern("wdw")
          .define('w', Items.WATER_BUCKET)
          .define('i', Tags.Items.INGOTS_IRON)
          .define('g', Tags.Items.INGOTS_GOLD)
          .define('I', Tags.Items.STORAGE_BLOCKS_IRON)
          .define('d', Blocks.DIRT))
      val COMBINE = RecipeSerializeHelper.bySpecial(CombineRecipe.SERIALIZER, CombineRecipe.LOCATION)
        .addCondition(configCondition)
      val RESERVOIRS = List(Tier.WOOD, Tier.STONE, Tier.IRON)
        .map(t => new ReservoirRecipe(ID("reservoir_" + t.lowerName), t))
        .map(r => new ReservoirFinishedRecipe(r))
        .map(r => RecipeSerializeHelper(r))
      val COPPER = RecipeSerializeHelper(new TierFinishedRecipe(ID("tank_copper_vanilla"), Tier.COPPER, Ingredient.of(Items.COPPER_INGOT)))
        .addCondition(configCondition)

      val recipes = RESERVOIRS ::: COMBINE :: FLUID_SOURCE :: PIPE :: PIPE_EASY :: ITEM_PIPE :: ITEM_PIPE_EASY :: CAT :: WOOD :: EASY_WOOD :: VOID :: COPPER :: TANKS

      for (recipe <- recipes) {
        val out = path.resolve(s"data/${recipe.location.getNamespace}/recipes/${recipe.location.getPath}.json")
        try {
          DataProvider.save(GSON, cache, recipe.build, out)
        } catch {
          case e: IOException => FluidTank.LOGGER.error(MARKER, s"Failed to save recipe ${recipe.location}.", e)
          case e: NullPointerException => FluidTank.LOGGER.error(MARKER, s"Failed to save recipe ${recipe.location}. Check the serializer registered.", e)
        }
      }
    }

    override def getName = "Recipe of FluidTank"

  }

  class ModelProvider(generatorIn: DataGenerator) extends DataProvider {
    //noinspection SpellCheckingInspection
    override def run(cache: HashCache): Unit = {
      val path = generatorIn.getOutputFolder
      val GSON = (new GsonBuilder).setPrettyPrinting().disableHtmlEscaping().create
      val models: mutable.Buffer[ModelSerializerHelper] = mutable.Buffer.empty
      models ++= ModObjects.blockTanks.map(ModelSerializerHelper.getTankModel)
      models += ModelSerializerHelper.getFluidSourceModel(ModObjects.blockSource)
      models += ModelSerializerHelper.getCatModel(ModObjects.blockCat)

      for (model <- models) {
        val out = path.resolve(s"assets/${model.location.getNamespace}/blockstates/${model.location.getPath}.json")
        try {
          DataProvider.save(GSON, cache, model.build, out)
        } catch {
          case e: IOException => FluidTank.LOGGER.error(MARKER, s"Failed to save model ${model.location}.", e)
          case e: NullPointerException => FluidTank.LOGGER.error(MARKER, s"Failed to save model ${model.location}. Check the serializer registered.", e)
        }
      }
    }

    override def getName: String = "Models of FluidTank"
  }

  def makeConditionArray(conditions: List[ICondition]): JsonArray = {
    conditions.foldLeft(new JsonArray) { case (a, c) => a.add(CraftingHelper.serialize(c)); a }
  }

  def ingredientArray(i1: Ingredient, is: Ingredient*): Ingredient = {
    Ingredient.merge(CollectionConverters.asJava(i1 +: is))
  }
}
