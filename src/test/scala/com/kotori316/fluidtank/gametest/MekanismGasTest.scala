package com.kotori316.fluidtank.gametest

import cats.data.Chain
import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.fluids.Tank
import com.kotori316.fluidtank.gametest.MekanismGasTest.BATCH
import com.kotori316.fluidtank.integration.mekanism_gas._
import com.kotori316.testutil.GameTestUtil.EMPTY_STRUCTURE
import mekanism.api.chemical.gas.{Gas, GasStack}
import mekanism.api.{AutomationType, Action => MekanismAction}
import mekanism.common.registries.MekanismGases
import net.minecraft.gametest.framework.{AfterBatch, BeforeBatch, GameTest, GameTestHelper}
import net.minecraft.server.level.ServerLevel
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.gametest.{GameTestHolder, PrefixGameTestTemplate}
import org.junit.jupiter.api.Assertions._

import scala.annotation.unused

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
class MekanismGasTest {
  type TankAPI = mekanism.api.chemical.IChemicalTank[Gas, GasStack]

  @BeforeBatch(batch = BATCH)
  def beforeTest(@unused level: ServerLevel): Unit = {
    com.kotori316.fluidtank.Utils.setInDev(false)
  }

  @AfterBatch(batch = BATCH)
  def afterTest(@unused level: ServerLevel): Unit = {
    com.kotori316.fluidtank.Utils.setInDev(true)
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def instance(helper: GameTestHelper): Unit = {
    val handler = new GasTankHandler
    assertTrue(handler.isEmpty)

    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def filledInstance(helper: GameTestHelper): Unit = {
    val handler = GasTankHandler(Tank(GasAmount(MekanismGases.OXYGEN.get(), 1000), 4000))
    assertFalse(handler.isEmpty)
    assertEquals(4000, handler.getCapacity)
    assertEquals(MekanismGases.OXYGEN.get(), handler.getTank.content)

    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def setCapacity(helper: GameTestHelper): Unit = {
    val handler = GasTankHandler(6000)
    assertTrue(handler.isEmpty)
    assertEquals(6000, handler.getCapacity)
    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def insertToEmpty1Simulate(helper: GameTestHelper): Unit = {
    val handler = GasTankHandler(6000)

    val inserted = handler.fill(GasAmount(MekanismGases.OXYGEN.get(), 1000), MekanismAction.SIMULATE)
    assertEquals(GasAmount(MekanismGases.OXYGEN.get(), 1000), inserted)
    assertTrue(handler.isEmpty, "Simulate")

    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def insertToEmpty1Execute(helper: GameTestHelper): Unit = {
    val handler = GasTankHandler(6000)

    val inserted = handler.fill(GasAmount(MekanismGases.OXYGEN.get(), 1000), MekanismAction.EXECUTE)
    assertEquals(GasAmount(MekanismGases.OXYGEN.get(), 1000), inserted)
    assertFalse(handler.isEmpty, "Execute")

    assertEquals(Tank(GasAmount(MekanismGases.OXYGEN.get(), 1000), 6000), handler.getTank)

    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def insertToEmpty2(helper: GameTestHelper): Unit = {
    val handler = GasTankHandler(6000)
    val simulate = handler.fill(GasAmount.EMPTY, MekanismAction.SIMULATE)
    assertTrue(simulate.isEmpty)
    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def insertToEmpty3GasAPI(helper: GameTestHelper): Unit = {
    val handler = GasTankHandler(6000)
    val tank: TankAPI = handler

    val left = tank.insert(MekanismGases.OXYGEN.getStack(1000), MekanismAction.SIMULATE, AutomationType.EXTERNAL)
    assertTrue(left.isEmpty, "The API requests that empty stack is returned if whole stack is accepted.")
    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def drainGasAPI1(helper: GameTestHelper): Unit = {
    val stack = MekanismGases.OXYGEN.getStack(2000)
    val handler = GasTankHandler(Tank(GasAmount.fromStack(stack), 6000))
    val tank: TankAPI = handler
    val drained = tank.extract(3000, MekanismAction.SIMULATE, AutomationType.EXTERNAL)

    assertEquals(stack, drained)

    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def drainGasAPI2(helper: GameTestHelper): Unit = {
    val stack = MekanismGases.OXYGEN.getStack(2000)
    val handler = GasTankHandler(Tank(GasAmount.fromStack(stack), 6000))
    val tank: TankAPI = handler
    val drained = tank.extract(1500, MekanismAction.SIMULATE, AutomationType.EXTERNAL)

    assertEquals(new GasStack(stack, 1500), drained)

    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def setStack1(helper: GameTestHelper): Unit = {
    val handler = GasTankHandler(4000)
    handler.setStack(GasAmount(MekanismGases.OXYGEN.get(), 1000).toStack)
    assertEquals(GasAmount(MekanismGases.OXYGEN.get(), 1000), handler.getTank.genericAmount)

    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def setStack2(helper: GameTestHelper): Unit = {
    val handler = GasTankHandler(4000)
    handler.fill(GasAmount(MekanismGases.ANTIMATTER.get(), 1000), MekanismAction.EXECUTE)
    handler.setStack(GasAmount(MekanismGases.OXYGEN.get(), 1000).toStack)
    assertEquals(GasAmount(MekanismGases.OXYGEN.get(), 1000), handler.getTank.genericAmount)

    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def list1Simulate(helper: GameTestHelper): Unit = {
    val handler1 = GasTankHandler(4000)
    val handler2 = GasTankHandler(4000)
    val list = new GasListHandler(Chain(handler1, handler2))
    val filled = list.fill(GasAmount(MekanismGases.OXYGEN.get(), 6000), IFluidHandler.FluidAction.SIMULATE)
    assertEquals(GasAmount(MekanismGases.OXYGEN.get(), 6000), filled)
    assertTrue(handler1.isEmpty, "Simulation must not change the content. %s".formatted(handler1))
    assertTrue(handler2.isEmpty, "Simulation must not change the content. %s".formatted(handler2))

    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def list1Execute(helper: GameTestHelper): Unit = {
    val handler1 = GasTankHandler(4000)
    val handler2 = GasTankHandler(4000)
    val list = new GasListHandler(Chain(handler1, handler2))
    val filled = list.fill(GasAmount(MekanismGases.OXYGEN.get(), 6000), IFluidHandler.FluidAction.EXECUTE)
    assertEquals(GasAmount(MekanismGases.OXYGEN.get(), 6000), filled)
    assertEquals(GasAmount(MekanismGases.OXYGEN.get(), 4000), handler1.getTank.genericAmount)
    assertEquals(GasAmount(MekanismGases.OXYGEN.get(), 2000), handler2.getTank.genericAmount)

    helper.succeed()
  }

  @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
  def list1Set(helper: GameTestHelper): Unit = {
    val handler1 = GasTankHandler(4000)
    val handler2 = GasTankHandler(4000)
    val list = new GasListHandler(Chain(handler1, handler2))
    list.setChemicalInTank(0, GasAmount(MekanismGases.OXYGEN.get(), 6000).toStack)

    assertEquals(GasAmount(MekanismGases.OXYGEN.get(), 4000), handler1.getTank.genericAmount)
    assertEquals(GasAmount(MekanismGases.OXYGEN.get(), 2000), handler2.getTank.genericAmount)

    helper.succeed()
  }
}

object MekanismGasTest {
  final val BATCH = "mekanism_integration"
}
