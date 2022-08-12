package com.kotori316.fluidtank.recipes

import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.tiles.Tier
import com.kotori316.fluidtank.{BeforeAllTest, FluidTank, ModObjects}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.{ItemStack, Items}
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import scala.jdk.CollectionConverters._

class ReservoirRecipeTest extends BeforeAllTest {
  private final val wood = ModObjects.tierToBlock(Tier.WOOD)
  private final val stone = ModObjects.tierToBlock(Tier.STONE)

  @Test
  private[recipes] def matchTest(): Unit = {
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "stone"), Tier.STONE)
    val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> new ItemStack(stone), 'b' -> new ItemStack(Items.BUCKET)))
    assertTrue(recipe.matches(inv, null))
    assertFalse(recipe.assemble(inv).hasTag)
  }

  @Test
  private[recipes] def matchTest1(): Unit = {
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "stone"), Tier.STONE, List(Ingredient.of(Items.BUCKET)).asJava)
    val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> new ItemStack(stone), 'b' -> new ItemStack(Items.BUCKET)))
    assertTrue(recipe.matches(inv, null))
    assertFalse(recipe.assemble(inv).hasTag)
  }

  @Test
  private[recipes] def remainTest(): Unit = {
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "stone"), Tier.STONE)
    val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> new ItemStack(stone), 'b' -> new ItemStack(Items.BUCKET)))
    assertTrue(recipe.matches(inv, null))
    val stacks = recipe.getRemainingItems(inv)
    assertTrue(stacks.stream().allMatch(_.isEmpty))
  }

  @Test
  private[recipes] def matchTest2(): Unit = {
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "stone"), Tier.STONE)
    val inv = RecipeInventoryUtil.getInv("b", itemMap = Map('b' -> new ItemStack(Items.BUCKET)))
    assertFalse(recipe.matches(inv, null))
  }

  @Test
  def matchTest3(): Unit = {
    val item = Items.ACACIA_PLANKS
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "wood"), Tier.WOOD,
      List(Ingredient.of(item)).asJava)
    locally {
      val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> new ItemStack(wood), 'b' -> new ItemStack(item)))
      assertTrue(recipe.matches(inv, null))
      val stacks = recipe.getRemainingItems(inv)
      assertTrue(stacks.stream().allMatch(_.isEmpty))
      assertFalse(recipe.assemble(inv).hasTag)
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
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "wood"), Tier.WOOD,
      List(Ingredient.of(item1), Ingredient.of(item2)).asJava)
    val itemMap = Map('t' -> new ItemStack(wood), 'l' -> item1, 'a' -> item2)
    locally {
      val inv = RecipeInventoryUtil.getInv("tla", itemMap = itemMap)
      assertTrue(recipe.matches(inv, null))
      val stacks = recipe.getRemainingItems(inv)
      assertTrue(stacks.stream().allMatch(_.isEmpty))
      assertFalse(recipe.assemble(inv).hasTag)
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
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "stone"), Tier.STONE)
    val tank = new ItemStack(stone)
    RecipeInventoryUtil.getFluidHandler(tank).fill(fluid.toStack, FluidAction.EXECUTE)
    val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> tank, 'b' -> new ItemStack(Items.BUCKET)))
    assertTrue(recipe.matches(inv, null))

    val result = recipe.assemble(inv)
    val handler = RecipeInventoryUtil.getFluidHandler(result)
    assertEquals(fluid, handler.getFluid)
    assertEquals(Tier.STONE.amount, handler.getCapacity)
  }

}

object ReservoirRecipeTest {

  def stoneFluids(): Array[FluidAmount] = {
    val fluids = for {
      kind <- List(FluidAmount.BUCKET_WATER, FluidAmount.BUCKET_LAVA)
      amount <- Range.inclusive(1000, 16000, 1500)
    } yield kind.setAmount(amount)
    fluids.toArray
  }
}
