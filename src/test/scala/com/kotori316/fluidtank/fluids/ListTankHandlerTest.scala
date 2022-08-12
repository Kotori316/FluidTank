package com.kotori316.fluidtank.fluids

import cats.data.Chain
import cats.implicits._
import com.kotori316.fluidtank._
import net.minecraft.world.level.material.{Fluid, Fluids, WaterFluid}
import net.minecraftforge.common.Tags
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.{FluidStack, FluidType}
import net.minecraftforge.fml.unsafe.UnsafeHacks
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

//noinspection DuplicatedCode
object ListTankHandlerTest extends BeforeAllTest {
  private final val WOOD = Tank(FluidAmount.EMPTY, 4000L)
  private final val STONE = Tank(FluidAmount.EMPTY, 16000L)

  private def createWoodStone: Chain[TankHandler] = Chain(TankHandler(WOOD), TankHandler(STONE))

  object Fill extends BeforeAllTest {
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
    def fillFluidLimited1(): Unit = {
      val handlers = Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(1000), 4000), Tank(FluidAmount.EMPTY, 6000))
      val h = new ListTankHandler(handlers.map(TankHandler.apply), limitOneFluid = true)

      {
        val filled = h.fill(FluidAmount.BUCKET_WATER.setAmount(3000), IFluidHandler.FluidAction.SIMULATE)
        assertTrue(FluidAmount.BUCKET_WATER.setAmount(3000) === filled,
          s"Filling Water to real connection. $filled")
      }
      {
        val filled = h.fill(FluidAmount.BUCKET_WATER.setAmount(12000), IFluidHandler.FluidAction.SIMULATE)
        assertTrue(FluidAmount.BUCKET_WATER.setAmount(9000) === filled,
          s"Filling Water to real connection. $filled")
      }
      val filled = h.fill(FluidAmount.BUCKET_WATER.setAmount(12000), IFluidHandler.FluidAction.EXECUTE)
      assertTrue(FluidAmount.BUCKET_WATER.setAmount(9000) === filled, s"Filling Water to real connection. $filled")
      assertTrue(Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(4000), 4000), Tank(FluidAmount.BUCKET_WATER.setAmount(6000), 6000)) === h.getTankList)
    }

    @Test
    def fillFluidLimited2(): Unit = {
      val handlers = Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(1000), 4000), Tank(FluidAmount.EMPTY, 6000))
      val h = new ListTankHandler(handlers.map(TankHandler.apply), limitOneFluid = true)

      {
        val filled = h.fill(FluidAmount.BUCKET_LAVA.setAmount(3000), IFluidHandler.FluidAction.SIMULATE)
        assertTrue(FluidAmount.EMPTY === filled,
          s"Filling Lava to real connection. $filled")
      }
    }

    @Test
    def fillFluidLimited3(): Unit = {
      val handlers = Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(0), 4000), Tank(FluidAmount.EMPTY, 6000))
      val h = new ListTankHandler(handlers.map(TankHandler.apply), limitOneFluid = true)

      {
        val filled = h.fill(FluidAmount.BUCKET_LAVA.setAmount(3000), IFluidHandler.FluidAction.SIMULATE)
        assertTrue(FluidAmount.BUCKET_LAVA.setAmount(3000) === filled,
          s"Filling Lava to real connection. $filled")
      }
      val filled = h.fill(FluidAmount.BUCKET_LAVA.setAmount(3000), IFluidHandler.FluidAction.EXECUTE)
      assertTrue(FluidAmount.BUCKET_LAVA.setAmount(3000) === filled, s"Filling Lava to real connection. $filled")
      val expectTankChain = Chain(Tank(FluidAmount.BUCKET_LAVA.setAmount(3000), 4000), Tank(FluidAmount.EMPTY, 6000))
      assertTrue(expectTankChain === h.getTankList,
        s"Tank list, actual=${h.getTankList}, expect=$expectTankChain")
    }
  }

  object Drain extends BeforeAllTest {
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
    def drainWater4(): Unit = {
      val before = Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(4000), WOOD.capacity), Tank(FluidAmount.BUCKET_WATER.setAmount(6000), STONE.capacity))
      val h = new ListTankHandler(before.map(TankHandler.apply))
      locally {
        val drained = h.drain(FluidAmount.EMPTY.setAmount(5000), IFluidHandler.FluidAction.SIMULATE)
        assertEquals(FluidAmount.BUCKET_WATER.setAmount(5000), drained)
        assertEquals(before, h.getTankList)
      }
      locally {
        val drained = h.drain(FluidAmount.EMPTY.setAmount(5000), IFluidHandler.FluidAction.EXECUTE)
        assertEquals(FluidAmount.BUCKET_WATER.setAmount(5000), drained)
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
  }

  object SpecialHandlers extends BeforeAllTest {
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

  def amountAndTanks(): Array[Array[Any]] = Array(
    Array(20000L, createWoodStone),
    Array(5000, Chain(Tank.EMPTY, Tank(FluidAmount.EMPTY, 3000), Tank(FluidAmount.BUCKET_WATER, 2000)).map(TankHandler.apply)),
    Array(0, Chain(Tank.EMPTY).map(TankHandler.apply)),
    Array(2000, Chain(Tank(FluidAmount.BUCKET_WATER, 2000)).map(TankHandler.apply)),
    Array(5000, Chain(Tank(FluidAmount.BUCKET_LAVA, 3000), Tank(FluidAmount.BUCKET_WATER, 2000)).map(TankHandler.apply)),
  )

  def fluidKeyAndAmount(): Array[Array[Any]] = for {
    key <- com.kotori316.fluidtank.fluids.FluidAmountTest.fluidKeys()
    amount <- Seq(1000, 2000)
  } yield Array(key, amount)

  object TankContents extends BeforeAllTest {

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.ListTankHandlerTest#amountAndTanks"))
    def testGetSumOfCapacity(expect: Long, tanks: Chain[TankHandler]): Unit = {
      val h = new ListTankHandler(tanks)
      assertEquals(expect, h.getSumOfCapacity)
    }

    @Test
    def testGetFluidInTank1(): Unit = {
      val h = new ListTankHandler(Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(2000), 2000), Tank(FluidAmount.BUCKET_LAVA, 2000)).map(TankHandler.apply))
      val stack = h.getFluidInTank(0)
      assertAll(
        () => assertTrue(FluidAmount.BUCKET_WATER.setAmount(2000).toStack.isFluidStackIdentical(stack), s"Result: ${(stack.getRawFluid, stack.getAmount)}"),
        () => assertTrue(FluidAmount.BUCKET_WATER.setAmount(2000) === FluidAmount.fromStack(stack), s"Result: ${FluidAmount.fromStack(stack)}"),
      )
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.ListTankHandlerTest#fluidKeyAndAmount"))
    def testGetFluidInTank2(key: FluidKey, i: Int): Unit = {
      val amount = key.toAmount(i)
      val h = new ListTankHandler(Chain(Tank(amount, 2000)).map(TankHandler.apply))
      val stack = h.getFluidInTank(0)
      val amountFromStack = FluidAmount.fromStack(stack)
      if (!key.isEmpty) assertAll(
        () => assertTrue(amount.toStack.isFluidStackIdentical(stack), show"Result1: ${amount.toStack}, $stack"),
        () => assertTrue(amount === amountFromStack, s"Result2: $amount, $amountFromStack"),
      )
    }

    @Test
    def testGetFluidInTank4(): Unit = {
      val h = new ListTankHandler(Chain(Tank.EMPTY).map(TankHandler.apply))
      val stack = h.getFluidInTank(0)
      assertAll(
        () => assertTrue(stack.isEmpty, s"Result: $stack"),
        () => assertTrue(FluidAmount.fromStack(stack).isEmpty, s"Result: ${FluidAmount.fromStack(stack)}"),
      )
    }

    @Test
    def testGetFluidInTank5(): Unit = {
      val h = new ListTankHandler(Chain(Tank(FluidAmount.BUCKET_WATER.setAmount(2000), 2000), Tank(FluidAmount.BUCKET_WATER, 2000)).map(TankHandler.apply))
      val stack = h.getFluidInTank(0)
      assertAll(
        () => assertTrue(FluidAmount.BUCKET_WATER.setAmount(3000).toStack.isFluidStackIdentical(stack), s"Result: ${(stack.getRawFluid, stack.getAmount)}"),
        () => assertTrue(FluidAmount.BUCKET_WATER.setAmount(3000) === FluidAmount.fromStack(stack), s"Result: ${FluidAmount.fromStack(stack)}"),
      )
    }

    @Test
    def testGetFluidInTank6(): Unit = {
      val h = new ListTankHandler(Chain(Tank(FluidAmount.BUCKET_LAVA.setAmount(2000), 2000), Tank(FluidAmount.BUCKET_WATER, 2000)).map(TankHandler.apply))
      val stack = h.getFluidInTank(0)
      assertAll(
        () => assertTrue(FluidAmount.BUCKET_LAVA.setAmount(2000).toStack.isFluidStackIdentical(stack), s"Result: ${(stack.getRawFluid, stack.getAmount)}"),
        () => assertTrue(FluidAmount.BUCKET_LAVA.setAmount(2000) === FluidAmount.fromStack(stack), s"Result: ${FluidAmount.fromStack(stack)}"),
      )
    }

    @Test
    def fillGas(): Unit = {
      val fluid = new WaterFluid.Source()
      val attribute = FluidType.Properties.create().density(999)
      UnsafeHacks.setField(classOf[Fluid].getDeclaredField("forgeFluidType"), fluid, new FluidType(attribute))
      //noinspection ScalaDeprecation,deprecation
      fluid.builtInRegistryHolder().bindTags(java.util.List.of(Tags.Fluids.GASEOUS))

      val fa = FluidAmount(fluid, 1000L, None)

      var tests: Seq[Executable] = Nil

      {
        val h = new ListTankHandler(Chain(Tank(FluidAmount.EMPTY, 2000), Tank(FluidAmount.EMPTY, 2000)).map(TankHandler.apply))
        tests ++= Seq(
          () => assertEquals(fa, h.fill(fa, IFluidHandler.FluidAction.SIMULATE)),
          () => assertEquals(fa.setAmount(4000), h.fill(fa.setAmount(4000), IFluidHandler.FluidAction.SIMULATE)),
          () => assertEquals(fa.setAmount(4000), h.fill(fa.setAmount(6000), IFluidHandler.FluidAction.SIMULATE)),
        )
      }
      {
        val h = new ListTankHandler(Chain(Tank(FluidAmount.EMPTY, 2000), Tank(FluidAmount.EMPTY, 2000)).map(TankHandler.apply))
        h.fill(fa, IFluidHandler.FluidAction.EXECUTE)
        tests ++= Seq(
          () => assertTrue(Chain(Tank(FluidAmount.EMPTY, 2000), Tank(fa, 2000)) === h.getTankList, s"Filled tank ${h.getTankList}")
        )
      }
      {
        val h = new ListTankHandler(Chain(Tank(FluidAmount.EMPTY, 2000), Tank(FluidAmount.EMPTY, 2000)).map(TankHandler.apply))
        h.fill(fa.setAmount(3000), IFluidHandler.FluidAction.EXECUTE)
        tests ++= Seq(
          () => assertTrue(Chain(Tank(fa, 2000), Tank(fa.setAmount(2000), 2000)) === h.getTankList, s"Filled tank ${h.getTankList}")
        )
      }

      {
        val h = new ListTankHandler(Chain(Tank(fa.setAmount(2000), 2000), Tank(fa.setAmount(2000), 2000)).map(TankHandler.apply))
        h.drain(1000, IFluidHandler.FluidAction.EXECUTE)
        tests ++= Seq(
          () => assertTrue(Chain(Tank(fa, 2000), Tank(fa.setAmount(2000), 2000)) === h.getTankList, s"Drained tank ${h.getTankList}")
        )
      }
      {
        val h = new ListTankHandler(Chain(Tank(fa.setAmount(2000), 2000), Tank(fa.setAmount(2000), 2000)).map(TankHandler.apply))
        h.drain(3000, IFluidHandler.FluidAction.EXECUTE)
        tests ++= Seq(
          () => assertTrue(Chain(Tank(fa.setAmount(0), 2000), Tank(fa, 2000)) === h.getTankList, s"Drained tank ${h.getTankList}")
        )
      }
      assertAll(tests: _*)
    }
  }
}
