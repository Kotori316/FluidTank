package com.kotori316.fluidtank.fluids

import com.kotori316.fluidtank.recipes.RecipeInventoryUtil
import com.kotori316.fluidtank.tiles.{Tier, TileTank}
import com.kotori316.fluidtank.{BeforeAllTest, FluidTank, ModObjects}
import net.minecraft.nbt.{CompoundTag, LongTag}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{DisplayName, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

//noinspection DuplicatedCode It's a test.
class ItemFluidHandlerTest extends BeforeAllTest {
  private final val woodTank = ModObjects.tierToBlock(Tier.WOOD)

  @Test
  def woodTankIsWood(): Unit = {
    assertEquals(new ResourceLocation(FluidTank.modID, "tank_wood"), woodTank.registryName)
  }

  @Test
  def emptyTank(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)

    assertEquals(Tier.WOOD, handler.tier)
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
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(2000L): FluidAmount, handler.getFluid)
  }

  @Test
  def makeEmpty(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)

    handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
    assertTrue(stack.hasTag)
    val drained = handler.drain(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.BUCKET_WATER, FluidAmount.fromStack(drained): FluidAmount)
    assertFalse(stack.hasTag)
  }

  @Test
  def filledTag(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)

    handler.fill(FluidAmount.BUCKET_WATER.setAmount(2000L).toStack, IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(2000L): FluidAmount, handler.getFluid)

    val tag = stack.getTag
    assertNotNull(tag)
    val childTag = tag.getCompound("BlockEntityTag")
    assertFalse(childTag.isEmpty)
    val fluidInTank = FluidAmount.fromNBT(childTag.getCompound(TileTank.NBT_Tank))
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(2000L): FluidAmount, fluidInTank)
  }

  @Test
  def stackedTankTest(): Unit = {
    val stack = new ItemStack(woodTank, 3)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)

    assertEquals(0, handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE))
    assertEquals(FluidAmount.EMPTY, handler.getFluid)
    assertEquals(FluidStack.EMPTY, handler.drain(1000, IFluidHandler.FluidAction.EXECUTE): FluidStack)
    assertEquals(FluidAmount.EMPTY, handler.getFluid)
  }

  @Test
  def serializeTest(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)
    handler.fill(FluidAmount.BUCKET_WATER.setAmount(4000L).toStack, IFluidHandler.FluidAction.EXECUTE)

    val stackTag = handler.createTag
    assertEquals(Tier.WOOD.toString.toLowerCase, stackTag.getString("tier"): String)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(4000L), FluidAmount.fromNBT(stackTag.getCompound("tank")))
  }

  @Test
  @DisplayName("Check tag type of empty tank")
  def checkType1(): Unit = {
    val handler = RecipeInventoryUtil.getFluidHandler(new ItemStack(woodTank))
    val tag = handler.createTag

    val tierTag = tag.get(TileTank.NBT_Tier)
    assertEquals(handler.tier, Tier.fromNBT(tierTag): Tier)
    val tankTag = tag.get(TileTank.NBT_Tank)
    assertEquals(CompoundTag.TYPE, tankTag.getType)

    val capacityTag = tankTag.asInstanceOf[CompoundTag].get(TileTank.NBT_Capacity)
    assertEquals(LongTag.TYPE, capacityTag.getType)
  }

  @Test
  @DisplayName("Check tag type of water tank")
  def checkType2(): Unit = {
    val handler = RecipeInventoryUtil.getFluidHandler(new ItemStack(woodTank))
    handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
    val tag = handler.createTag

    val tierTag = tag.get(TileTank.NBT_Tier)
    assertEquals(handler.tier, Tier.fromNBT(tierTag): Tier)
    val tankTag = tag.get(TileTank.NBT_Tank)
    assertEquals(CompoundTag.TYPE, tankTag.getType)

    val capacityTag = tankTag.asInstanceOf[CompoundTag].get(TileTank.NBT_Capacity)
    assertEquals(LongTag.TYPE, capacityTag.getType)
  }

  @Test
  @DisplayName("Get capability via getter")
  def testGetCapability1(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
    assertTrue(handler.isPresent)
  }

  @Test
  @DisplayName("Get capability even if stack size is over 2")
  def testGetCapability2(): Unit = {
    val stack = new ItemStack(woodTank, 2)
    val handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
    assertTrue(handler.isPresent)
  }

  @ParameterizedTest
  @ValueSource(ints = Array(1, 10, 100, 500, 999, 1000))
  def tankContainer1(amount: Int): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)
    handler.fill(FluidAmount.BUCKET_WATER.setAmount(amount).toStack, IFluidHandler.FluidAction.EXECUTE)

    val container = stack.getCraftingRemainingItem
    assertEquals(woodTank.itemBlock, container.getItem)
    val containerContent = RecipeInventoryUtil.getFluidHandler(container).getFluid
    assertTrue(containerContent.isEmpty)
    assertEquals(0L, containerContent.amount)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(amount): FluidAmount, handler.getFluid)
  }

  @ParameterizedTest
  @ValueSource(ints = Array(1001, 2000, 2500, 3257))
  def tankContainer2(amount: Int): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)
    handler.fill(FluidAmount.BUCKET_WATER.setAmount(amount).toStack, IFluidHandler.FluidAction.EXECUTE)

    val container = stack.getCraftingRemainingItem
    assertEquals(woodTank.itemBlock, container.getItem)
    val containerContent = RecipeInventoryUtil.getFluidHandler(container).getFluid
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(amount - 1000): FluidAmount, containerContent)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(amount): FluidAmount, handler.getFluid)
  }
}
