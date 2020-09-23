package com.kotori316.fluidtank.test

import com.kotori316.fluidtank.fluids.{FluidAmount, Tank, TankHandler}
import net.minecraft.fluid.Fluids
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test

//noinspection DuplicatedCode It's a test!
class TankHandlerTest {
  @Test
  def fillWaterToEmpty(): Unit = {
    val tank = TankHandler(4000L)
    val filled = tank.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE)
    assertEquals(4000, filled)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(4000), tank.getTank.fluidAmount)
  }

  @Test
  def fillWaterToEmpty2(): Unit = {
    val tank = TankHandler(Tank(FluidAmount.BUCKET_LAVA.setAmount(0), 4000L))
    locally {
      val filled = tank.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.SIMULATE)
      assertEquals(4000, filled)
    }
    locally {
      val filled = tank.fill(new FluidStack(Fluids.LAVA, 4000), IFluidHandler.FluidAction.SIMULATE)
      assertEquals(4000, filled)
    }
    locally {
      val filled = tank.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.EXECUTE)
      assertEquals(4000, filled)
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(4000), tank.getTank.fluidAmount)
    }
  }

  @Test
  def fillLavaToEmpty(): Unit = {
    val tank = TankHandler(4000L)
    val filled = tank.fill(new FluidStack(Fluids.LAVA, 4000), IFluidHandler.FluidAction.EXECUTE)
    assertEquals(4000, filled)
    assertEquals(FluidAmount.BUCKET_LAVA.setAmount(4000), tank.getTank.fluidAmount)
  }

  @Test
  def fillEmptyToEmpty(): Unit = {
    val tank = TankHandler(4000L)
    val filled = tank.fill(new FluidStack(Fluids.EMPTY, 4000), IFluidHandler.FluidAction.EXECUTE)
    assertEquals(0, filled)
    assertEquals(FluidAmount.EMPTY, tank.getTank.fluidAmount)
  }

  @Test
  def fillSimulation(): Unit = {
    val tank = TankHandler(4000L)
    val before = tank.getTank
    locally {
      val filled = tank.fill(new FluidStack(Fluids.LAVA, 4000), IFluidHandler.FluidAction.SIMULATE)
      assertEquals(4000, filled)
      assertEquals(FluidAmount.EMPTY, tank.getTank.fluidAmount)
      assertEquals(before, tank.getTank)
    }
    locally {
      val filled = tank.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.SIMULATE)
      assertEquals(4000, filled)
      assertEquals(FluidAmount.EMPTY, tank.getTank.fluidAmount)
      assertEquals(before, tank.getTank)
    }
    locally {
      val filled = tank.fill(new FluidStack(Fluids.EMPTY, 4000), IFluidHandler.FluidAction.SIMULATE)
      assertEquals(0, filled)
      assertEquals(FluidAmount.EMPTY, tank.getTank.fluidAmount)
      assertEquals(before, tank.getTank)
    }
  }

  @Test
  def fillWaterToWater(): Unit = {
    val tank = new TankHandler
    tank.setTank(Tank(FluidAmount.BUCKET_WATER, 16000L))

    val filled = tank.fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE)
    assertEquals(3000, filled)
    assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(4000L), 16000L), tank.getTank)
  }

  @Test
  def fillWaterToWater2(): Unit = {
    val tank = new TankHandler
    tank.setTank(Tank(FluidAmount.BUCKET_WATER, 16000L)) // Water 1000, Air 15000

    val filled = tank.fill(new FluidStack(Fluids.WATER, 17000), IFluidHandler.FluidAction.EXECUTE)
    assertEquals(15000, filled)
    assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(16000L), 16000L), tank.getTank)
  }

  @Test
  def fillFail1(): Unit = {
    val tank = new TankHandler
    val before = Tank(FluidAmount.BUCKET_WATER, 16000L)
    tank.setTank(before)

    {
      val filled = tank.fill(new FluidStack(Fluids.LAVA, 3000), IFluidHandler.FluidAction.SIMULATE)
      assertEquals(0, filled)
      assertEquals(before, tank.getTank)
    }
    {
      val filled = tank.fill(new FluidStack(Fluids.LAVA, 3000), IFluidHandler.FluidAction.EXECUTE)
      assertEquals(0, filled)
      assertEquals(before, tank.getTank)
    }
  }

  @Test
  def fillFail2(): Unit = {
    val tank = new TankHandler
    val before = Tank(FluidAmount.BUCKET_WATER.setAmount(16000L), 16000L)
    tank.setTank(before)

    val filled = tank.fill(new FluidStack(Fluids.WATER, 17000), IFluidHandler.FluidAction.EXECUTE)
    assertEquals(0, filled)
    assertEquals(before, tank.getTank)
  }

  @Test
  def drainEmpty(): Unit = {
    val tank = TankHandler(4000L)
    val drained = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE)
    assertTrue(drained.isEmpty)
    assertTrue(tank.getTank.fluidAmount.isEmpty)
  }

  @Test
  def drainNormal1(): Unit = {
    val tank = new TankHandler
    tank.setTank(Tank(FluidAmount.BUCKET_WATER, 16000L))

    val drained = tank.drain(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.fromStack(new FluidStack(Fluids.WATER, 1000)), FluidAmount.fromStack(drained))
    assertTrue(tank.getTank.fluidAmount.isEmpty)
  }

  @Test
  def drainNormal2(): Unit = {
    val tank = new TankHandler
    tank.setTank(Tank(FluidAmount.BUCKET_WATER, 16000L))

    val drained = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.fromStack(new FluidStack(Fluids.WATER, 1000)), FluidAmount.fromStack(drained))
    assertTrue(tank.getTank.fluidAmount.isEmpty)
  }

  @Test
  def drainNormal3(): Unit = {
    val tank = new TankHandler
    tank.setTank(Tank(FluidAmount.BUCKET_WATER.setAmount(4000L), 16000L))

    val drained = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.fromStack(new FluidStack(Fluids.WATER, 1000)), FluidAmount.fromStack(drained))
    assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(3000), 16000), tank.getTank)
  }

  @Test
  def drainFail1(): Unit = {
    val tank = TankHandler(4000L)
    val before = tank.getTank
    locally {
      val drained = tank.drain(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE)
      assertTrue(drained.isEmpty)
      assertEquals(before, tank.getTank)
    }
    locally {
      val drained = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE)
      assertTrue(drained.isEmpty)
      assertEquals(before, tank.getTank)
    }
  }

  @Test
  def drainFail2(): Unit = {
    val tank = new TankHandler
    val before = Tank(FluidAmount.BUCKET_WATER.setAmount(4000L), 16000L)
    tank.setTank(before)

    val drained = tank.drain(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE)
    assertTrue(drained.isEmpty)
    assertEquals(before, tank.getTank)
  }
}
