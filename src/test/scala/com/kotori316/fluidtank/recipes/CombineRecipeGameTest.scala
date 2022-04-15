package com.kotori316.fluidtank.recipes

import com.kotori316.fluidtank.{Config, FluidTank, ModObjects}
import com.kotori316.testutil.GameTestUtil
import com.kotori316.testutil.GameTestUtil.EMPTY_STRUCTURE
import net.minecraft.gametest.framework.{GameTest, GameTestAssertException, GameTestHelper}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.common.Tags
import net.minecraftforge.common.crafting.conditions.ICondition
import net.minecraftforge.gametest.{GameTestHolder, PrefixGameTestTemplate}
import org.junit.jupiter.api.Assertions.{assertAll, assertFalse, assertTrue}
import org.junit.jupiter.api.function.Executable
import org.junit.platform.commons.util.ReflectionUtils

import scala.util.Try

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
class CombineRecipeGameTest {
  final val BATCH = "recipeTestBatch"

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def readTag(helper: GameTestHelper): Unit = {
    val r = new ItemStack(Blocks.STONE).is(Tags.Items.STONE)
    if (!r) throw new GameTestAssertException("Tags.Items.STONE is not for Blocks.STONE")
    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def noUnavailableTank(helper: GameTestHelper): Unit = {
    val context = GameTestUtil.getContext(helper)
    try {
      Config.content.usableUnavailableTankInRecipe.set(false)
      val ingredient = Try(CombineRecipe.SERIALIZER.getClass.getDeclaredMethod("tankList", classOf[ICondition.IContext]))
        .map(m => ReflectionUtils.invokeMethod(m, null, context))
        .collect { case i: Ingredient => i }.getOrElse(throw new RuntimeException)

      val (accept, deny) = ModObjects.blockTanks.partition(b => b.tier.isAvailableInVanilla && b.tier.isNormalTier)
      assertAll(
        accept.map(new ItemStack(_)).map[Executable](i => () => assertTrue(ingredient.test(i), "%s must match.".formatted(i))): _*
      )
      assertAll(
        deny.map(new ItemStack(_)).map[Executable](i => () => assertFalse(ingredient.test(i), "%s must not match.".formatted(i))): _*
      )
    } finally {
      Config.content.usableUnavailableTankInRecipe.set(true)
    }
    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def okUnavailableTank(helper: GameTestHelper): Unit = {
    val context = GameTestUtil.getContext(helper)
    try {
      Config.content.usableUnavailableTankInRecipe.set(true)
      val ingredient = Try(CombineRecipe.SERIALIZER.getClass.getDeclaredMethod("tankList", classOf[ICondition.IContext]))
        .map(m => ReflectionUtils.invokeMethod(m, null, context))
        .collect { case i: Ingredient => i }.getOrElse(throw new RuntimeException)

      val (accept, deny) = ModObjects.blockTanks.partition(b => b.tier.isNormalTier)
      assertAll(
        accept.map(new ItemStack(_)).map[Executable](i => () => assertTrue(ingredient.test(i), "%s must match.".formatted(i))): _*
      )
      assertAll(
        deny.map(new ItemStack(_)).map[Executable](i => () => assertFalse(ingredient.test(i), "%s must not match.".formatted(i))): _*
      )
    } finally {
      Config.content.usableUnavailableTankInRecipe.set(true)
    }
    helper.succeed()
  }
}
