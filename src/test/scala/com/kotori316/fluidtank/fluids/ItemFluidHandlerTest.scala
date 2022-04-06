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

//noinspection DuplicatedCode It's a test.
object ItemFluidHandlerTest extends BeforeAllTest {
  private final val woodTank = ModObjects.tierToBlock(Tier.WOOD)

  @Test
  def woodTankIsWood(): Unit = {
    assertEquals(new ResourceLocation(FluidTank.modID, "tank_wood"), woodTank.getRegistryName)
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
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(2000L), handler.getFluid)
  }

  @Test
  def makeEmpty(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = RecipeInventoryUtil.getFluidHandler(stack)

    handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
    assertTrue(stack.hasTag)
    val drained = handler.drain(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.BUCKET_WATER, FluidAmount.fromStack(drained))
    assertFalse(stack.hasTag)
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

  @Test
  @DisplayName("Check tag type of empty tank")
  def checkType1(): Unit = {
    val handler = RecipeInventoryUtil.getFluidHandler(new ItemStack(woodTank))
    val tag = handler.createTag

    val tierTag = tag.get(TileTank.NBT_Tier)
    assertEquals(handler.tier, Tier.fromNBT(tierTag))
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
    assertEquals(handler.tier, Tier.fromNBT(tierTag))
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
}
