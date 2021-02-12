package com.kotori316.fluidtank.recipes

import cats.implicits._
import com.kotori316.fluidtank.blocks.BlockInvisibleTank
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.{BeforeAllTest, FluidTank, ModObjects}
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction._
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import scala.util.chaining._

private[recipes] final class ConvertRecipeTest extends BeforeAllTest {
  private final val woodTank = ModObjects.blockTanks.head
  private final val woodInvisible = ModObjects.blockTanksInvisible.head

  private def id(methodName: String) = new ResourceLocation(FluidTank.modID, methodName.toLowerCase)

  @Test
  def normal2Invisible(): Unit = {
    val recipe = new ConvertInvisibleRecipe(id("normal2Invisible"))
    val tankStack = new ItemStack(woodTank)
    val inv = RecipeInventoryUtil.getInv("   ", " s ", "   ", Map('s' -> tankStack))

    assertTrue(recipe.matches(inv, null), "recipe match")
    val result = recipe.getCraftingResult(inv)
    assertEquals(classOf[BlockInvisibleTank], result.getItem.asInstanceOf[ItemBlockTank].blockTank.getClass)
    assertFalse(result.hasTag)
    locally {
      val handler = RecipeInventoryUtil.getFluidHandler(result)
      handler.init()
      assertTrue(handler.getFluid.isEmpty)
    }
  }

  @Test
  def noMatchTest(): Unit = {
    val recipe = new ConvertInvisibleRecipe(id("noMatchTest"))
    val tankStack = new ItemStack(woodTank)
    val inv = RecipeInventoryUtil.getInv("ss", itemMap = Map('s' -> tankStack))
    assertFalse(recipe.matches(inv, null), "2 tanks in inventory")
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.fluidtank.recipes.TierRecipeTest#fluids1"))
  def normal2Invisible2(fluidAmount: FluidAmount): Unit = {
    val recipe = new ConvertInvisibleRecipe(id("normal2Invisible2"))
    val tankStack = new ItemStack(woodTank)
    locally {
      val handler = RecipeInventoryUtil.getFluidHandler(tankStack)
      handler.fill(fluidAmount.toStack, EXECUTE)
    }
    val inv = RecipeInventoryUtil.getInv("   ", " s ", "   ", Map('s' -> tankStack))

    assertTrue(recipe.matches(inv, null))
    val result = recipe.getCraftingResult(inv)
    assertEquals(classOf[BlockInvisibleTank], result.getItem.asInstanceOf[ItemBlockTank].blockTank.getClass)
    assertEquals(woodInvisible, result.getItem.asInstanceOf[ItemBlockTank].blockTank)
    locally {
      val handler = RecipeInventoryUtil.getFluidHandler(result)
      handler.init()
      assertTrue(fluidAmount === handler.getFluid, s"${handler.getFluid}")
    }
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.fluidtank.recipes.TierRecipeTest#fluids1"))
  def invisible2Normal(fluid: FluidAmount): Unit = {
    val recipe = new ConvertInvisibleRecipe(id("invisible2Normal"))
    val tankStack = new ItemStack(woodInvisible).tap(s => RecipeInventoryUtil.getFluidHandler(s).fill(fluid.toStack, EXECUTE))
    val inv = RecipeInventoryUtil.getInv("", "s", "", Map('s' -> tankStack))
    assertTrue(recipe.matches(inv, null))
    val result = recipe.getCraftingResult(inv)
    assertEquals(woodTank, result.getItem.asInstanceOf[ItemBlockTank].blockTank)
    locally {
      val handler = RecipeInventoryUtil.getFluidHandler(result)
      handler.init()
      assertTrue(fluid === handler.getFluid, s"${handler.getFluid}")
    }
  }
}
