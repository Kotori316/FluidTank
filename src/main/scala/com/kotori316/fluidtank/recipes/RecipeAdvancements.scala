package com.kotori316.fluidtank.recipes

import java.nio.file.{Files, Path}

import com.google.gson.{GsonBuilder, JsonArray, JsonObject}
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import net.minecraft.advancements.criterion.{FilledBucketTrigger, InventoryChangeTrigger, ItemPredicate, RecipeUnlockedTrigger}
import net.minecraft.advancements.{Advancement, AdvancementRewards, ICriterionInstance, IRequirementsStrategy}
import net.minecraft.item.{Item, Items}
import net.minecraft.tags.Tag
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.Tags
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition, TagEmptyCondition}

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
  def output(path: Path): Unit = {
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

    if (Files.notExists(path))
      Files.createDirectories(path)

    for (a <- recipeAdvancements) {
      val out = path.resolve(s"${a.location.getPath}.json")
      Files.write(out,
        java.util.Arrays.asList(GSON.toJson(a.build).split("\n"): _*))
    }
  }

}
