package com.kotori316.fluidtank.test

import cats.data.Chain
import cats.implicits._
import com.kotori316.fluidtank.fluids.{FluidAmount, ListTankHandler, Tank, TankHandler, VoidTankHandler}
import net.minecraft.fluid.Fluids
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertTrue}
import org.junit.jupiter.api.Test

//noinspection DuplicatedCode
class ListTankHandlerTest {
  private final val WOOD = Tank(FluidAmount.EMPTY, 4000L)
  private final val STONE = Tank(FluidAmount.EMPTY, 16000L)

  private final def createWoodStone: Chain[TankHandler] = Chain(TankHandler(WOOD), TankHandler(STONE))

  @Test
  def fillWater1(): Unit = {
    val h = new ListTankHandler(createWoodStone)

    val filled = h.fill(new FluidStack(Fluids.WATER, 2000), IFluidHandler.FluidAction.EXECUTE)
    assertEquals(2000, filled)
    assertEquals(Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(2000), WOOD.capacity), STONE), h.getTankList)
  }

  @Test
  def fillWater2(): Unit = {
    val h = new ListTankHandler(createWoodStone)

    val filled = h.fill(new FluidStack(Fluids.WATER, 10000), IFluidHandler.FluidAction.EXECUTE)
    assertEquals(10000, filled)
    assertEquals(Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(4000), WOOD.capacity), Tank(FluidAmount.BUCKET_WATER.setAmount(6000), STONE.capacity)), h.getTankList)
  }

  @Test
  def fillLava1(): Unit = {
    val h = new ListTankHandler(createWoodStone)

    val filled = h.fill(new FluidStack(Fluids.LAVA, 10000), IFluidHandler.FluidAction.EXECUTE)
    assertEquals(10000, filled)
    assertEquals(Chain(Tank(FluidAmount.BUCKET_LAVA.setAmount(4000), WOOD.capacity), Tank(FluidAmount.BUCKET_LAVA.setAmount(6000), STONE.capacity)), h.getTankList)
  }

  @Test
  def fillLava2(): Unit = {
    val before = Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(0), WOOD.capacity), Tank(FluidAmount.BUCKET_WATER.setAmount(0), STONE.capacity))
    val h = new ListTankHandler(before.map(TankHandler.apply))
    locally {
      val filled = h.fill(new FluidStack(Fluids.WATER, 10000), IFluidHandler.FluidAction.SIMULATE)
      assertEquals(10000, filled)
    }
    locally {
      val filled = h.fill(new FluidStack(Fluids.LAVA, 10000), IFluidHandler.FluidAction.SIMULATE)
      assertEquals(10000, filled)
    }
    locally {
      val filled = h.fill(new FluidStack(Fluids.EMPTY, 10000), IFluidHandler.FluidAction.SIMULATE)
      assertEquals(0, filled)
    }
  }

  @Test
  def fillWaterSimulate(): Unit = {
    val h = new ListTankHandler(createWoodStone)

    val filled = h.fill(new FluidStack(Fluids.WATER, 10000), IFluidHandler.FluidAction.SIMULATE)
    assertEquals(10000, filled)
    assertEquals(createWoodStone.map(_.getTank), h.getTankList)
  }

  @Test
  def drainWater1(): Unit = {
    val before = Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(4000), WOOD.capacity), Tank(FluidAmount.BUCKET_WATER.setAmount(6000), STONE.capacity))
    val h = new ListTankHandler(before.map(TankHandler.apply))
    locally {
      val drained = h.drain(5000, IFluidHandler.FluidAction.SIMULATE)
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(5000), FluidAmount.fromStack(drained))
      assertEquals(before, h.getTankList)
    }
    locally {
      val drained = h.drain(5000, IFluidHandler.FluidAction.EXECUTE)
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(5000), FluidAmount.fromStack(drained))
      assertEquals(Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(4000), WOOD.capacity), Tank(FluidAmount.BUCKET_WATER.setAmount(1000), STONE.capacity)), h.getTankList)
    }
  }

  @Test
  def drainWater2(): Unit = {
    val before = Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(4000), WOOD.capacity), Tank(FluidAmount.BUCKET_WATER.setAmount(6000), STONE.capacity))
    val h = new ListTankHandler(before.map(TankHandler.apply))
    locally {
      val drained = h.drain(15000, IFluidHandler.FluidAction.SIMULATE)
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(10000), FluidAmount.fromStack(drained))
      assertEquals(before, h.getTankList)
    }
    locally {
      val drained = h.drain(15000, IFluidHandler.FluidAction.EXECUTE)
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(10000), FluidAmount.fromStack(drained))
      assertTrue(h.getTankList.forall(_.fluidAmount.isEmpty))
    }
  }

  @Test
  def drainWater3(): Unit = {
    val before = Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(4000), WOOD.capacity), Tank(FluidAmount.BUCKET_WATER.setAmount(6000), STONE.capacity))
    val h = new ListTankHandler(before.map(TankHandler.apply))
    locally {
      val drained = h.drain(FluidAmount.BUCKET_WATER.setAmount(5000).toStack, IFluidHandler.FluidAction.SIMULATE)
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(5000), FluidAmount.fromStack(drained))
      assertEquals(before, h.getTankList)
    }
    locally {
      val drained = h.drain(FluidAmount.BUCKET_WATER.setAmount(5000).toStack, IFluidHandler.FluidAction.EXECUTE)
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(5000), FluidAmount.fromStack(drained))
      assertEquals(Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(4000), WOOD.capacity), Tank(FluidAmount.BUCKET_WATER.setAmount(1000), STONE.capacity)), h.getTankList)
    }
  }

  @Test
  def drainFailLava(): Unit = {
    val before = Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(4000), WOOD.capacity), Tank(FluidAmount.BUCKET_WATER.setAmount(6000), STONE.capacity))
    val h = new ListTankHandler(before.map(TankHandler.apply))
    locally {
      val drained = h.drain(FluidAmount.BUCKET_LAVA.setAmount(5000).toStack, IFluidHandler.FluidAction.SIMULATE)
      assertTrue(drained.isEmpty)
      assertEquals(before, h.getTankList)
    }
    locally {
      val drained = h.drain(FluidAmount.BUCKET_LAVA.setAmount(5000).toStack, IFluidHandler.FluidAction.EXECUTE)
      assertTrue(drained.isEmpty)
      assertEquals(before, h.getTankList)
    }
  }

  @Test
  def fillVoidTank(): Unit = {
    val value = Chain(TankHandler(Tank(FluidAmount.EMPTY, 4000)), new VoidTankHandler(), TankHandler(Tank(FluidAmount.EMPTY, 4000)))
    val h = new ListTankHandler(value)
    assertTrue(FluidAmount.BUCKET_WATER.setAmount(6000L) === h.fill(FluidAmount.BUCKET_WATER.setAmount(6000L), IFluidHandler.FluidAction.SIMULATE))

    h.fill(FluidAmount.BUCKET_WATER.setAmount(6000L), IFluidHandler.FluidAction.EXECUTE)
    assertAll(
      () => assertTrue(Option(Tank(FluidAmount.BUCKET_WATER.setAmount(4000), 4000)) === value.get(0).map(_.getTank)),
      () => assertTrue(Option(Tank.EMPTY) === value.get(1).map(_.getTank)),
      () => assertTrue(Option(Tank(FluidAmount.EMPTY, 4000)) === value.get(2).map(_.getTank)),
    )
  }
}
