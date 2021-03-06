package com.kotori316.fluidtank.recipes

import cats.implicits._
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.tiles.Tiers
import com.kotori316.fluidtank.{BeforeAllTest, FluidTank, ModObjects}
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.{ItemStack, Items}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import scala.jdk.CollectionConverters._

private[recipes] final class ReservoirRecipeTest extends BeforeAllTest {
  val wood = ModObjects.blockTanks.head
  val stone = ModObjects.blockTanks.find(_.tier === Tiers.STONE).get

  @Test
  private[recipes] def matchTest(): Unit = {
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "stone"), Tiers.STONE)
    val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> new ItemStack(stone), 'b' -> new ItemStack(Items.BUCKET)))
    assertTrue(recipe.matches(inv, null))
    assertFalse(recipe.getCraftingResult(inv).hasTag)
  }

  @Test
  private[recipes] def matchTest1(): Unit = {
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "stone"), Tiers.STONE, List(Ingredient.fromItems(Items.BUCKET)).asJava)
    val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> new ItemStack(stone), 'b' -> new ItemStack(Items.BUCKET)))
    assertTrue(recipe.matches(inv, null))
    assertFalse(recipe.getCraftingResult(inv).hasTag)
  }

  @Test
  private[recipes] def remainTest(): Unit = {
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "stone"), Tiers.STONE)
    val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> new ItemStack(stone), 'b' -> new ItemStack(Items.BUCKET)))
    assertTrue(recipe.matches(inv, null))
    val stacks = recipe.getRemainingItems(inv)
    assertTrue(stacks.stream().allMatch(_.isEmpty))
  }

  @Test
  private[recipes] def matchTest2(): Unit = {
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "stone"), Tiers.STONE)
    val inv = RecipeInventoryUtil.getInv("b", itemMap = Map('b' -> new ItemStack(Items.BUCKET)))
    assertFalse(recipe.matches(inv, null))
  }

  @Test
  def matchTest3(): Unit = {
    val item = Items.ACACIA_PLANKS
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "wood"), Tiers.WOOD,
      List(Ingredient.fromItems(item)).asJava)
    locally {
      val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> new ItemStack(wood), 'b' -> new ItemStack(item)))
      assertTrue(recipe.matches(inv, null))
      val stacks = recipe.getRemainingItems(inv)
      assertTrue(stacks.stream().allMatch(_.isEmpty))
      assertFalse(recipe.getCraftingResult(inv).hasTag)
    }
    locally {
      val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> new ItemStack(wood), 'b' -> new ItemStack(Items.BUCKET)))
      assertFalse(recipe.matches(inv, null))
    }
  }

  @Test
  def matchTest4(): Unit = {
    val item1 = new ItemStack(Items.ACACIA_LOG)
    val item2 = new ItemStack(Items.APPLE)
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "wood"), Tiers.WOOD,
      List(Ingredient.fromStacks(item1), Ingredient.fromStacks(item2)).asJava)
    val itemMap = Map('t' -> new ItemStack(wood), 'l' -> item1, 'a' -> item2)
    locally {
      val inv = RecipeInventoryUtil.getInv("tla", itemMap = itemMap)
      assertTrue(recipe.matches(inv, null))
      val stacks = recipe.getRemainingItems(inv)
      assertTrue(stacks.stream().allMatch(_.isEmpty))
      assertFalse(recipe.getCraftingResult(inv).hasTag)
    }
    val invalidRecipes: Seq[Executable] = "alt".combinations(2).flatMap(_.permutations).map[Executable] { recipeStr =>
      val inv = RecipeInventoryUtil.getInv(recipeStr, itemMap = itemMap)
      () => assertFalse(recipe.matches(inv, null))
    }.toSeq
    assertAll(invalidRecipes: _*)
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.fluidtank.recipes.ReservoirRecipeTest#stoneFluids"))
  private[recipes] def copyNBT1(fluid: FluidAmount): Unit = {
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "stone"), Tiers.STONE)
    val tank = new ItemStack(stone)
    RecipeInventoryUtil.getFluidHandler(tank).fill(fluid.toStack, FluidAction.EXECUTE)
    val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> tank, 'b' -> new ItemStack(Items.BUCKET)))
    assertTrue(recipe.matches(inv, null))

    val result = recipe.getCraftingResult(inv)
    val handler = RecipeInventoryUtil.getFluidHandler(result)
    assertEquals(fluid, handler.getFluid)
    assertEquals(Tiers.STONE.amount, handler.getCapacity)
  }
}

private object ReservoirRecipeTest {
  def stoneFluids(): Array[FluidAmount] = {
    val fluids = for {
      kind <- List(FluidAmount.BUCKET_WATER, FluidAmount.BUCKET_LAVA)
      amount <- Range.inclusive(1000, 16000, 1500)
    } yield kind.setAmount(amount)
    fluids.toArray
  }
}
