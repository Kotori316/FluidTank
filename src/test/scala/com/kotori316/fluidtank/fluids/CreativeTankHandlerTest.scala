package com.kotori316.fluidtank.fluids

import com.kotori316.fluidtank.BeforeAllTest
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertTrue}
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.{DisplayName, Nested, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.{MethodSource, ValueSource}

class CreativeTankHandlerTest extends BeforeAllTest {

  @Nested
  class ForgeHandlerTest extends BeforeAllTest {
    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeyAmount"))
    def fillCreativeHandlerSimulation(amount: Long, key: FluidKey): Unit = {
      val h: TankHandler = new CreativeTankHandler
      val filled = h.fill(key.toAmount(amount), IFluidHandler.FluidAction.SIMULATE)
      if (key.isEmpty) {
        val expected = key.toAmount(0)
        assertEquals(expected, filled, "If fluid is empty, filled amount must be 0.")
      } else {
        val expected = key.toAmount(amount)
        assertEquals(expected, filled, "If fluid is not empty, all fluid must be filled.")
      }
      assertTrue(h.getTank.fluidAmount.isEmpty, s"Simulation shouldn't change the content. ${h.getTank}")
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeyAmount"))
    def fillCreativeHandlerExecution(amount: Long, key: FluidKey): Unit = {
      val h: TankHandler = new CreativeTankHandler
      val fluidAmount = key.toAmount(amount)
      val filled = h.fill(fluidAmount, IFluidHandler.FluidAction.EXECUTE)
      if (fluidAmount.isEmpty) {
        val expected = key.toAmount(0)
        assertEquals(expected, filled, "If fluid is empty, filled amount must be 0.")
      } else {
        assertEquals(fluidAmount, filled, "If fluid is not empty, all fluid must be filled.")
        assertEquals(key, FluidKey.from(h.getTank.fluidAmount), s"Execution must change the content. ${h.getTank}")
        assertEquals(Long.MaxValue, h.getTank.fluidAmount.amount, s"Inserting to creative tank must fill all tanks to max. ${h.getTank}")
      }
    }

    @Test
    def drainCreativeHandler(): Unit = {
      val h: TankHandler = new CreativeTankHandler
      assertAll(
        () => assertTrue(h.drain(1000, IFluidHandler.FluidAction.SIMULATE).isEmpty),
        () => assertTrue(h.drain(FluidAmount.EMPTY.toStack, IFluidHandler.FluidAction.SIMULATE).isEmpty),
        () => assertTrue(h.drain(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.SIMULATE).isEmpty),
      )
      h.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
      assertAll(
        Range(1, 10).map(i => Math.pow(10, i).toLong).map(FluidAmount.BUCKET_WATER.setAmount)
          .map[Executable](f => () => assertEquals(f, FluidAmount.fromStack(h.drain(f.toStack, IFluidHandler.FluidAction.SIMULATE)): FluidAmount, s"Drain $f Simulation")): _*
      )
      assertAll(
        Range(1, 10).map(i => Math.pow(10, i).toLong).map(FluidAmount.BUCKET_WATER.setAmount)
          .map[Executable](f => () => assertEquals(f, FluidAmount.fromStack(h.drain(f.toStack, IFluidHandler.FluidAction.EXECUTE)): FluidAmount, s"Drain $f Execution")): _*
      )
      assertAll(
        Range(1, 10).map(i => Math.pow(10, i).toLong).map(FluidAmount.BUCKET_LAVA.setAmount)
          .map[Executable](f => () => assertTrue(FluidAmount.fromStack(h.drain(f.toStack, IFluidHandler.FluidAction.SIMULATE)).isEmpty, s"Drain $f Simulation")): _*
      )
    }
  }

  @Nested
  class OperationTest extends BeforeAllTest {
    @Test
    @DisplayName("Drain Empty from Creative")
    def drainEmptyFromCreativeHandler(): Unit = {
      val h: TankHandler = new CreativeTankHandler
      h.fill(FluidAmount.BUCKET_WATER, IFluidHandler.FluidAction.EXECUTE)
      val drained = h.drain(FluidAmount.EMPTY.setAmount(1000L), IFluidHandler.FluidAction.SIMULATE)
      assertEquals(FluidAmount.BUCKET_WATER, drained)
      assertTrue(h.getTank.amount > 0)
    }

    @Test
    def emptyCreativeHandler(): Unit = {
      val h: TankHandler = new CreativeTankHandler
      val emptySimulate = FluidAmount.fromStack(h.drain(FluidStack.EMPTY, IFluidHandler.FluidAction.SIMULATE))
      val zeroSimulate = FluidAmount.fromStack(h.drain(0, IFluidHandler.FluidAction.SIMULATE))
      assertAll(
        () => assertEquals(0, h.fill(FluidStack.EMPTY, IFluidHandler.FluidAction.SIMULATE), "Filling EMPTY"),
        () => assertEquals(0, h.fill(FluidStack.EMPTY, IFluidHandler.FluidAction.EXECUTE), "Filling EMPTY"),
        () => assertTrue(h.getTank.fluidAmount.isEmpty, "Fill with 0 cause no changes."),
        () => assertEquals(FluidAmount.EMPTY, emptySimulate, "Drain empty fluid to get EMPTY."),
        () => assertEquals(FluidAmount.EMPTY, zeroSimulate, "Drain 0 fluid to get EMPTY."),
      )
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeysNonEmpty"))
    def fillToEmptyTankSimulation(key: FluidKey): Unit = {
      val handler = new CreativeTankHandler
      assertTrue(handler.getTank.isEmpty)
      val toFill = key.toAmount(1000L)
      val filled = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE)
      assertEquals(toFill, filled)
      assertTrue(handler.getTank.isEmpty)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeysNonEmpty"))
    def fillToEmptyTankExecution(key: FluidKey): Unit = {
      val handler = new CreativeTankHandler
      assertTrue(handler.getTank.isEmpty)
      val toFill = key.toAmount(1000L)
      val filled = handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE)
      assertEquals(toFill, filled)
      assertEquals(handler.getTank.capacity, handler.getTank.fluidAmount.amount, "Must be filled to full.")
      assertEquals(key, FluidKey.from(handler.getTank.fluidAmount), "Must be equal to filled fluid.")
    }

    @ParameterizedTest
    @ValueSource(longs = Array(1L, 100L, 1000L, Int.MaxValue, Int.MaxValue + 10L, Long.MaxValue))
    def fillToWaterTankSimulation(amount: Long): Unit = {
      val handler = new CreativeTankHandler
      handler.setTank(Tank(FluidAmount.BUCKET_WATER, 4000L))
      val toFill = FluidAmount.BUCKET_WATER.setAmount(amount)

      val filled = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE)
      assertEquals(toFill, filled)
      assertEquals(Tank(FluidAmount.BUCKET_WATER, 4000L), handler.getTank, "This is simulation.")
    }

    @ParameterizedTest
    @ValueSource(longs = Array(1L, 100L, 1000L, Int.MaxValue, Int.MaxValue + 10L, Long.MaxValue))
    def fillLavaToWaterTankExecution(amount: Long): Unit = {
      val handler = new CreativeTankHandler
      handler.setTank(Tank(FluidAmount.BUCKET_WATER, 4000L))
      val toFill = FluidAmount.BUCKET_LAVA.setAmount(amount)

      val filled = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE)
      assertTrue(filled.isEmpty, "Filling lava must be rejected.")
      assertEquals(Tank(FluidAmount.BUCKET_WATER, 4000L), handler.getTank,
        "Touching filled creative tank will not change the content.")
    }

    @Test
    def drainFromEmpty1(): Unit = {
      val handler = new CreativeTankHandler
      val toDrain = FluidAmount.BUCKET_WATER
      val drained = handler.drain(toDrain, IFluidHandler.FluidAction.SIMULATE)
      assertTrue(drained.isEmpty)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(0L, 1L, 100L, 1000L, Int.MaxValue, Int.MaxValue + 10L, Long.MaxValue))
    def drainFromEmpty2(amount: Long): Unit = {
      val handler = new CreativeTankHandler
      val toDrain = FluidAmount.EMPTY.setAmount(amount)
      val drained = handler.drain(toDrain, IFluidHandler.FluidAction.SIMULATE)
      assertTrue(drained.isEmpty)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(0L, 1L, 100L, 1000L, Int.MaxValue, Int.MaxValue + 10L, Long.MaxValue))
    def drainWaterFromWaterTank(amount: Long): Unit = {
      val handler = new CreativeTankHandler
      handler.setTank(Tank(FluidAmount.BUCKET_WATER, 4000L))
      val toDrain = FluidAmount.BUCKET_WATER.setAmount(amount)

      val drained = handler.drain(toDrain, IFluidHandler.FluidAction.SIMULATE)
      assertEquals(toDrain, drained)
    }

    @ParameterizedTest
    @ValueSource(longs = Array(0L, 1L, 100L, 1000L, Int.MaxValue, Int.MaxValue + 10L, Long.MaxValue))
    def drainLavaFromWaterTank(amount: Long): Unit = {
      val handler = new CreativeTankHandler
      handler.setTank(Tank(FluidAmount.BUCKET_WATER, 4000L))
      val toDrain = FluidAmount.BUCKET_LAVA.setAmount(amount)

      val drained = handler.drain(toDrain, IFluidHandler.FluidAction.SIMULATE)
      assertTrue(drained.isEmpty)
    }
  }
}
