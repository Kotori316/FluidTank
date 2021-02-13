package com.kotori316.fluidtank.fluid

import com.kotori316.fluidtank.BeforeAllTest
import com.kotori316.fluidtank.fluids._
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import scala.jdk.CollectionConverters._

//noinspection DuplicatedCode It's test!
private[fluid] final class TransferOperationTest extends BeforeAllTest {
  private[this] final val waterTank = Tank(FluidAmount.BUCKET_WATER.setAmount(0), 16000)
  private[this] final val lavaTank = Tank(FluidAmount.BUCKET_LAVA.setAmount(4000), 16000)

  @Test
  def fill1(): Unit = {
    val fillAction = fillOp(waterTank)
    val x1: Seq[Executable] = {
      val (log, left, tank) = fillAction.run((), FluidAmount.BUCKET_WATER.setAmount(10000))
      Seq(
        () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.FillFluid])),
        () => assertTrue(left.isEmpty),
        () => assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(10000), 16000), tank),
      )
    }
    val x2: Seq[Executable] = {
      val (log, left, tank) = fillAction.run((), FluidAmount.BUCKET_LAVA.setAmount(10000))
      Seq(
        () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.FillFluid])),
        () => assertTrue(left.isEmpty),
        () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(10000), 16000), tank),
      )
    }

    assertAll((x1 ++ x2).asJava)
  }

  @Test
  def fill2(): Unit = {
    val fillAction = for {
      a <- fillOp(waterTank)
      b <- fillOp(waterTank)
    } yield (a, b)

    val (_, left, (a, b)) = fillAction.run((), FluidAmount.BUCKET_WATER.setAmount(20000))
    assertTrue(left.isEmpty)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(16000), a.fluidAmount)
    assertEquals(FluidAmount.BUCKET_WATER.setAmount(4000), b.fluidAmount)
  }

  @Test
  def fillListAllSuccess(): Unit = {
    val tanks = List(Tank(FluidAmount.BUCKET_WATER.setAmount(0), 16000), Tank.EMPTY, Tank(FluidAmount.BUCKET_LAVA.setAmount(0), 16000))
    val fillOperation = fillList(tanks)
    locally {
      val (_, left, a :: b :: c :: Nil) = fillOperation.run((), FluidAmount.BUCKET_WATER)
      assertAll(
        () => assertTrue(left.isEmpty, s"Left isEmpty, $left"),
        () => assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(1000), 16000), a),
        () => assertEquals(Tank.EMPTY, b),
        () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(0), 16000), c),
      )
    }
    locally {
      val (_, left, a :: b :: c :: Nil) = fillOperation.run((), FluidAmount.BUCKET_LAVA)
      assertAll(
        () => assertTrue(left.isEmpty, s"Left isEmpty, $left"),
        () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(1000), 16000), a),
        () => assertEquals(Tank.EMPTY, b),
        () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(0), 16000), c),
      )
    }
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

  @Test
  def fillListWaterOnly(): Unit = {
    val tanks = List(Tank(FluidAmount.BUCKET_WATER.setAmount(0), 16000), Tank.EMPTY)
    val fillOperation = fillList(tanks)
    locally {
      val (_, left, a :: b :: Nil) = fillOperation.run((), FluidAmount.BUCKET_WATER)
      assertAll(
        () => assertTrue(left.isEmpty, s"Left isEmpty, $left"),
        () => assertEquals(Tank(FluidAmount.BUCKET_WATER.setAmount(1000), 16000), a),
        () => assertEquals(Tank.EMPTY, b),
      )
    }
    locally {
      val (_, left, a :: b :: Nil) = fillOperation.run((), FluidAmount.BUCKET_LAVA)
      assertAll(
        () => assertTrue(left.isEmpty, s"Left isEmpty, $left"),
        () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(1000), 16000), a),
        () => assertEquals(Tank.EMPTY, b),
      )
    }
  }

  @Test
  def fillListWaterOnly2(): Unit = {
    val tanks = List(Tank(FluidAmount.BUCKET_WATER, 16000), Tank.EMPTY)
    val fillOperation = fillList(tanks)
    locally {
      val (_, left, a :: b :: Nil) = fillOperation.run((), FluidAmount.BUCKET_WATER)
      assertAll(
        () => assertTrue(left.isEmpty, s"Left isEmpty, $left"),
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

  @Test
  def fillAll1(): Unit = {
    val fillAction = fillAll(List(waterTank, waterTank.copy(capacity = 32000)))
    val (_, left, a :: b :: Nil) = fillAction.run((), FluidAmount.BUCKET_WATER.setAmount(1))
    assertAll(
      () => assertTrue(left.isEmpty),
      () => assertEquals(waterTank.copy(fluidAmount = FluidAmount.BUCKET_WATER.setAmount(waterTank.capacity)), a),
      () => assertEquals(waterTank.copy(fluidAmount = FluidAmount.BUCKET_WATER.setAmount(32000), capacity = 32000), b),
    )
  }

  @Test
  def fillAll2(): Unit = {
    val fillAction = fillAll(List(waterTank, waterTank.copy(capacity = 32000)))
    val (_, left, a :: b :: Nil) = fillAction.run((), FluidAmount.BUCKET_LAVA.setAmount(1))
    assertAll(
      () => assertEquals(FluidAmount.BUCKET_LAVA.setAmount(0), left),
      () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(16000), 16000), a),
      () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(32000), 32000), b),
    )
  }

  @Test
  def fillAll3(): Unit = {
    val fillAction = fillAll(List(lavaTank, lavaTank.copy(capacity = 32000)))
    val (_, left, List(a, b)) = fillAction.run((), FluidAmount.BUCKET_WATER.setAmount(1))
    assertAll(
      () => assertEquals(FluidAmount.BUCKET_WATER.setAmount(1), left),
      () => assertEquals(lavaTank, a),
      () => assertEquals(lavaTank.copy(capacity = 32000), b),
    )
  }

  @Test
  def drain1(): Unit = {
    val drainAction = drainOp(lavaTank)
    val x1: Seq[Executable] = {
      val (log, left, tank) = drainAction.run((), FluidAmount.BUCKET_WATER.setAmount(10000))
      Seq(
        () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.DrainFailed])),
        () => assertEquals(FluidAmount.BUCKET_WATER.setAmount(10000), left),
        () => assertEquals(lavaTank, tank),
      )
    }
    val x2: Seq[Executable] = {
      val (log, left, tank) = drainAction.run((), FluidAmount.BUCKET_LAVA.setAmount(10000))
      Seq(
        () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.DrainFluid])),
        () => assertEquals(FluidAmount.BUCKET_LAVA.setAmount(6000), left),
        () => assertEquals(Tank(FluidAmount.BUCKET_LAVA.setAmount(0), 16000), tank),
      )
    }

    assertAll((x1 ++ x2).asJava)
  }

  @Test
  def drainFromEmptyTank(): Unit = {
    val drainAction = drainOp(Tank.EMPTY)
    val e1: Seq[Executable] = {
      val (log, left, tank) = drainAction.run((), FluidAmount.BUCKET_WATER.setAmount(10000))
      Seq(
        () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.DrainFailed]), s"Log=$log"),
        () => assertEquals(FluidAmount.BUCKET_WATER.setAmount(10000), left),
        () => assertEquals(Tank.EMPTY, tank),
      )
    }
    val e2: Seq[Executable] = {
      val (log, left, tank) = drainAction.run((), FluidAmount.BUCKET_LAVA.setAmount(10000))
      Seq(
        () => assertTrue(log.forall(_.isInstanceOf[FluidTransferLog.DrainFailed]), s"Log=$log"),
        () => assertEquals(FluidAmount.BUCKET_LAVA.setAmount(10000), left),
        () => assertEquals(Tank.EMPTY, tank),
      )
    }
    assertAll((e1 ++ e2).asJava)
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
