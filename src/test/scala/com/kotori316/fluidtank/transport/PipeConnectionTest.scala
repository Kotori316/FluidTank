package com.kotori316.fluidtank.transport

import cats.implicits.{catsKernelStdGroupForInt, catsKernelStdGroupForTuple2}
import com.kotori316.fluidtank.transport.PipeConnection._
import com.kotori316.fluidtank.transport.PipeConnectionTest.Holder
import com.kotori316.scala_lib.util.Norm._
import com.kotori316.scala_lib.util.NormInstanceL2._
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotEquals, assertTrue}
import org.junit.jupiter.api.Test

class PipeConnectionTest {

  @Test
  def dummy(): Unit = {
    assertEquals(Math.sqrt(3), new BlockPos(1, 1, 1).norm)
    assertTrue(PipeConnection.empty[BlockPos]({ case (_, _) => () }, _ => false).isEmpty)
  }

  @Test
  def createList(): Unit = {
    val map = Map(
      t(0, 1) -> new Holder,
      t(1, 1) -> new Holder,
      t(0, 2) -> new Holder,
      t(1, 2) -> new Holder,
      t(1, 3) -> new Holder,
      t(2, 3) -> new Holder,
    )
    val c1 = PipeConnection[(Int, Int)](Set(t(0, 1)), { case (p, c) => map.get(p).foreach(_.setConnection(c)) }, _ => false)
    assertTrue(c1.poses.contains(t(0, 1)))
    assertTrue(map(t(0, 1)).connection != null)
    assertEquals(c1, map(t(0, 1)).connection)

    val c2 = c1.add(t(1, 1))
    assertTrue(map(t(0, 1)).connection != null)
    assertTrue(map(t(1, 1)).connection != null)
    assertEquals(c2, map(t(0, 1)).connection)
    assertEquals(c2, map(t(1, 1)).connection)

    val c3 = PipeConnection.empty[(Int, Int)]({ case (p, c) => map.get(p).foreach(_.setConnection(c)) }, p => p._2 == 3)
    val c4 = map.keys.foldLeft(c3) { case (c, p) => c add p }
    assertTrue(map.values.forall(_.connection == c4))
    assertEquals(2, c4.outputs.size)
    assertEquals(t(2, 3), c4.outputSorted(t(0, 0)).last)
    assertEquals(t(1, 3), c4.outputSorted(t(3, 3)).last)
  }

  @inline
  def t(i1: Int, i2: Int) = (i1, i2)

  @Test
  def checkClass(): Unit = {
    val a = PipeConnection.empty[(Int, Int)]({ case (_, _) => () }, _ => false)
    assertEquals(classOf[PipeConnection.EmptyConnection[(Int, Int)]], a.getClass)
    val b = a.add(t(1, 2))
    assertEquals(classOf[PipeConnection.EmptyConnection[(Int, Int)]], a.getClass)
    assertNotEquals(classOf[PipeConnection.EmptyConnection[(Int, Int)]], b.getClass)
    assertEquals(classOf[PipeConnection[(Int, Int)]], b.getClass)
  }
}

object PipeConnectionTest {

  class Holder {
    var connection: PipeConnection[(Int, Int)] = _

    def setConnection(p: PipeConnection[(Int, Int)]): Unit = {
      this.connection = p
    }
  }

}