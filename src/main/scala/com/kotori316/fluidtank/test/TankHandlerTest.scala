package com.kotori316.fluidtank.test

import com.kotori316.fluidtank.fluids.{CreativeTankHandler, EmptyTankHandler, FluidAmount, Tank, TankHandler, VoidTankHandler}
import net.minecraft.fluid.Fluids
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

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

  @Test
  def emptyHandler(): Unit = {
    val h: TankHandler = EmptyTankHandler
    locally {
      val filled = h.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.SIMULATE)
      assertEquals(0, filled)
    }
    locally {
      val filled = h.fill(FluidAmount.BUCKET_LAVA.toStack, IFluidHandler.FluidAction.SIMULATE)
      assertEquals(0, filled)
    }
    locally {
      val drained = h.drain(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.SIMULATE)
      assertTrue(drained.isEmpty)
    }
    locally {
      val drained = h.drain(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.SIMULATE)
      assertTrue(drained.isEmpty)
    }
    locally {
      val drained = h.drain(1000, IFluidHandler.FluidAction.SIMULATE)
      assertTrue(drained.isEmpty)
    }
  }

  @Test
  def voidHandler(): Unit = {
    val h: TankHandler = VoidTankHandler
    locally {
      val filled = h.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.SIMULATE)
      assertEquals(1000, filled)
    }
    locally {
      val filled = h.fill(FluidAmount.BUCKET_LAVA.toStack, IFluidHandler.FluidAction.SIMULATE)
      assertEquals(1000, filled)
    }
    locally {
      val drained = h.drain(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.SIMULATE)
      assertTrue(drained.isEmpty)
    }
    locally {
      val drained = h.drain(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.SIMULATE)
      assertTrue(drained.isEmpty)
    }
    locally {
      val drained = h.drain(1000, IFluidHandler.FluidAction.SIMULATE)
      assertTrue(drained.isEmpty)
    }
  }

  @Test
  def fillCreativeHandler(): Unit = {
    val h = new CreativeTankHandler
    val filled = h.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.SIMULATE)
    assertEquals(FluidAmount.BUCKET_WATER.amount, filled)
    assertTrue(h.getTank.fluidAmount.isEmpty)

    val filled2 = h.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
    assertEquals(FluidAmount.BUCKET_WATER.amount, filled2)
    assertEquals(FluidAmount.BUCKET_WATER.fluid, h.getTank.fluidAmount.fluid)
    assertEquals(Long.MaxValue, h.getTank.fluidAmount.amount)
  }

  @Test
  def drainCreativeHandler(): Unit = {
    val h = new CreativeTankHandler
    assertAll(
      () => assertTrue(h.drain(1000, IFluidHandler.FluidAction.SIMULATE).isEmpty),
      () => assertTrue(h.drain(FluidAmount.EMPTY.toStack, IFluidHandler.FluidAction.SIMULATE).isEmpty),
      () => assertTrue(h.drain(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.SIMULATE).isEmpty),
    )
    h.fill(FluidAmount.BUCKET_WATER.toStack, IFluidHandler.FluidAction.EXECUTE)
    assertAll(
      Range(1, 10).map(i => Math.pow(10, i).toLong).map(FluidAmount.BUCKET_WATER.setAmount)
        .map[Executable](f => () => assertEquals(f, FluidAmount.fromStack(h.drain(f.toStack, IFluidHandler.FluidAction.SIMULATE)), s"Drain $f Simulation")): _*
    )
    assertAll(
      Range(1, 10).map(i => Math.pow(10, i).toLong).map(FluidAmount.BUCKET_WATER.setAmount)
        .map[Executable](f => () => assertEquals(f, FluidAmount.fromStack(h.drain(f.toStack, IFluidHandler.FluidAction.EXECUTE)), s"Drain $f Execution")): _*
    )
    assertAll(
      Range(1, 10).map(i => Math.pow(10, i).toLong).map(FluidAmount.BUCKET_LAVA.setAmount)
        .map[Executable](f => () => assertTrue(FluidAmount.fromStack(h.drain(f.toStack, IFluidHandler.FluidAction.SIMULATE)).isEmpty, s"Drain $f Simulation")): _*
    )
  }

  @Test
  def emptyCreativeHandler(): Unit = {
    val h = new CreativeTankHandler
    assertAll(
      () => assertEquals(0, h.fill(FluidStack.EMPTY, IFluidHandler.FluidAction.SIMULATE), "Filling EMPTY"),
      () => assertEquals(0, h.fill(FluidStack.EMPTY, IFluidHandler.FluidAction.EXECUTE), "Filling EMPTY"),
      () => assertTrue(h.getTank.fluidAmount.isEmpty, "Fill with 0 cause no changes."),
      () => assertEquals(FluidAmount.EMPTY, FluidAmount.fromStack(h.drain(FluidStack.EMPTY, IFluidHandler.FluidAction.SIMULATE)), "Drain empty fluid to get EMPTY."),
      () => assertEquals(FluidAmount.EMPTY, FluidAmount.fromStack(h.drain(0, IFluidHandler.FluidAction.SIMULATE)), "Drain 0 fluid to get EMPTY."),
    )
  }
}
