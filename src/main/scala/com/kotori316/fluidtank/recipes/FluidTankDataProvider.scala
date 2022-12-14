package com.kotori316.fluidtank.recipes

import java.util.Collections
import java.util.concurrent.CompletableFuture

import com.google.gson.JsonArray
import com.kotori316.fluidtank._
import com.kotori316.fluidtank.blocks.{BlockTank, ContentLootFunction}
import com.kotori316.fluidtank.integration.mekanism_gas.{BlockGasTank, GasContentLootFunction}
import com.kotori316.fluidtank.recipes.FluidTankConditions.{PipeConfigCondition, TankConfigCondition}
import com.kotori316.fluidtank.recipes.ReservoirRecipe.ReservoirFinishedRecipe
import com.kotori316.fluidtank.recipes.TierRecipe.TierFinishedRecipe
import com.kotori316.fluidtank.tiles.Tier
import net.minecraft.advancements.critereon.{EntityPredicate, FilledBucketTrigger, InventoryChangeTrigger, ItemPredicate, MinMaxBounds}
import net.minecraft.core.registries.Registries
import net.minecraft.data.loot.{BlockLootSubProvider, LootTableProvider}
import net.minecraft.data.recipes.{RecipeCategory, ShapedRecipeBuilder}
import net.minecraft.data.{CachedOutput, DataGenerator, DataProvider}
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.{ItemTags, TagKey}
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.{Item, Items}
import net.minecraft.world.level.block.{Block, Blocks}
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraftforge.common.Tags
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.{ICondition, ModLoadedCondition, NotCondition, TagEmptyCondition}
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.MarkerManager

import scala.jdk.javaapi.CollectionConverters

object FluidTankDataProvider {
  private final val MARKER = MarkerManager.getMarker("FluidTankDataProvider")

  def gatherData(event: GatherDataEvent): Unit = {
    event.getGenerator.addProvider(event.includeServer, new AdvancementProvider(event.getGenerator))
    event.getGenerator.addProvider(event.includeServer, new RecipeProvider(event.getGenerator))
    event.getGenerator.addProvider(event.includeClient, new StateAndModelProvider(event.getGenerator, event.getExistingFileHelper))
    event.getGenerator.addProvider(event.includeServer, new LootTableProvider(event.getGenerator.getPackOutput, Collections.emptySet(),
      CollectionConverters.asJava(Seq(new LootTableProvider.SubProviderEntry(() => new LootSubProvider, LootContextParamSets.BLOCK)))
    ))
  }

  private[this] final def ID(s: String) = new ResourceLocation(FluidTank.modID, s)

  private def tag(name: ResourceLocation): TagKey[Item] = TagKey.create(Registries.ITEM, name)

  class AdvancementProvider(generatorIn: DataGenerator) extends DataProvider {
    override def run(cache: CachedOutput): CompletableFuture[_] = {
      val path = generatorIn.getPackOutput.getOutputFolder

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

      val outputWork = for (advancement <- recipeAdvancements) yield {
        val out = path.resolve(s"data/${advancement.location.getNamespace}/advancements/recipes/tank/${advancement.location.getPath}.json")
        DataProvider.saveStable(cache, advancement.build, out)
      }
      CompletableFuture.allOf(outputWork: _*)
    }

    override def getName = "Advancement of FluidTank"
  }

