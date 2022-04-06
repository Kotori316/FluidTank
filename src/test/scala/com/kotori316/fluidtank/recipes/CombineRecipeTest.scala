package com.kotori316.fluidtank.recipes

import cats.implicits._
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.Tier
import com.kotori316.fluidtank.{BeforeAllTest, FluidTank, ModObjects, hashTier}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.{DisplayName, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import scala.jdk.CollectionConverters._

object CombineRecipeTest extends BeforeAllTest {
  private final val woodTank = ModObjects.tierToBlock(Tier.WOOD)
  private final val stoneTank = ModObjects.tierToBlock(Tier.STONE)
  private final val emeraldTank = ModObjects.tierToBlock(Tier.EMERALD)
  private final val recipe = new CombineRecipe(new ResourceLocation(FluidTank.modID, "CombineRecipeTest".toLowerCase),
    Ingredient.of(ModObjects.blockTanks.filter(b => b.tier.isNormalTier && b.tier.isAvailableInVanilla): _*))

  @ParameterizedTest
  @ValueSource(ints = Array(1, 2, 5, 10, 64))
  def combine2Stone(stackSize: Int): Unit = {
    val wood = new ItemStack(woodTank)
    val stone = new ItemStack(stoneTank)
    RecipeInventoryUtil.getFluidHandler(wood).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)
    RecipeInventoryUtil.getFluidHandler(stone).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)
    wood.setCount(stackSize)
    stone.setCount(stackSize)

    val inventory = RecipeInventoryUtil.getInv("ws", itemMap = Map('s' -> stone, 'w' -> wood))
    assertTrue(recipe.matches(inventory, null))

    val result = recipe.assemble(inventory)
    assertEquals(1, result.getCount)
    assertFalse(result.isEmpty)
    assertTrue(result.getItem.isInstanceOf[ItemBlockTank], s"Class check of $result, ${result.getItem.getClass}")
    val tankItem = result.getItem.asInstanceOf[ItemBlockTank]
    assertTrue(tankItem.blockTank.tier === Tier.STONE, s"The tier of tank, $tankItem, ${tankItem.blockTank}, ${tankItem.blockTank.tier}")
    assertTrue(RecipeInventoryUtil.getFluidHandler(result).getFluid === FluidAmount.BUCKET_WATER.setAmount(2000L), s"Tag: ${result.getTag}")
  }

  @ParameterizedTest
  @ValueSource(strings = Array("sw", "ws"))
  def combine2StoneLeftTest(recipeFormat: String): Unit = {
    val wood = new ItemStack(woodTank)
    val stone = new ItemStack(stoneTank)
    RecipeInventoryUtil.getFluidHandler(wood).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)
    RecipeInventoryUtil.getFluidHandler(stone).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)

    val inventory = RecipeInventoryUtil.getInv(recipeFormat, itemMap = Map('s' -> stone, 'w' -> wood))
    assertTrue(recipe.matches(inventory, null))

    val remain = recipe.getRemainingItems(inventory)
    assertAll(remain.asScala.zipWithIndex.map[Executable] { case (s, index) => () =>
      if (index == recipeFormat.indexOf("w")) assertEquals(woodTank.itemBlock, s.getItem, s"Index $index (May be tank)") else assertTrue(s.isEmpty, s"Index $index")
    }.asJava)
  }

  @Test
  @DisplayName("Fail to combine Wood Tank and Stone tank and CACTUS.")
  def combine2StoneFail(): Unit = {
    val wood = new ItemStack(woodTank)
    val stone = new ItemStack(stoneTank)
    RecipeInventoryUtil.getFluidHandler(wood).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)
    RecipeInventoryUtil.getFluidHandler(stone).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)

    val inventory = RecipeInventoryUtil.getInv("ws", "c", itemMap = Map('s' -> stone, 'w' -> wood, 'c' -> new ItemStack(Blocks.CACTUS)))
    assertFalse(recipe.matches(inventory, null))
  }

  @Test
  def combine2Emerald(): Unit = {
    val wood = new ItemStack(woodTank)
    val emerald = new ItemStack(emeraldTank)
    RecipeInventoryUtil.getFluidHandler(wood).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)
    RecipeInventoryUtil.getFluidHandler(emerald).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)

    val inventory = RecipeInventoryUtil.getInv("ws", itemMap = Map('s' -> emerald, 'w' -> wood))
    assertTrue(recipe.matches(inventory, null))

    val result = recipe.assemble(inventory)
    assertEquals(1, result.getCount)
    assertFalse(result.isEmpty)
    assertTrue(result.getItem.isInstanceOf[ItemBlockTank], s"Class check of $result, ${result.getItem.getClass}")
    val tankItem = result.getItem.asInstanceOf[ItemBlockTank]
    assertTrue(tankItem.blockTank.tier === Tier.EMERALD, s"The tier of tank, $tankItem, ${tankItem.blockTank}, ${tankItem.blockTank.tier}")
    assertTrue(RecipeInventoryUtil.getFluidHandler(result).getFluid === FluidAmount.BUCKET_WATER.setAmount(2000L), s"Tag: ${result.getTag}")
  }

  @Test
  def combine2Same(): Unit = {
    val emerald0 = new ItemStack(emeraldTank)
    val emerald = new ItemStack(emeraldTank)
    RecipeInventoryUtil.getFluidHandler(emerald0).fill(FluidAmount.BUCKET_WATER.setAmount(4000L).toStack, EXECUTE)
    RecipeInventoryUtil.getFluidHandler(emerald).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)

    val inventory = RecipeInventoryUtil.getInv("ws", itemMap = Map('s' -> emerald, 'w' -> emerald0))
    assertTrue(recipe.matches(inventory, null))

    val result = recipe.assemble(inventory)
    assertTrue(result.getItem.isInstanceOf[ItemBlockTank], s"Class check of $result, ${result.getItem.getClass}")
    val tankItem = result.getItem.asInstanceOf[ItemBlockTank]
    assertTrue(tankItem.blockTank.tier === Tier.EMERALD, s"The tier of tank, $tankItem, ${tankItem.blockTank}, ${tankItem.blockTank.tier}")
    assertTrue(RecipeInventoryUtil.getFluidHandler(result).getFluid === FluidAmount.BUCKET_WATER.setAmount(5000L), s"Tag: ${result.getTag}")
  }

  @ParameterizedTest
  @ValueSource(ints = Array(1, 2, 5, 10, 64))
  def combine2SameLeftTest(stackSize: Int): Unit = {
    val emerald0 = new ItemStack(emeraldTank)
    val emerald = new ItemStack(emeraldTank)
    RecipeInventoryUtil.getFluidHandler(emerald0).fill(FluidAmount.BUCKET_WATER.setAmount(4000L).toStack, EXECUTE)
    RecipeInventoryUtil.getFluidHandler(emerald).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)
    emerald.setCount(stackSize)
    emerald0.setCount(stackSize)

    val inventory = RecipeInventoryUtil.getInv("ws", itemMap = Map('s' -> emerald, 'w' -> emerald0))
    assertTrue(recipe.matches(inventory, null))
    val remain = recipe.getRemainingItems(inventory)
    assertAll(remain.asScala.zipWithIndex.map[Executable] { case (s, index) => () =>
      if (index == 1) assertEquals(emeraldTank.itemBlock, s.getItem, s"Index $index (May be tank)") else assertTrue(s.isEmpty, s"Index $index")
    }.asJava)
  }

  @Test
  def combine2SameOverCapacity(): Unit = {
    val wood = new ItemStack(woodTank)
    val wood2 = new ItemStack(woodTank)
    RecipeInventoryUtil.getFluidHandler(wood).fill(FluidAmount.BUCKET_WATER.setAmount(4000L).toStack, EXECUTE)
    RecipeInventoryUtil.getFluidHandler(wood2).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)

    val inventory = RecipeInventoryUtil.getInv("ws", itemMap = Map('s' -> wood2, 'w' -> wood))
    assertFalse(recipe.matches(inventory, null))
  }

  @Test
  @DisplayName("Fail to combine Wood Tank and Creative Tank")
  def cantCombine1(): Unit = {
    val wood = new ItemStack(woodTank)
    RecipeInventoryUtil.getFluidHandler(wood).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)
    val creative = new ItemStack(ModObjects.tierToBlock(Tier.CREATIVE))

    val inventory = RecipeInventoryUtil.getInv("ws", itemMap = Map('s' -> creative, 'w' -> wood))
    assertFalse(recipe.matches(inventory, null))
  }

  @Test
  @DisplayName("Fail to combine Wood Tank and non tank")
  def cantCombine2(): Unit = {
    val wood = new ItemStack(woodTank)
    RecipeInventoryUtil.getFluidHandler(wood).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)
    val creative = new ItemStack(Blocks.BONE_BLOCK)

    val inventory = RecipeInventoryUtil.getInv("ws", itemMap = Map('s' -> creative, 'w' -> wood))
    assertFalse(recipe.matches(inventory, null))
  }

  @Test
  @DisplayName("Failed to combine Stone tank with 1000 mB of water and Void Tank")
  def cantCombine3(): Unit = {
    val voidTank = new ItemStack(ModObjects.tierToBlock(Tier.VOID))
    val stone = new ItemStack(stoneTank)
    RecipeInventoryUtil.getFluidHandler(stone).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)

    val inventory = RecipeInventoryUtil.getInv("ws", itemMap = Map('s' -> stone, 'w' -> voidTank))
    assertFalse(recipe.matches(inventory, null))
  }

  @Test
  @DisplayName("Combine Wood tank with 1000 mB of water and empty Stone Tank")
  def combineWithEmpty1(): Unit = {
    val wood = new ItemStack(woodTank)
    RecipeInventoryUtil.getFluidHandler(wood).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)
    val stone = new ItemStack(stoneTank)

    val inventory = RecipeInventoryUtil.getInv("ws", itemMap = Map('s' -> stone, 'w' -> wood))
    assertTrue(recipe.matches(inventory, null))
    val result = recipe.assemble(inventory)
    val tankItem = result.getItem.asInstanceOf[ItemBlockTank]
    assertEquals(Tier.STONE, tankItem.blockTank.tier)
    assertEquals(FluidAmount.BUCKET_WATER, RecipeInventoryUtil.getFluidHandler(result).getFluid)
  }
}
