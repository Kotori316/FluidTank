package com.kotori316.fluidtank.recipes

import cats.implicits._
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.Tiers
import com.kotori316.fluidtank.{BeforeAllTest, FluidTank, ModObjects}
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import scala.jdk.CollectionConverters._

private[recipes] class CombineRecipeTest extends BeforeAllTest {
  private final val woodTank = ModObjects.blockTanks.head
  private final val stoneTank = ModObjects.blockTanks.find(_.tier === Tiers.STONE).get
  private final val emeraldTank = ModObjects.blockTanks.find(_.tier === Tiers.EMERALD).get
  private final val recipe = new CombineRecipe(new ResourceLocation(FluidTank.modID, "CombineRecipeTest".toLowerCase))

  @Test
  def combine2Stone(): Unit = {
    val wood = new ItemStack(woodTank)
    val stone = new ItemStack(stoneTank)
    RecipeInventoryUtil.getFluidHandler(wood).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)
    RecipeInventoryUtil.getFluidHandler(stone).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)

    val inventory = RecipeInventoryUtil.getInv("ws", itemMap = Map('s' -> stone, 'w' -> wood))
    assertTrue(recipe.matches(inventory, null))

    val result = recipe.getCraftingResult(inventory)
    assertTrue(result.getItem.isInstanceOf[ItemBlockTank], s"Class check of $result, ${result.getItem.getClass}")
    val tankItem = result.getItem.asInstanceOf[ItemBlockTank]
    assertTrue(tankItem.blockTank.tier === Tiers.STONE, s"The tier of tank, $tankItem, ${tankItem.blockTank}, ${tankItem.blockTank.tier}")
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

    val result = recipe.getCraftingResult(inventory)
    assertTrue(result.getItem.isInstanceOf[ItemBlockTank], s"Class check of $result, ${result.getItem.getClass}")
    val tankItem = result.getItem.asInstanceOf[ItemBlockTank]
    assertTrue(tankItem.blockTank.tier === Tiers.EMERALD, s"The tier of tank, $tankItem, ${tankItem.blockTank}, ${tankItem.blockTank.tier}")
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

    val result = recipe.getCraftingResult(inventory)
    assertTrue(result.getItem.isInstanceOf[ItemBlockTank], s"Class check of $result, ${result.getItem.getClass}")
    val tankItem = result.getItem.asInstanceOf[ItemBlockTank]
    assertTrue(tankItem.blockTank.tier === Tiers.EMERALD, s"The tier of tank, $tankItem, ${tankItem.blockTank}, ${tankItem.blockTank.tier}")
    assertTrue(RecipeInventoryUtil.getFluidHandler(result).getFluid === FluidAmount.BUCKET_WATER.setAmount(5000L), s"Tag: ${result.getTag}")
  }

  @Test
  def combine2SameLeftTest(): Unit = {
    val emerald0 = new ItemStack(emeraldTank)
    val emerald = new ItemStack(emeraldTank)
    RecipeInventoryUtil.getFluidHandler(emerald0).fill(FluidAmount.BUCKET_WATER.setAmount(4000L).toStack, EXECUTE)
    RecipeInventoryUtil.getFluidHandler(emerald).fill(FluidAmount.BUCKET_WATER.toStack, EXECUTE)

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
}