  class RecipeProvider(generatorIn: DataGenerator) extends DataProvider {
    override def run(cache: CachedOutput): CompletableFuture[_] = {
      val path = generatorIn.getPackOutput.getOutputFolder

      val tankWoodItem = ForgeRegistries.ITEMS.getValue(ID("tank_wood"))
      val woodTanks = Ingredient.of(ModObjects.tierToBlock(Tier.WOOD))
      val tankConfigCondition = new TankConfigCondition()
      val pipeConfigCondition = new PipeConfigCondition()
      val easyCondition = new FluidTankConditions.EasyCondition()
      val WOOD = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, tankWoodItem)
          .define('x', Tags.Items.GLASS).define('p', ItemTags.LOGS)
          .pattern("x x")
          .pattern("xpx")
          .pattern("xxx"))
        .addCondition(tankConfigCondition)
        .addCondition(new NotCondition(easyCondition))
        .addCondition(new TagCondition(Tags.Items.GLASS.location))
      val EASY_WOOD = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, tankWoodItem)
          .define('x', Tags.Items.GLASS).define('p', ItemTags.PLANKS)
          .pattern("p p")
          .pattern("p p")
          .pattern("xpx"), saveName = ID("tank_wood_easy"))
        .addCondition(tankConfigCondition)
        .addCondition(easyCondition)
        .addCondition(new TagCondition(Tags.Items.GLASS.location))
      val VOID = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModObjects.tierToBlock(Tier.VOID))
          .define('o', Tags.Items.OBSIDIAN).define('t', woodTanks)
          .pattern("ooo")
          .pattern("oto")
          .pattern("ooo"))
        .addCondition(tankConfigCondition)
      val CAT = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModObjects.blockCat)
          .define('x', woodTanks)
          .define('p', ingredientArray(Ingredient.of(Tags.Items.CHESTS), Ingredient.of(Blocks.BARREL)))
          .pattern("x x")
          .pattern("xpx")
          .pattern("xxx"))
      val PIPE = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModObjects.blockFluidPipe)
          .define('t', woodTanks)
          .define('g', Tags.Items.GLASS)
          .define('e', ingredientArray(Ingredient.of(Tags.Items.ENDER_PEARLS), Ingredient.of(Items.ENDER_EYE)))
          .pattern("gtg")
          .pattern(" e ")
          .pattern("gtg"))
        .addCondition(new NotCondition(easyCondition))
        .addCondition(pipeConfigCondition)
      val PIPE_EASY = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModObjects.blockFluidPipe, 2)
          .define('t', woodTanks)
          .define('g', Tags.Items.GLASS)
          .pattern("gtg")
          .pattern("   ")
          .pattern("gtg"), saveName = ID("pipe_easy"))
        .addCondition(easyCondition)
        .addCondition(pipeConfigCondition)
      val ITEM_PIPE = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModObjects.blockItemPipe, 4)
          .define('t', woodTanks)
          .define('g', Tags.Items.GLASS)
          .define('e', ingredientArray(Ingredient.of(Tags.Items.ENDER_PEARLS), Ingredient.of(Items.ENDER_EYE)))
          .pattern("g g")
          .pattern("tet")
          .pattern("g g"))
        .addCondition(new NotCondition(easyCondition))
        .addCondition(pipeConfigCondition)
      val ITEM_PIPE_EASY = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModObjects.blockItemPipe, 8)
          .define('t', woodTanks)
          .define('g', Tags.Items.GLASS)
          .pattern("g g")
          .pattern("t t")
          .pattern("g g"), saveName = ID("item_pipe_easy"))
        .addCondition(easyCondition)
        .addCondition(pipeConfigCondition)
      val TANKS = ModObjects.blockTanks.collect { case b if b.tier.hasTagRecipe => b.tier }
        .map(tier => RecipeSerializeHelper(new TierFinishedRecipe(ID("tank_" + tier.toString.toLowerCase), tier))
          .addTagCondition(tag(new ResourceLocation(tier.tagName)))
          .addCondition(tankConfigCondition))
      val FLUID_SOURCE = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModObjects.blockSource)
          .pattern("wiw")
          .pattern("gIg")
          .pattern("wdw")
          .define('w', Items.WATER_BUCKET)
          .define('i', Tags.Items.INGOTS_IRON)
          .define('g', Tags.Items.INGOTS_GOLD)
          .define('I', Tags.Items.STORAGE_BLOCKS_IRON)
          .define('d', Blocks.DIRT))
      val COMBINE = RecipeSerializeHelper(new CombineRecipe.CombineFinishedRecipe(new ResourceLocation(CombineRecipe.LOCATION)))
        .addCondition(tankConfigCondition)
      val RESERVOIRS = List(Tier.WOOD, Tier.STONE, Tier.IRON)
        .map(t => new ReservoirRecipe(ID("reservoir_" + t.lowerName), t))
        .map(r => new ReservoirFinishedRecipe(r))
        .map(r => RecipeSerializeHelper(r))
      val COPPER = RecipeSerializeHelper(new TierFinishedRecipe(ID("tank_copper_vanilla"), Tier.COPPER, Ingredient.of(Items.COPPER_INGOT)))
        .addCondition(tankConfigCondition)
        .addCondition(new TagEmptyCondition("forge", "ingots/copper"))
      val GAS_TANKS = (ModObjects.gasTanks zip
        (ModObjects.tierToBlock(Tier.WOOD) :: ModObjects.gasTanks) zip
        Seq(ItemTags.LOGS, Tags.Items.INGOTS_IRON, Tags.Items.INGOTS_GOLD, Tags.Items.GEMS_DIAMOND, Tags.Items.GEMS_EMERALD)
        ).map { case ((tank, preTank), tag) => RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, tank)
          .pattern("ptp")
          .pattern("tot")
          .pattern("ptp")
          .define('p', preTank)
          .define('t', tag)
          .define('o', ItemTags.create(new ResourceLocation("forge", "ingots/osmium")))
      )
        .addCondition(tankConfigCondition)
        .addTagCondition(tag)
        .addTagCondition(ItemTags.create(new ResourceLocation("forge", "ingots/osmium")))
        .addCondition(new ModLoadedCondition("mekanism"))
      }

      val recipes = RESERVOIRS ::: COMBINE :: FLUID_SOURCE :: PIPE :: PIPE_EASY :: ITEM_PIPE :: ITEM_PIPE_EASY :: CAT :: WOOD :: EASY_WOOD :: VOID :: COPPER :: TANKS ::: GAS_TANKS

      val outputWork = for (recipe <- recipes) yield {
        val out = path.resolve(s"data/${recipe.location.getNamespace}/recipes/${recipe.location.getPath}.json")
        DataProvider.saveStable(cache, recipe.build, out)
      }
      CompletableFuture.allOf(outputWork: _*)
    }

    override def getName = "Recipe of FluidTank"

  }

  final class LootSubProvider extends BlockLootSubProvider(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags()) {
    override def generate(): Unit = {
      ModObjects.blockTanks.foreach { b =>
        this.add(b, tankContent(b))
      }
      Seq(ModObjects.blockSource, ModObjects.blockCat, ModObjects.blockItemPipe, ModObjects.blockFluidPipe).foreach { b =>
        this.dropSelf(b)
      }
      ModObjects.gasTanks.foreach { b =>
        this.add(b, gasTankContent(b))
      }
    }

    override def getKnownBlocks: java.lang.Iterable[Block] = {
      CollectionConverters.asJava(
        ModObjects.blockTanks ++
          Seq(ModObjects.blockSource, ModObjects.blockCat, ModObjects.blockItemPipe, ModObjects.blockFluidPipe) ++
          ModObjects.gasTanks
      )
    }

    private def tankContent(tankBlock: BlockTank): LootTable.Builder = createSingleItemTable(tankBlock).apply(ContentLootFunction.builder)

    private def gasTankContent(gasTankBlock: BlockGasTank): LootTable.Builder = createSingleItemTable(gasTankBlock).apply(GasContentLootFunction.builder)
  }

  def makeConditionArray(conditions: List[ICondition]): JsonArray = {
    conditions.foldLeft(new JsonArray) { case (a, c) => a.add(CraftingHelper.serialize(c)); a }
  }

  def ingredientArray(i1: Ingredient, is: Ingredient*): Ingredient = {
    Ingredient.merge(CollectionConverters.asJava(i1 +: is))
  }
}
