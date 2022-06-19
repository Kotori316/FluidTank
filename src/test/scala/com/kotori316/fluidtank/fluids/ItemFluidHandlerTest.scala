package com.kotori316.fluidtank.fluids

import com.kotori316.fluidtank.DynamicSerializable._
import com.kotori316.fluidtank.recipes.RecipeInventoryUtil
import com.kotori316.fluidtank.tiles.{Tiers, TileTankNoDisplay}
import com.kotori316.fluidtank.{BeforeAllTest, DynamicSerializable, FluidTank, ModObjects}
import com.mojang.serialization.{Dynamic, JsonOps}
import net.minecraft.item.ItemStack
import net.minecraft.util.{JSONUtils, ResourceLocation}
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

//noinspection DuplicatedCode It's a test.
private[fluids] final class ItemFluidHandlerTest extends BeforeAllTest {
  private def woodTank = ModObjects.blockTanks.head

  @Test
  def woodTankIsWood(): Unit = {
    assertEquals(new ResourceLocation(FluidTank.modID, "tank_wood"), woodTank.getRegistryName)
  }

  @Test
  def emptyTank(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)

    assertEquals(Tiers.WOOD, handler.tiers)
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
    val fluidInTank = FluidAmount.fromNBT(childTag.getCompound(TileTankNoDisplay.NBT_Tank))
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

    val stackTagJson = handler.createTag.toJson
    assertEquals(Tiers.WOOD.toString.toLowerCase, JSONUtils.getString(stackTagJson.getAsJsonObject, "tier"))
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(4000L),
      DynamicSerializable[FluidAmount].deserialize(new Dynamic(JsonOps.INSTANCE, stackTagJson.getAsJsonObject.get("tank"))))
  }

  @ParameterizedTest
  @ValueSource(ints = Array(1, 10, 100, 500, 999, 1000))
  def tankContainer1(amount: Int): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)
    handler.fill(FluidAmount.BUCKET_WATER.setAmount(amount).toStack, IFluidHandler.FluidAction.EXECUTE)

    val container = stack.getContainerItem
    assertEquals(woodTank.itemBlock, container.getItem)
    val containerContent = RecipeInventoryUtil.getFluidHandler(container).getFluid
    assertTrue(containerContent.isEmpty)
    assertEquals(0L, containerContent.amount)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(amount), handler.getFluid)
  }

  @ParameterizedTest
  @ValueSource(ints = Array(1001, 2000, 2500, 3257))
  def tankContainer2(amount: Int): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)
    handler.fill(FluidAmount.BUCKET_WATER.setAmount(amount).toStack, IFluidHandler.FluidAction.EXECUTE)

    val container = stack.getContainerItem
    assertEquals(woodTank.itemBlock, container.getItem)
    val containerContent = RecipeInventoryUtil.getFluidHandler(container).getFluid
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(amount - 1000), containerContent)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(amount), handler.getFluid)
  }
}
