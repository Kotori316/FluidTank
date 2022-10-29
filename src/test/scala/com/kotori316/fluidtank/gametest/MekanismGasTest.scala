package com.kotori316.fluidtank.gametest

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.fluids.Tank
import com.kotori316.fluidtank.gametest.MekanismGasTest.BATCH
import com.kotori316.fluidtank.integration.mekanism_gas.{GasAmount, GasTankHandler}
import com.kotori316.testutil.GameTestUtil.EMPTY_STRUCTURE
import mekanism.common.registries.MekanismGases
import net.minecraft.gametest.framework.{GameTest, GameTestHelper}
import net.minecraftforge.gametest.{GameTestHolder, PrefixGameTestTemplate}
import org.junit.jupiter.api.Assertions._

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
class MekanismGasTest {
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
}

object MekanismGasTest {
  final val BATCH = "mekanism_integration"
}
