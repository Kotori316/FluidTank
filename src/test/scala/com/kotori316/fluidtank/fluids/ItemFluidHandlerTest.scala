package com.kotori316.fluidtank.fluids

import com.kotori316.fluidtank.recipes.RecipeInventoryUtil
import com.kotori316.fluidtank.tiles.{Tier, TileTank}
import com.kotori316.fluidtank.{BeforeAllTest, FluidTank, ModObjects}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

//noinspection DuplicatedCode It's a test.
object ItemFluidHandlerTest extends BeforeAllTest {
  private def woodTank = ModObjects.blockTanks.head

  @Test
  def woodTankIsWood(): Unit = {
    assertEquals(new ResourceLocation(FluidTank.modID, "tank_wood"), woodTank.getRegistryName)
  }

  @Test
  def emptyTank(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)

    assertEquals(Tier.WOOD, handler.tiers)
    assertEquals(FluidAmount.EMPTY, handler.getFluid)
    assertEquals(1000, handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.SIMULATE))
  }

  @Test
  def fillInEmpty(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)

    handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.BUCKET_WATER, handler.getFluid)

    handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(2000L), handler.getFluid)
  }

  @Test
  def filledTag(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)

    handler.fill(FluidAmount.BUCKET_WATER.setAmount(2000L).toStack, IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(2000L), handler.getFluid)

    val tag = stack.getTag
    assertNotNull(tag)
    val childTag = tag.getCompound("BlockEntityTag")
    assertFalse(childTag.isEmpty)
    val fluidInTank = FluidAmount.fromNBT(childTag.getCompound(TileTank.NBT_Tank))
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(2000L), fluidInTank)
  }

  @Test
  def stackedTankTest(): Unit = {
    val stack = new ItemStack(woodTank, 3)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)

    assertEquals(0, handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE))
    assertEquals(FluidAmount.EMPTY, handler.getFluid)
    assertEquals(FluidStack.EMPTY, handler.drain(1000, IFluidHandler.FluidAction.EXECUTE))
    assertEquals(FluidAmount.EMPTY, handler.getFluid)
  }

  @Test
  def serializeTest(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)
    handler.fill(FluidAmount.BUCKET_WATER.setAmount(4000L).toStack, IFluidHandler.FluidAction.EXECUTE)

    val stackTag = handler.createTag
    assertEquals(Tier.WOOD.toString.toLowerCase, stackTag.getString("tier"))
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(4000L), FluidAmount.fromNBT(stackTag.getCompound("tank")))
  }
}
