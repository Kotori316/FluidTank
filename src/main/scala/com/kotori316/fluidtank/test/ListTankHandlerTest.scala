package com.kotori316.fluidtank.test

import cats.data.Chain
import com.kotori316.fluidtank.fluids.{FluidAmount, ListTankHandler, Tank, TankHandler}
import net.minecraft.fluid.Fluids
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
  def fillWaterSimulate(): Unit = {
    val h = new ListTankHandler(createWoodStone)

    val filled = h.fill(new FluidStack(Fluids.WATER, 10000), IFluidHandler.FluidAction.SIMULATE)
    assertEquals(10000, filled)
    assertEquals(createWoodStone.map(_.getTank), h.getTankList)
  }
}
