package com.kotori316.fluidtank.recipes

import com.google.gson.{JsonArray, JsonObject}
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.Tier
import com.kotori316.fluidtank.{FluidTank, ModObjects, Utils}
import net.minecraft.core.NonNullList
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.crafting.{Ingredient, RecipeSerializer, ShapelessRecipe}
import net.minecraft.world.item.{BlockItem, ItemStack, Items}
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.registries.ForgeRegistryEntry

import scala.jdk.CollectionConverters._

final case class ReservoirRecipe(idIn: ResourceLocation, tier: Tier, subIngredients: Seq[Ingredient])
  extends ShapelessRecipe(idIn, ReservoirRecipe.GROUP, ReservoirRecipe.findOutput(tier), ReservoirRecipe.findIngredients(tier, subIngredients)) {

  def this(idIn: ResourceLocation, tier: Tier) = {
    this(idIn, tier, Seq(Ingredient.of(Items.BUCKET)))
  }

  override def assemble(inv: CraftingContainer): ItemStack = {
    val result = super.assemble(inv)
    (0 until inv.getContainerSize)
      .map(inv.getItem)
      .filter(_.getItem.isInstanceOf[ItemBlockTank])
      .filter(_.hasTag)
      .map(BlockItem.getBlockEntityData)
      .find(_ != null)
      .foreach(nbt => Utils.setTileTag(result, nbt))
    result
  }

  override def getRemainingItems(inv: CraftingContainer): NonNullList[ItemStack] =
    NonNullList.withSize(inv.getContainerSize, ItemStack.EMPTY)
}

object ReservoirRecipe {
  val GROUP = "fluidtank:reservoirs"
  val SERIALIZER: RecipeSerializer[ReservoirRecipe] = new ReservoirRecipe.Serializer

  private def findOutput(tier: Tier): ItemStack = {
    ModObjects.itemReservoirs
      .find(_.tier == tier)
      .map(new ItemStack(_))
      .getOrElse {
        throw new IllegalStateException("Reservoir of " + tier + " not found.")
      }
  }

  private def findIngredients(tier: Tier, subIngredients: Seq[Ingredient]): NonNullList[Ingredient] = {
    val recipeItemsIn = NonNullList.create[Ingredient]
    val tankStream = ModObjects.blockTanks.filter(_.tier == tier)

    val tankIngredient = Ingredient.of(tankStream.map(new ItemStack(_)): _*)
    recipeItemsIn.add(tankIngredient)
    recipeItemsIn.addAll(subIngredients.asJava)
    recipeItemsIn
  }

  class Serializer extends ForgeRegistryEntry[RecipeSerializer[_]] with RecipeSerializer[ReservoirRecipe] {
    setRegistryName(new ResourceLocation(FluidTank.modID, "reservoir_recipe"))

    override def fromJson(recipeId: ResourceLocation, json: JsonObject): ReservoirRecipe = {
      val tier: Tier = Tier.byName(GsonHelper.getAsString(json, "tier")).orElse(Tier.Invalid)
      val ingredientList = if (json.has("sub")) {
        GsonHelper.getAsJsonArray(json, "sub").iterator().asScala
          .map(CraftingHelper.getIngredient)
          .toSeq
      } else {
        Seq(Ingredient.of(Items.BUCKET))
      }

      if (ingredientList.isEmpty || ingredientList.size > 8) {
        FluidTank.LOGGER.error("Too many or too few items to craft reservoir. Size: {}, {}, Recipe: {}",
          ingredientList.size, ingredientList, recipeId)
      }
      new ReservoirRecipe(recipeId, tier, ingredientList)
    }

    override def fromNetwork(recipeId: ResourceLocation, buffer: FriendlyByteBuf): ReservoirRecipe = {
      val tierName: String = buffer.readUtf
      val tier: Tier = Tier.byName(tierName).orElseThrow(() => new IllegalArgumentException)
      val subIngredientCount: Int = buffer.readVarInt
      val ingredients = (0 until subIngredientCount).map(_ => Ingredient.fromNetwork(buffer))
      new ReservoirRecipe(recipeId, tier, ingredients)
    }

    override def toNetwork(buffer: FriendlyByteBuf, recipe: ReservoirRecipe): Unit = {
      buffer.writeUtf(recipe.tier.toString)
      buffer.writeVarInt(recipe.subIngredients.size)
      recipe.subIngredients.foreach(i => i.toNetwork(buffer))
    }
  }

  case class ReservoirFinishedRecipe(recipe: ReservoirRecipe) extends FinishedRecipe {
    override def serializeRecipeData(json: JsonObject): Unit = {
      json.addProperty("tier", recipe.tier.lowerName)
      val subIngredients = new JsonArray
      recipe.subIngredients.map(_.toJson).foreach(subIngredients.add)
      json.add("sub", subIngredients)
    }

    override def getId: ResourceLocation = recipe.getId

    override def getType: RecipeSerializer[_] = SERIALIZER

    override def serializeAdvancement: JsonObject = null

    override def getAdvancementId = new ResourceLocation("")
  }
}
