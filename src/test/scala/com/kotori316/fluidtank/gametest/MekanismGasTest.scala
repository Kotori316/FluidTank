package com.kotori316.fluidtank.gametest

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.fluids.Tank
import com.kotori316.fluidtank.gametest.MekanismGasTest.BATCH
import com.kotori316.fluidtank.integration.mekanism_gas.{GasAmount, GasTankHandler}
import com.kotori316.testutil.GameTestUtil.EMPTY_STRUCTURE
import mekanism.api.chemical.gas.{Gas, GasStack}
import mekanism.api.{AutomationType, Action => MekanismAction}
import mekanism.common.registries.MekanismGases
import net.minecraft.gametest.framework.{AfterBatch, BeforeBatch, GameTest, GameTestHelper}
import net.minecraft.server.level.ServerLevel
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
}

object MekanismGasTest {
  final val BATCH = "mekanism_integration"
}
