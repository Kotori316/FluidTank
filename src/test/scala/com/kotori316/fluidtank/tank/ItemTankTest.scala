package com.kotori316.fluidtank.tank

import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.fluids.{FluidAction, FluidAmount, Tank, TankHandler}
import com.kotori316.fluidtank.items.TankItemFluidHandler
import com.kotori316.fluidtank.tiles.Tier
import net.minecraft.world.item.ItemStack
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{Disabled, Nested, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.{EnumSource, MethodSource, ValueSource}

class ItemTankTest {

  @Test
  @Disabled("Creating item instance is not allowed in Fabric test.")
  def createInstance(): Unit = {
    val tank = new TankItemFluidHandler(Tier.WOOD, new ItemStack(ModObjects.tierToBlock(Tier.WOOD)))
    assertEquals(FluidAmount.EMPTY, tank.getFluid)
    assertEquals(4000L, tank.getCapacity)
  }

  @Test
  def createInstance2(): Unit = {
    val tank = TankHandler(Tank(FluidAmount.BUCKET_WATER, 2000L))
    assertEquals(FluidAmount.BUCKET_WATER, tank.getTank.fluidAmount)
    assertEquals(2000L, tank.getTank.capacity)
  }

  @Nested
  class FillTest {
    @Test
    def fillToEmptySimulate(): Unit = {
      val tank = TankHandler(Tank(FluidAmount.EMPTY, 1000L))
      val result = tank.fill(FluidAmount.BUCKET_WATER, FluidAction.SIMULATE)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertEquals(FluidAmount.EMPTY, tank.getTank.fluidAmount)
    }

    @Test
    def fillToEmptyExecute(): Unit = {
      val tank = TankHandler(Tank(FluidAmount.EMPTY, 1000L))
      val result = tank.fill(FluidAmount.BUCKET_WATER, FluidAction.EXECUTE)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertEquals(FluidAmount.BUCKET_WATER, tank.getTank.fluidAmount)
    }

    @Test
    def fillEmpty(): Unit = {
      val tank = TankHandler(Tank(FluidAmount.EMPTY, 1000L))
      val result = tank.fill(FluidAmount.EMPTY, FluidAction.SIMULATE)
      assertTrue(result.isEmpty)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(1500L, 2000L, 5000L, 16000L))
    def fillOverCapacitySimulate(amount: Long): Unit = {
      val fill = FluidAmount.BUCKET_WATER.setAmount(amount)
      val tank = TankHandler(Tank(FluidAmount.EMPTY, 1000L))
      val result = tank.fill(fill, FluidAction.SIMULATE)
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(1000), result)
      assertEquals(FluidAmount.EMPTY, tank.getTank.fluidAmount)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(1500L, 2000L, 5000L, 16000L))
    def fillOverCapacityExecute(amount: Long): Unit = {
      val fill = FluidAmount.BUCKET_WATER.setAmount(amount)
      val tank = TankHandler(Tank(FluidAmount.EMPTY, 1000L))
      val result = tank.fill(fill, FluidAction.EXECUTE)
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(1000), result)
      assertEquals(FluidAmount.BUCKET_WATER, tank.getTank.fluidAmount)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.tank.ItemTankTest#nonEmptyFluids"))
    def fillToFullTank(fluid: FluidAmount): Unit = {
      val tank = TankHandler(Tank(FluidAmount.BUCKET_WATER, 1000L))
      val result = tank.fill(fluid, FluidAction.EXECUTE)
      assertTrue(result.isEmpty)
    }
  }

  @Nested
  class DrainTest {
    @Test
    def drainWaterSimulate(): Unit = {
      val tank = TankHandler(Tank(FluidAmount.BUCKET_WATER, 2000L))
      val result = tank.drain(FluidAmount.BUCKET_WATER, FluidAction.SIMULATE)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertEquals(FluidAmount.BUCKET_WATER, tank.getTank.fluidAmount)
    }

    @Test
    def drainWaterExecute(): Unit = {
      val tank = TankHandler(Tank(FluidAmount.BUCKET_WATER, 2000L))
      val result = tank.drain(FluidAmount.BUCKET_WATER, FluidAction.EXECUTE)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertTrue(tank.getTank.fluidAmount.isEmpty)
    }

    @Disabled
    @Test
    def drainWaterSimulate2(): Unit = {
      val tank = TankHandler(Tank(FluidAmount.BUCKET_WATER, 2000L))
      val result = tank.drain(FluidAmount.EMPTY.setAmount(1000), FluidAction.SIMULATE)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertEquals(FluidAmount.BUCKET_WATER, tank.getTank.fluidAmount)
    }

    @Disabled
    @Test
    def drainWaterExecute2(): Unit = {
      val tank = TankHandler(Tank(FluidAmount.BUCKET_WATER, 2000L))
      val result = tank.drain(FluidAmount.EMPTY.setAmount(1000), FluidAction.EXECUTE)
      assertEquals(FluidAmount.BUCKET_WATER, result)
      assertTrue(tank.getTank.fluidAmount.isEmpty)
    }

    @ParameterizedTest
    @EnumSource
    def drainLavaFromWaterTank(mode: FluidAction): Unit = {
      val tank = TankHandler(Tank(FluidAmount.BUCKET_WATER, 2000L))
      val result = tank.drain(FluidAmount.BUCKET_LAVA, mode)
      assertTrue(result.isEmpty)
      assertEquals(FluidAmount.BUCKET_WATER, tank.getTank.fluidAmount)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.tank.ItemTankTest#nonEmptyFluids"))
    def drainFromEmptyTank1(fluid: FluidAmount): Unit = {
      val tank = TankHandler(Tank(FluidAmount.EMPTY, 2000L))
      val result = tank.drain(fluid, FluidAction.EXECUTE)
      assertTrue(result.isEmpty)
      assertTrue(tank.getTank.fluidAmount.isEmpty)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(1500L, 2000L, 5000L, 16000L))
    def drainOverSimulate(amount: Long): Unit = {
      val tank = TankHandler(Tank(FluidAmount.BUCKET_LAVA, 2000L))
      val result = tank.drain(FluidAmount.BUCKET_LAVA.setAmount(amount), FluidAction.SIMULATE)
      assertEquals(FluidAmount.BUCKET_LAVA, result)
      assertEquals(FluidAmount.BUCKET_LAVA, tank.getTank.fluidAmount)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(1500L, 2000L, 5000L, 16000L))
    def drainOverExecute(amount: Long): Unit = {
      val tank = TankHandler(Tank(FluidAmount.BUCKET_LAVA, 2000L))
      val result = tank.drain(FluidAmount.BUCKET_LAVA.setAmount(amount), FluidAction.EXECUTE)
      assertEquals(FluidAmount.BUCKET_LAVA, result)
      assertTrue(tank.getTank.fluidAmount.isEmpty)
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
