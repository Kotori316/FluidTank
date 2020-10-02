package com.kotori316.fluidtank.test

import com.kotori316.fluidtank.fluids._
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import scala.jdk.CollectionConverters._

class TransferOperationTest {
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
}
