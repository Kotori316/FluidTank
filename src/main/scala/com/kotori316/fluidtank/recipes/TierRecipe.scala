package com.kotori316.fluidtank.recipes

import cats.syntax.foldable._
import com.google.gson.JsonObject
import com.kotori316.fluidtank.blocks.BlockTank
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidKey}
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tier, TileTank}
import com.kotori316.fluidtank.{Config, FluidTank, ModObjects, Utils}
import net.minecraft.core.NonNullList
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.util.GsonHelper
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.crafting.{CraftingRecipe, Ingredient, RecipeSerializer}
import net.minecraft.world.item.{BlockItem, ItemStack}
import net.minecraft.world.level.Level
import net.minecraftforge.common.crafting.IShapedRecipe
import net.minecraftforge.registries.ForgeRegistryEntry

object TierRecipe {
  private final val LOGGER = Utils.getLogger(classOf[TierRecipe])
  final val SERIALIZER: RecipeSerializer[_] = new TierRecipe.Serializer
  private final val recipeWidth = 3
  private final val recipeHeight = 3

  private def filterTier(blockTank: BlockTank) = if (Config.content.usableUnavailableTankInRecipe.get()) true
  else blockTank.tier.hasWayToCreate

  final val KEY_TIER = "tier"
  final val KEY_SUB_ITEM = "sub_item"

  class Serializer extends ForgeRegistryEntry[RecipeSerializer[_]] with RecipeSerializer[TierRecipe] {
    setRegistryName(new ResourceLocation(FluidTank.modID, "crafting_grade_up"))

    override def fromJson(recipeId: ResourceLocation, json: JsonObject): TierRecipe = {
      val tier = Tier.byName(GsonHelper.getAsString(json, KEY_TIER)).orElse(Tier.Invalid)
      val subItem = Ingredient.fromJson(json.get(KEY_SUB_ITEM))
      if (subItem == Ingredient.EMPTY)
        LOGGER.warn("Empty ingredient was loaded for {}, data: {}", recipeId, json)
      LOGGER.debug("Serializer loaded {} from json for tier {}, sub {}.", recipeId, tier, subItem)
      new TierRecipe(recipeId, tier, subItem)
    }

    override def fromNetwork(recipeId: ResourceLocation, buffer: FriendlyByteBuf): TierRecipe = {
      val tierName = buffer.readUtf
      val tier = Tier.byName(tierName).orElseThrow(() => new IllegalArgumentException)
      val subItem = Ingredient.fromNetwork(buffer)
      if (subItem == Ingredient.EMPTY)
        LOGGER.warn("Empty ingredient was loaded for {}", recipeId)
      LOGGER.debug("Serializer loaded {} from packet for tier {}, sub {}..", recipeId, tier, subItem)
      new TierRecipe(recipeId, tier, subItem)
    }

    override def toNetwork(buffer: FriendlyByteBuf, recipe: TierRecipe): Unit = {
      buffer.writeUtf(recipe.getTier.toString)
      recipe.getSubItems.toNetwork(buffer)
      LOGGER.debug("Serialized {} to packet for tier {}.", recipe.getId, recipe.getTier)
    }
  }

  case class TierFinishedRecipe(recipeId: ResourceLocation, tier: Tier, ingredient: Ingredient) extends FinishedRecipe {

    def this(recipeId: ResourceLocation, tier: Tier) = {
      this(recipeId, tier, Ingredient.of(ItemTags.create(new ResourceLocation(tier.tagName))))
    }

    override def serializeRecipeData(json: JsonObject): Unit = {
      json.addProperty(KEY_TIER, this.tier.lowerName)
      json.add(KEY_SUB_ITEM, ingredient.toJson)
    }

    override def getId: ResourceLocation = recipeId

    override def getType: RecipeSerializer[_] = SERIALIZER

    override def serializeAdvancement: JsonObject = null

    override def getAdvancementId = new ResourceLocation("")
  }
}

