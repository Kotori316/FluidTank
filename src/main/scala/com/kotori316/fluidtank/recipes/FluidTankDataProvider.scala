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
import net.minecraft.tags.{ITag, ItemTags, TagCollectionManager}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.Tags
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition}
import net.minecraftforge.common.data.ExistingFileHelper
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.MarkerManager

import scala.util.{Failure, Success, Try}

@Mod.EventBusSubscriber(modid = FluidTank.modID, bus = Mod.EventBusSubscriber.Bus.MOD)
object FluidTankDataProvider {
  private final val MARKER = MarkerManager.getMarker("FluidTankDataProvider")

  @SubscribeEvent
  def gatherData(event: GatherDataEvent): Unit = {
    if (event.includeServer()) {
      event.getGenerator.addProvider(new AdvancementProvider(event.getGenerator))
      event.getGenerator.addProvider(new RecipeProvider(event.getGenerator))
      event.getGenerator.addProvider(new FluidTagsProvider(event.getGenerator, event.getExistingFileHelper))
    }
  }

  private[this] final val ID = (s: String) => new ResourceLocation(FluidTank.modID, s)

  private def tag(name: ResourceLocation): ITag.INamedTag[Item] = TagCollectionManager.getManager.getItemTags.get(name) match {
    case tag: ITag.INamedTag[_] => tag.asInstanceOf[ITag.INamedTag[Item]]
    case _ => ItemTags.createOptional(name)
  }

