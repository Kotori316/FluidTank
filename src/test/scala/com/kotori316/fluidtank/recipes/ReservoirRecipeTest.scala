package com.kotori316.fluidtank.recipes

import cats.implicits._
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.tiles.Tiers
import com.kotori316.fluidtank.{BeforeAllTest, FluidTank, ModObjects}
import net.minecraft.item.{ItemStack, Items}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

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
  private[recipes] def copyNBT1(): Unit = {
    val recipe = new ReservoirRecipe(new ResourceLocation(FluidTank.modID, "stone"), Tiers.STONE)
    val tank = new ItemStack(stone)
    RecipeInventoryUtil.getFluidHandler(tank).fill(FluidAmount.BUCKET_WATER.toStack, FluidAction.EXECUTE)
    val inv = RecipeInventoryUtil.getInv("tb", itemMap = Map('t' -> tank, 'b' -> new ItemStack(Items.BUCKET)))
    assertTrue(recipe.matches(inv, null))

    val result = recipe.getCraftingResult(inv)
    val handler = RecipeInventoryUtil.getFluidHandler(result)
    assertEquals(FluidAmount.BUCKET_WATER, handler.getFluid)
    assertEquals(Tiers.STONE.amount, handler.getCapacity)
  }
}
