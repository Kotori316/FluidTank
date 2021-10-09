package com.kotori316.fluidtank.fluids

import com.kotori316.fluidtank.BeforeAllTest
import net.minecraft.fluid.Fluids
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.{MethodSource, ValueSource}

import scala.jdk.CollectionConverters._

//noinspection DuplicatedCode It's test!
object TransferOperationTest {
  private[this] final val waterTank = Tank(FluidAmount.BUCKET_WATER.setAmount(0), 16000)
  private[this] final val lavaTank = Tank(FluidAmount.BUCKET_LAVA.setAmount(4000), 16000)

  def normalFluids(): Array[FluidAmount] = Array(FluidAmount.BUCKET_WATER, FluidAmount.BUCKET_LAVA)

  object Fill extends BeforeAllTest {
    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.TransferOperationTest#normalFluids"))
    def fillToEmpty1(fa: FluidAmount): Unit = {
      val fillAction = fillOp(waterTank)
      val x1: Seq[Executable] = {
        val (log, left, tank) = fillAction.run((), fa.setAmount(10000))
        Seq(
          () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.FillFluid])),
          () => assertTrue(left.isEmpty),
          () => assertEquals(Tank(fa.setAmount(10000), 16000), tank),
        )
      }
      assertAll(x1: _*)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.TransferOperationTest#normalFluids"))
    def fill2(fa: FluidAmount): Unit = {
      val emptyTank = Tank(FluidAmount.EMPTY, 16000)
      val fillAction = for {
        a <- fillOp(emptyTank)
        b <- fillOp(emptyTank)
      } yield (a, b)

      val (_, left, (a, b)) = fillAction.run((), fa.setAmount(20000))
      assertTrue(left.isEmpty)
      assertEquals(fa.setAmount(16000), a.fluidAmount)
      assertEquals(fa.setAmount(4000), b.fluidAmount)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.TransferOperationTest#normalFluids"))
    def fillListAllSuccess(fa: FluidAmount): Unit = {
      val tanks = List(Tank(FluidAmount.BUCKET_WATER.setAmount(0), 16000), Tank.EMPTY, Tank(FluidAmount.BUCKET_LAVA.setAmount(0), 16000))
      val fillOperation = fillList(tanks)

      val (_, left, a :: b :: c :: Nil) = fillOperation.run((), fa)
      assertAll(
        () => assertTrue(left.isEmpty, s"Left isEmpty, $left"),
        () => assertEquals(Tank(fa.setAmount(1000), 16000), a),
        () => assertEquals(Tank.EMPTY, b),
        () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(0), 16000), c),
      )
    }

    @Test
    def fillListAllSuccess2(): Unit = {
      val tanks = List(Tank(FluidAmount.BUCKET_WATER.setAmount(1000), 16000), Tank.EMPTY, Tank(FluidAmount.BUCKET_LAVA.setAmount(1000), 16000))
      val fillOperation = fillList(tanks)
      locally {
        val (_, left, a :: b :: c :: Nil) = fillOperation.run((), FluidAmount.BUCKET_WATER)
        assertAll(
          () => assertTrue(left.isEmpty, s"Left isEmpty, $left"),
          () => assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(2000), 16000), a),
          () => assertEquals(Tank.EMPTY, b, "Second tank isn't touched."),
          () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(1000), 16000), c),
        )
      }
      locally {
        val (_, left, a :: b :: c :: Nil) = fillOperation.run((), FluidAmount.BUCKET_LAVA)
        assertAll(
          () => assertTrue(left.isEmpty, s"Left isEmpty, $left"),
          () => assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(1000), 16000), a),
          () => assertTrue(b.isEmpty, "Second tank was tried to fill lava."),
          () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(2000), 16000), c),
        )
      }
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.TransferOperationTest#normalFluids"))
    def fillListWaterOnly(fa: FluidAmount): Unit = {
      val tanks = List(Tank(FluidAmount.BUCKET_WATER.setAmount(0), 16000), Tank.EMPTY)
      val fillOperation = fillList(tanks)

      val (_, left, a :: b :: Nil) = fillOperation.run((), fa)
      assertAll(
        () => assertTrue(left.isEmpty, s"Left isEmpty, $left"),
        () => assertEquals(Tank(fa, 16000), a),
        () => assertEquals(Tank.EMPTY, b),
      )
    }

    @Test
    def fillListWaterOnly2(): Unit = {
      val tanks = List(Tank(FluidAmount.BUCKET_WATER, 16000), Tank.EMPTY)
      val fillOperation = fillList(tanks)
      locally {
        val (_, left, a :: b :: Nil) = fillOperation.run((), FluidAmount.BUCKET_WATER)
        assertAll(
          () => assertTrue(left.isEmpty, s"Left isEmpty, $left"),
          () => assertEquals(Fluids.WATER, left.fluid),
          () => assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(2000), 16000), a),
          () => assertEquals(Tank.EMPTY, b),
        )
      }
      locally {
        val (_, left, a :: b :: Nil) = fillOperation.run((), FluidAmount.BUCKET_LAVA)
        assertAll(
          () => assertEquals(FluidAmount.BUCKET_LAVA, left),
          () => assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(1000), 16000), a),
          () => assertTrue(b.isEmpty),
        )
      }
    }

  }

  object FillAll extends BeforeAllTest {

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.TransferOperationTest#normalFluids"))
    def fillAll1(fluidAmount: FluidAmount): Unit = {
      val fillAction = fillAll(List(waterTank, waterTank.copy(capacity = 32000)))
      val (_, left, a :: b :: Nil) = fillAction.run((), fluidAmount.setAmount(1))
      assertAll(
        () => assertTrue(left.isEmpty),
        () => assertEquals(fluidAmount.setAmount(0), left),
        () => assertEquals(waterTank.copy(fluidAmount = fluidAmount.setAmount(waterTank.capacity)), a),
        () => assertEquals(waterTank.copy(fluidAmount = fluidAmount.setAmount(32000), capacity = 32000), b),
        () => assertEquals(Tank(fluidAmount.setAmount(16000), 16000), a),
        () => assertEquals(Tank(fluidAmount.setAmount(32000), 32000), b),
      )
    }

    @ParameterizedTest
    @ValueSource(ints = Array(0, 1, 1000))
    def fillAll3(amount: Int): Unit = {
      val fillAction = fillAll(List(lavaTank, lavaTank.copy(capacity = 32000)))
      val (_, left, List(a, b)) = fillAction.run((), FluidAmount.BUCKET_WATER.setAmount(amount))
      assertAll(
        () => assertEquals(FluidAmount.BUCKET_WATER.setAmount(amount), left),
        () => assertEquals(lavaTank, a),
        () => assertEquals(lavaTank.copy(capacity = 32000), b),
      )
    }

    @Test
    def fillAll4(): Unit = {
      val tanks = Seq(Tank(FluidAmount.BUCKET_WATER, 10000), Tank(FluidAmount.BUCKET_LAVA, 10000))
      val fillAction = fillAll(tanks)
      val (_, left, filled) = fillAction.run((), FluidAmount.EMPTY)
      assertAll(
        () => assertTrue(left.isEmpty),
        () => assertEquals(tanks, filled)
      )
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.TransferOperationTest#normalFluids"))
    def fillAllToEmpty1(fa: FluidAmount): Unit = {
      val tanks = Seq()
      val fillAction = fillAll(tanks)
      val (_, left, filled) = fillAction.run((), fa)
      assertAll(
        () => assertFalse(left.isEmpty),
        () => assertTrue(filled.isEmpty)
      )
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.TransferOperationTest#normalFluids"))
    def fillAllToEmpty2(fa: FluidAmount): Unit = {
      val tanks = Seq(Tank.EMPTY, Tank.EMPTY)
      val fillAction = fillAll(tanks)
      val (_, left, filled) = fillAction.run((), fa)
      assertAll(
        () => assertFalse(left.isEmpty),
        () => assertEquals(tanks, filled)
      )
    }
  }

  object Drain extends BeforeAllTest {

    @Test
    def drain1(): Unit = {
      val drainAction = drainOp(lavaTank)
      val (log, left, tank) = drainAction.run((), FluidAmount.BUCKET_WATER.setAmount(10000))
      val x1: Seq[Executable] = Seq(
        () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.DrainFailed])),
        () => assertEquals(FluidAmount.BUCKET_WATER.setAmount(10000), left),
        () => assertEquals(lavaTank, tank),
      )
      assertAll(x1: _*)
    }

    @Test
    def drain2(): Unit = {
      val drainAction = drainOp(lavaTank)
      val toDrain = FluidAmount.BUCKET_LAVA.setAmount(10000)
      val (log, left, tank) = drainAction.run((), toDrain)
      val drained = toDrain - left
      val x2: Seq[Executable] = Seq(
        () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.DrainFluid])),
        () => assertEquals(FluidAmount.BUCKET_LAVA.setAmount(6000), left),
        () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(0), 16000), tank),
        () => assertEquals(FluidAmount.BUCKET_LAVA * 4, drained)
      )
      assertAll(x2: _*)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.TransferOperationTest#normalFluids"))
    def drainFromEmptyTank(fa: FluidAmount): Unit = {
      val drainAction = drainOp(Tank.EMPTY)
      val (log, left, tank) = drainAction.run((), fa.setAmount(10000))
      val e1: Seq[Executable] = Seq(
        () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.DrainFailed]), s"Log=$log"),
        () => assertEquals(fa.setAmount(10000), left),
        () => assertEquals(Tank.EMPTY, tank),
      )
      assertAll(e1: _*)
    }

    @Test
    def drainFromEmptyWaterTank(): Unit = {
      val drainAction = drainOp(waterTank)
      val e1: Seq[Executable] = {
        val (log, left, tank) = drainAction.run((), FluidAmount.BUCKET_WATER.setAmount(10000))
        Seq(
          () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.DrainFailed]), s"Log=$log"),
          () => assertEquals(FluidAmount.BUCKET_WATER.setAmount(10000), left),
          () => assertEquals(waterTank, tank),
        )
      }
      val e2: Seq[Executable] = {
        val (log, left, tank) = drainAction.run((), FluidAmount.BUCKET_LAVA.setAmount(10000))
        Seq(
          () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.DrainFailed]), s"Log=$log"),
          () => assertEquals(FluidAmount.BUCKET_LAVA.setAmount(10000), left),
          () => assertEquals(waterTank, tank),
        )
      }
      assertAll((e1 ++ e2).asJava)
    }

    @Test
    def drainAll1(): Unit = {
      val tanks = List(Tank(FluidAmount.BUCKET_WATER.setAmount(1000), 16000), Tank.EMPTY, Tank(FluidAmount.BUCKET_LAVA.setAmount(1000), 16000))
      val drainOp = drainList(tanks)
      val x1: Seq[Executable] = {
        val (_, left, a :: b :: c :: Nil) = drainOp.run((), FluidAmount.EMPTY.setAmount(1000))
        Seq(
          () => assertTrue(left.isEmpty, s"Left: $left"),
          () => assertEquals(FluidAmount.BUCKET_WATER.setAmount(0), left, s"Left: $left"),
          () => assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(0), 16000), a),
          () => assertEquals(Tank.EMPTY, b),
          () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(1000), 16000), c),
        )
      }
      val x2: Seq[Executable] = {
        val (_, left, a :: b :: c :: Nil) = drainOp.run((), FluidAmount.BUCKET_WATER.setAmount(1000))
        Seq(
          () => assertTrue(left.isEmpty, s"Left: $left"),
          () => assertEquals(FluidAmount.BUCKET_WATER.setAmount(0), left, s"Left: $left"),
          () => assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(0), 16000), a),
          () => assertEquals(Tank.EMPTY, b),
          () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(1000), 16000), c),
        )
      }
      val x3: Seq[Executable] = {
        val (_, left, a :: b :: c :: Nil) = drainOp.run((), FluidAmount.BUCKET_LAVA.setAmount(1000))
        Seq(
          () => assertTrue(left.isEmpty, s"Left: $left"),
          () => assertEquals(FluidAmount.BUCKET_LAVA.setAmount(0), left, s"Left: $left"),
          () => assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(1000), 16000), a),
          () => assertEquals(Tank.EMPTY, b),
          () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(0), 16000), c),
        )
      }
      assertAll((x1 ++ x2 ++ x3).asJava)
    }

  }

  object Util extends BeforeAllTest {
    @Test
    def tankIsEmpty(): Unit = {
      assertAll(
        () => assertTrue(waterTank.isEmpty),
        () => assertFalse(lavaTank.isEmpty),
        () => assertTrue(Tank.EMPTY.isEmpty),
        () => assertFalse(Tank(FluidAmount.BUCKET_WATER, 2000L).isEmpty),
      )
    }
  }

}