  class AdvancementProvider(generatorIn: DataGenerator) extends IDataProvider {
    override def act(cache: DirectoryCache): Unit = {
      val path = generatorIn.getOutputFolder
      val GSON = (new GsonBuilder).setPrettyPrinting().create

      val easyCondition = new FluidTankConditions.EasyCondition()
      val woodLocation = ID("tank_wood")
      val TANK_WOOD = AdvancementSerializeHelper(woodLocation)
        .addCriterion("has_bucket", FilledBucketTrigger.Instance.forItem(ItemPredicate.Builder.create().item(Items.WATER_BUCKET).build()))
        .addItemCriterion(Tags.Items.GLASS)
      val TANK_WOOD_EASY = TANK_WOOD.copy(location = ID("tank_wood_easy"))
      val TANKS = ModObjects.blockTanks.collect { case b if b.tier.hasTagRecipe => b.tier }
        .map(tier => AdvancementSerializeHelper(ID("tank_" + tier.toString.toLowerCase)).addItemCriterion(tag(new ResourceLocation(tier.tagName))))
      val VOID_TANK = AdvancementSerializeHelper(ID("tank_void"))
        .addItemCriterion(ForgeRegistries.ITEMS.getValue(woodLocation))
      val CAT = AdvancementSerializeHelper(ID("chest_as_tank"))
        .addItemCriterion(ForgeRegistries.ITEMS.getValue(woodLocation))
        .addCriterion("has_lots_of_items", new InventoryChangeTrigger.Instance(EntityPredicate.AndPredicate.ANY_AND, MinMaxBounds.IntBound.atLeast(10),
          MinMaxBounds.IntBound.UNBOUNDED, MinMaxBounds.IntBound.UNBOUNDED, Array(ItemPredicate.Builder.create().item(Items.WATER_BUCKET).build())))
      val PIPE = AdvancementSerializeHelper(ID("pipe"))
        .addCriterion("has_pearl", InventoryChangeTrigger.Instance.forItems(
          ItemPredicate.Builder.create().item(Items.ENDER_EYE).build(),
          ItemPredicate.Builder.create().tag(Tags.Items.ENDER_PEARLS).build()))
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
          IDataProvider.save(GSON, cache, advancement.build, out)
        } catch {
          case e: IOException => FluidTank.LOGGER.error(MARKER, s"Failed to save advancement ${advancement.location}.", e)
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
      val configCondition = new FluidTankConditions.ConfigCondition()
      val easyCondition = new FluidTankConditions.EasyCondition()
      val WOOD = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(tankWoodItem)
          .key('x', Tags.Items.GLASS).key('p', ItemTags.LOGS)
          .patternLine("x x")
          .patternLine("xpx")
          .patternLine("xxx"))
        .addCondition(configCondition)
        .addCondition(new NotCondition(easyCondition))
        .addCondition(new TagCondition(Tags.Items.GLASS.getName))
      val EASY_WOOD = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(tankWoodItem)
          .key('x', Tags.Items.GLASS).key('p', ItemTags.PLANKS)
          .patternLine("p p")
          .patternLine("p p")
          .patternLine("xpx"), saveName = ID("tank_wood_easy"))
        .addCondition(configCondition)
        .addCondition(easyCondition)
        .addCondition(new TagCondition(Tags.Items.GLASS.getName))
      val VOID = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(ForgeRegistries.ITEMS.getValue(ID("tank_void")))
          .key('o', Tags.Items.OBSIDIAN).key('t', woodTanks)
          .patternLine("ooo")
          .patternLine("oto")
          .patternLine("ooo"))
        .addCondition(configCondition)
      val CAT = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(ModObjects.blockCat)
          .key('x', woodTanks)
          .key('p', ingredientArray(Ingredient.fromTag(Tags.Items.CHESTS), Ingredient.fromItems(Blocks.BARREL)))
          .patternLine("x x")
          .patternLine("xpx")
          .patternLine("xxx"))
      val PIPE = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(ModObjects.blockFluidPipe)
          .key('t', woodTanks)
          .key('g', Tags.Items.GLASS)
          .key('e', ingredientArray(Ingredient.fromTag(Tags.Items.ENDER_PEARLS), Ingredient.fromItems(Items.ENDER_EYE)))
          .patternLine("gtg")
          .patternLine(" e ")
          .patternLine("gtg"))
        .addCondition(new NotCondition(easyCondition))
      val PIPE_EASY = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(ModObjects.blockFluidPipe, 2)
          .key('t', woodTanks)
          .key('g', Tags.Items.GLASS)
          .patternLine("gtg")
          .patternLine("   ")
          .patternLine("gtg"), saveName = ID("pipe_easy"))
        .addCondition(easyCondition)
      val ITEM_PIPE = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(ModObjects.blockItemPipe, 4)
          .key('t', woodTanks)
          .key('g', Tags.Items.GLASS)
          .key('e', ingredientArray(Ingredient.fromTag(Tags.Items.ENDER_PEARLS), Ingredient.fromItems(Items.ENDER_EYE)))
          .patternLine("g g")
          .patternLine("tet")
          .patternLine("g g"))
        .addCondition(new NotCondition(easyCondition))
      val ITEM_PIPE_EASY = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(ModObjects.blockItemPipe, 8)
          .key('t', woodTanks)
          .key('g', Tags.Items.GLASS)
          .patternLine("g g")
          .patternLine("t t")
          .patternLine("g g"), saveName = ID("item_pipe_easy"))
        .addCondition(easyCondition)
      val TANKS = ModObjects.blockTanks.collect { case b if b.tier.hasTagRecipe => b.tier }
        .map(tier => RecipeSerializeHelper(new TierRecipe.FinishedRecipe(ID("tank_" + tier.toString.toLowerCase), tier))
          .addTagCondition(tag(new ResourceLocation(tier.tagName)))
          .addCondition(configCondition))
      val FLUID_SOURCE = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shapedRecipe(ModObjects.blockSource)
          .patternLine("wiw")
          .patternLine("gIg")
          .patternLine("wdw")
          .key('w', Items.WATER_BUCKET)
          .key('i', Tags.Items.INGOTS_IRON)
          .key('g', Tags.Items.INGOTS_GOLD)
          .key('I', Tags.Items.STORAGE_BLOCKS_IRON)
          .key('d', Blocks.DIRT))

      val recipes = FLUID_SOURCE :: PIPE :: PIPE_EASY :: ITEM_PIPE :: ITEM_PIPE_EASY :: CAT :: WOOD :: EASY_WOOD :: VOID :: TANKS

      for (recipe <- recipes) {
        val out = path.resolve(s"data/${recipe.location.getNamespace}/recipes/${recipe.location.getPath}.json")
        try {
          IDataProvider.save(GSON, cache, recipe.build, out)
        } catch {
          case e: IOException => FluidTank.LOGGER.error(MARKER, s"Failed to save recipe ${recipe.location}.", e)
        }
      }
    }

    override def getName = "Recipe of FluidTank"

  }

  class FluidTagsProvider(g: DataGenerator, e: ExistingFileHelper) extends net.minecraft.data.FluidTagsProvider(g, FluidTank.modID, e) {
    override def registerTags(): Unit = {
      val tag = Try {
        val f = Class.forName("net.minecraftforge.common.Tags$Fluids").getField("MILK")
        f.get(null).asInstanceOf[ITag.INamedTag[net.minecraft.fluid.Fluid]]
      }
      tag match {
        case Failure(exception) => println("Skipped Fluid Tag provider " + exception.toString)
        case Success(t) => getOrCreateBuilder(t).addOptional(ModObjects.MILK_FLUID.getRegistryName)
      }
    }
  }

  def makeConditionArray(conditions: List[ICondition]): JsonArray = {
    conditions.foldLeft(new JsonArray) { case (a, c) => a.add(CraftingHelper.serialize(c)); a }
  }

  def ingredientArray(i1: Ingredient, is: Ingredient*): Ingredient = {
    Ingredient.merge(java.util.Arrays.asList(i1 +: is: _*))
  }
}
