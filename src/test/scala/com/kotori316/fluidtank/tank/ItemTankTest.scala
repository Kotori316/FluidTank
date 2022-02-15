package com.kotori316.fluidtank.tank

import com.kotori316.fluidtank.FluidAmount
import net.minecraft.nbt.CompoundTag
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{Disabled, Nested, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.{MethodSource, ValueSource}

import scala.util.chaining.scalaUtilChainingOps

class ItemTankTest {

  @Test
  def createInstance(): Unit = {
    val tank = ItemTank.empty(1000L, Tiers.WOOD)
    assertEquals(FluidAmount.EMPTY, tank.getFluid)
    assertEquals(1000L, tank.getCapacity)
  }

  @Test
  def createInstance2(): Unit = {
    val tank = new ItemTank(FluidAmount.BUCKET_WATER, 2000L, Tiers.WOOD)
    assertEquals(FluidAmount.BUCKET_WATER, tank.getFluid)
    assertEquals(2000L, tank.getCapacity)
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.fluidtank.tank.ItemTankTest#nonEmptyFluids"))
  def testCanFillEmptyTank(fluid: FluidAmount): Unit = {
    val tank = ItemTank.empty(1000L, Tiers.WOOD)
    assertTrue(tank.canFillFluidType(fluid))
  }

  @Test
  def testCanFillWaterTank(): Unit = {
    val tank = new ItemTank(FluidAmount.BUCKET_WATER, 2000L, Tiers.WOOD)
    assertTrue(tank.canFillFluidType(FluidAmount.BUCKET_WATER))
    assertFalse(tank.canFillFluidType(FluidAmount.BUCKET_LAVA))
    assertFalse(tank.canFillFluidType(FluidAmount.EMPTY))
  }

  @Test
  def testCanFillLavaTank(): Unit = {
    val tank = new ItemTank(FluidAmount.BUCKET_LAVA, 2000L, Tiers.WOOD)
    assertFalse(tank.canFillFluidType(FluidAmount.BUCKET_WATER))
    assertTrue(tank.canFillFluidType(FluidAmount.BUCKET_LAVA))
    assertFalse(tank.canFillFluidType(FluidAmount.EMPTY))
  }

  @Nested
  class FillTest {
    @Test
    def fillToEmptySimulate(): Unit = {
      val tank = ItemTank.empty(1000L, Tiers.WOOD)
      val result = tank.fill(FluidAmount.BUCKET_WATER, doFill = false)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertEquals(FluidAmount.EMPTY, tank.getFluid)
    }

    @Test
    def fillToEmptyExecute(): Unit = {
      val tank = ItemTank.empty(1000L, Tiers.WOOD)
      val result = tank.fill(FluidAmount.BUCKET_WATER, doFill = true)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertEquals(FluidAmount.BUCKET_WATER, tank.getFluid)
    }

    @Test
    def fillEmpty(): Unit = {
      val tank = ItemTank.empty(1000L, Tiers.WOOD)
      val result = tank.fill(FluidAmount.EMPTY, doFill = false)
      assertTrue(result.isEmpty)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(1500L, 2000L, 5000L, 16000L))
    def fillOverCapacitySimulate(amount: Long): Unit = {
      val fill = FluidAmount.BUCKET_WATER.setAmount(amount)
      val tank = ItemTank.empty(1000L, Tiers.WOOD)
      val result = tank.fill(fill, doFill = false)
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(1000), result)
      assertEquals(FluidAmount.EMPTY, tank.getFluid)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(1500L, 2000L, 5000L, 16000L))
    def fillOverCapacityExecute(amount: Long): Unit = {
      val fill = FluidAmount.BUCKET_WATER.setAmount(amount)
      val tank = ItemTank.empty(1000L, Tiers.WOOD)
      val result = tank.fill(fill, doFill = true)
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(1000), result)
      assertEquals(FluidAmount.BUCKET_WATER, tank.getFluid)
    }

    @Test
    def respectMinParameter(): Unit = {
      val tank = ItemTank.empty(100L, Tiers.WOOD)
      val result = tank.fill(FluidAmount.BUCKET_WATER, doFill = false, min = 500)
      assertTrue(result.isEmpty)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.tank.ItemTankTest#nonEmptyFluids"))
    def fillToFullTank(fluid: FluidAmount): Unit = {
      val tank = new ItemTank(FluidAmount.BUCKET_WATER, 1000L, Tiers.WOOD)
      val result = tank.fill(fluid, doFill = true)
      assertTrue(result.isEmpty)
    }
  }

  @Nested
  class DrainTest {
    @Test
    def drainWaterSimulate(): Unit = {
      val tank = new ItemTank(FluidAmount.BUCKET_WATER, 2000L, Tiers.WOOD)
      val result = tank.drain(FluidAmount.BUCKET_WATER, doDrain = false)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertEquals(FluidAmount.BUCKET_WATER, tank.getFluid)
    }

    @Test
    def drainWaterExecute(): Unit = {
      val tank = new ItemTank(FluidAmount.BUCKET_WATER, 2000L, Tiers.WOOD)
      val result = tank.drain(FluidAmount.BUCKET_WATER, doDrain = true)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertTrue(tank.getFluid.isEmpty)
    }

    @Disabled
    @Test
    def drainWaterSimulate2(): Unit = {
      val tank = new ItemTank(FluidAmount.BUCKET_WATER, 2000L, Tiers.WOOD)
      val result = tank.drain(FluidAmount.EMPTY.setAmount(1000), doDrain = false)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertEquals(FluidAmount.BUCKET_WATER, tank.getFluid)
    }

    @Disabled
    @Test
    def drainWaterExecute2(): Unit = {
      val tank = new ItemTank(FluidAmount.BUCKET_WATER, 2000L, Tiers.WOOD)
      val result = tank.drain(FluidAmount.EMPTY.setAmount(1000), doDrain = true)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertTrue(tank.getFluid.isEmpty)
    }

    @ParameterizedTest
    @ValueSource(booleans = Array(true, false))
    def drainLavaFromWaterTank(doDrain: Boolean): Unit = {
      val tank = new ItemTank(FluidAmount.BUCKET_WATER, 2000L, Tiers.WOOD)
      val result = tank.drain(FluidAmount.BUCKET_LAVA, doDrain = doDrain)
      assertTrue(result.isEmpty)
      assertEquals(FluidAmount.BUCKET_WATER, tank.getFluid)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.tank.ItemTankTest#nonEmptyFluids"))
    def drainFromEmptyTank1(fluid: FluidAmount): Unit = {
      val tank = ItemTank.empty(2000L, Tiers.WOOD)
      val result = tank.drain(fluid, doDrain = true)
      assertTrue(result.isEmpty)
      assertTrue(tank.getFluid.isEmpty)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(1500L, 2000L, 5000L, 16000L))
    def drainOverSimulate(amount: Long): Unit = {
      val tank = new ItemTank(FluidAmount.BUCKET_LAVA, 2000L, Tiers.WOOD)
      val result = tank.drain(FluidAmount.BUCKET_LAVA.setAmount(amount), doDrain = false)
      assertEquals(FluidAmount.BUCKET_LAVA, result)
      assertEquals(FluidAmount.BUCKET_LAVA, tank.getFluid)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(1500L, 2000L, 5000L, 16000L))
    def drainOverExecute(amount: Long): Unit = {
      val tank = new ItemTank(FluidAmount.BUCKET_LAVA, 2000L, Tiers.WOOD)
      val result = tank.drain(FluidAmount.BUCKET_LAVA.setAmount(amount), doDrain = true)
      assertEquals(FluidAmount.BUCKET_LAVA, result)
      assertTrue(tank.getFluid.isEmpty)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(1500L, 2000L, 5000L, 16000L))
    def respectMin(amount: Long): Unit = {
      val tank = new ItemTank(FluidAmount.BUCKET_LAVA, 2000L, Tiers.WOOD)
      val result = tank.drain(FluidAmount.BUCKET_LAVA.setAmount(amount), doDrain = false, min = amount)
      assertTrue(result.isEmpty)
    }
  }

  @Nested
  class SerializeTest {
    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.tank.ItemTankTest#nonEmptyFluids"))
    def cycle1(fluid: FluidAmount): Unit = {
      val tank = new ItemTank(fluid, Tiers.STONE.amount, Tiers.STONE)
      val tag = tank.createTag("").orNull

      val deserialized = ItemTank.from(tag, Tiers.Invalid)
      assertEquals(fluid, deserialized.getFluid)
      assertEquals(Tiers.STONE.amount, deserialized.getCapacity)
    }

    @Test
    def cycle2(): Unit = {
      val tank = ItemTank.empty(4000, Tiers.WOOD)
      val tag = tank.createTag("")
      assertTrue(tag.isEmpty)

      val deserialized = ItemTank.from(tag.orNull, Tiers.Invalid)
      assertTrue(deserialized.getFluid.isEmpty)
      assertEquals(0, deserialized.getCapacity)
    }

    @Test
    def fromEmptyTag1(): Unit = {
      val deserialized = ItemTank.from(null, Tiers.Invalid)
      assertEquals(0, deserialized.getCapacity)
      assertTrue(deserialized.getFluid.isEmpty)
    }

    @Test
    def fromEmptyTag2(): Unit = {
      val deserialized = ItemTank.from(new CompoundTag(), Tiers.Invalid)
      assertEquals(0, deserialized.getCapacity)
      assertTrue(deserialized.getFluid.isEmpty)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.tank.ItemTankTest#nonEmptyFluids"))
    def fromNonCapacityTag(fluid: FluidAmount): Unit = {
      val tag = new CompoundTag().tap(
        _.put(TankBlock.NBT_Tank, fluid.write(new CompoundTag()))
      )

      val deserialized = ItemTank.from(tag, Tiers.GOLD)
      assertEquals(fluid, deserialized.getFluid)
      assertEquals(Tiers.GOLD.amount, deserialized.getCapacity)
    }
  }
}

object ItemTankTest {
  def nonEmptyFluids(): Array[FluidAmount] = {
    val seq = for {
      fluid <- Seq(FluidAmount.BUCKET_WATER, FluidAmount.BUCKET_LAVA)
      amount <- Seq(10L, 500L, FluidAmount.AMOUNT_BUCKET, 2000L, 5000L)
    } yield fluid.setAmount(amount)
    seq.toArray
  }
}