class TierRecipe(id: ResourceLocation, tier: Tier, subItems: Ingredient)
  extends CraftingRecipe with IShapedRecipe[CraftingContainer] {
  private final val result = ModObjects.blockTanks.find(_.tier == this.tier).map(new ItemStack(_)).getOrElse(ItemStack.EMPTY)
  private final val normalTankSet = {
    val tierSet = Tier.values.filter((t: Tier) => t.rank == tier.rank - 1).toSet
    ModObjects.blockTanks.filter(b => tierSet(b.tier)).filter(TierRecipe.filterTier).toSet
  }

  TierRecipe.LOGGER.debug("Recipe instance({}) created for Tier {}.", id, tier)

  override def matches(inv: CraftingContainer, worldIn: Level): Boolean = checkInv(inv)

  private def checkInv(inv: CraftingContainer): Boolean = {
    var i = 0
    var j = 0
    while (i <= inv.getWidth - TierRecipe.recipeWidth) {
      while (j <= inv.getHeight - TierRecipe.recipeHeight) {
        if (this.checkMatch(inv, i, j))
          return true
        j += 1
      }
      i += 1
    }
    false
  }

  /**
   * Checks if the region of a crafting inventory is match for the recipe.
   * <p>Copied from [[net.minecraft.world.item.crafting.ShapedRecipe]]</p>
   */
  def checkMatch(craftingInventory: CraftingContainer, w: Int, h: Int): Boolean = {
    val ingredients = this.getIngredients
    var i = 0
    var j = 0
    while (i <= craftingInventory.getWidth) {
      while (j <= craftingInventory.getHeight) {
        val k = i - w
        val l = j - h

        val ingredient = if (k >= 0 && l >= 0 && k < TierRecipe.recipeWidth && l < TierRecipe.recipeHeight)
          ingredients.get(TierRecipe.recipeWidth - k - 1 + l * TierRecipe.recipeWidth)
        else
          Ingredient.EMPTY
        if (!ingredient.test(craftingInventory.getItem(i + j * craftingInventory.getWidth)))
          return false

        j += 1
      }
      i += 1
    }

    // Items are placed correctly.
    val tankStacks = (0 until craftingInventory.getContainerSize).map(craftingInventory.getItem).filter(this.getTankItems.test)
    tankStacks.sizeIs == 4 &&
      tankStacks.map(BlockItem.getBlockEntityData)
        .filter(_ != null)
        .map(nbt => FluidAmount.fromNBT(nbt.getCompound(TileTank.NBT_Tank)))
        .filter(_.nonEmpty)
        .map(FluidKey.from)
        .distinct.sizeIs <= 1
  }

  override def assemble(inv: CraftingContainer): ItemStack = {
    if (!this.checkInv(inv)) {
      TierRecipe.LOGGER.error("Requested to return crafting result for invalid inventory. {}", Range(0, inv.getContainerSize).map(inv.getItem))
      return ItemStack.EMPTY
    }
    val result = getResultItem
    val fluidAmount: FluidAmount = (0 until inv.getContainerSize).map(inv.getItem)
      .filter(s => s.getItem.isInstanceOf[ItemBlockTank])
      .map(BlockItem.getBlockEntityData)
      .filter(_ != null)
      .map(nbt => FluidAmount.fromNBT(nbt.getCompound(TileTank.NBT_Tank)))
      .filter(_.nonEmpty)
      .toList
      .combineAll
    if (fluidAmount.nonEmpty) {
      val compound = new CompoundTag
      val tankTag = new CompoundTag
      tankTag.putLong(TileTank.NBT_Capacity, tier.amount)
      fluidAmount.write(tankTag)
      compound.put(TileTank.NBT_Tank, tankTag)
      compound.put(TileTank.NBT_Tier, tier.toNBTTag)
      Utils.setTileTag(result, compound)
    }
    result
  }

  override def canCraftInDimensions(width: Int, height: Int): Boolean = width >= 3 && height >= 3

  override def getSerializer: RecipeSerializer[_] = TierRecipe.SERIALIZER

  override def getResultItem: ItemStack = result.copy

  override def getId: ResourceLocation = id

  override def getIngredients: NonNullList[Ingredient] = {
    allSlot.sortBy(_._1).map(_._2).foldLeft(NonNullList.create[Ingredient]()) { case (l, i) => l.add(i); l }
  }

  override def getRemainingItems(inv: CraftingContainer): NonNullList[ItemStack] = {
    val stacks = NonNullList.withSize(inv.getContainerSize, ItemStack.EMPTY)
    for (i <- 0 until stacks.size) {
      val item = inv.getItem(i)
      if (item.hasContainerItem && !item.getItem.isInstanceOf[ItemBlockTank])
        stacks.set(i, item.getContainerItem)
    }
    stacks
  }

  def getTankItems: Ingredient = {
    Ingredient.of(this.normalTankSet.map(new ItemStack(_)).toSeq: _*)
  }

  def getSubItems: Ingredient = subItems

  def getTier: Tier = tier

  def tankItemWithSlot: Seq[(Int, Ingredient)] = {
    Seq(0, 2, 6, 8).map(v => (v, getTankItems))
  }

  def subItemWithSlot: Seq[(Int, Ingredient)] = {
    Seq(1, 3, 5, 7).map(v => (v, getSubItems))
  }

  def allSlot: Seq[(Int, Ingredient)] = {
    (4, Ingredient.EMPTY) +: tankItemWithSlot :++ subItemWithSlot
  }

  override def getRecipeWidth = 3

  override def getRecipeHeight = 3
}