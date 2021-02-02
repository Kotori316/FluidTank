package com.kotori316.fluidtank.fluid

import com.kotori316.fluidtank.DynamicSerializable._
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.items.{ItemBlockTank, TankItemFluidHandler}
import com.kotori316.fluidtank.tiles.{Tiers, TileTankNoDisplay}
import com.kotori316.fluidtank.{BeforeAllTest, DynamicSerializable, FluidTank, ModObjects}
import com.mojang.serialization.{Dynamic, JsonOps}
import net.minecraft.item.ItemStack
import net.minecraft.util.{JSONUtils, ResourceLocation}
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

//noinspection DuplicatedCode It's a test.
private[fluid] class ItemFluidHandlerTest extends BeforeAllTest {
  private def woodTank = ModObjects.blockTanks.head

  @Test
  def woodTankIsWood(): Unit = {
    assertEquals(new ResourceLocation(FluidTank.modID, "tank_wood"), woodTank.getRegistryName)
  }

  @Test
  def emptyTank(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = new TankItemFluidHandler(stack.getItem.asInstanceOf[ItemBlockTank], stack)

    assertEquals(Tiers.WOOD, handler.tiers)
    assertEquals(FluidAmount.EMPTY, handler.getFluid)
    assertEquals(1000, handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.SIMULATE))
  }

  @Test
  def fillInEmpty(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = new TankItemFluidHandler(stack.getItem.asInstanceOf[ItemBlockTank], stack)

    handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.BUCKET_WATER, handler.getFluid)

    handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(2000L), handler.getFluid)
  }

  @Test
  def filledTag(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = new TankItemFluidHandler(stack.getItem.asInstanceOf[ItemBlockTank], stack)

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
    val handler = new TankItemFluidHandler(stack.getItem.asInstanceOf[ItemBlockTank], stack)

    assertEquals(0, handler.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE))
    assertEquals(FluidAmount.EMPTY, handler.getFluid)
    assertEquals(FluidStack.EMPTY, handler.drain(1000, IFluidHandler.FluidAction.EXECUTE))
    assertEquals(FluidAmount.EMPTY, handler.getFluid)
  }

  @Test
  def serializeTest(): Unit = {
    val stack = new ItemStack(woodTank)
    val handler = new TankItemFluidHandler(stack.getItem.asInstanceOf[ItemBlockTank], stack)
    handler.fill(FluidAmount.BUCKET_WATER.setAmount(4000L).toStack, IFluidHandler.FluidAction.EXECUTE)

    val stackTagJson = handler.createTag.toJson
    assertEquals(Tiers.WOOD.toString.toLowerCase, JSONUtils.getString(stackTagJson.getAsJsonObject, "tier"))
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(4000L),
      DynamicSerializable[FluidAmount].deserialize(new Dynamic(JsonOps.INSTANCE, stackTagJson.getAsJsonObject.get("tank"))))
  }
}
