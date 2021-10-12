package com.kotori316.fluidtank.transport

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.transport.NeighborInstance._
import com.kotori316.fluidtank.transport.PipeConnection2Test.Holder
import net.minecraft.core.BlockPos
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

private[transport] final class PipeConnection2Test extends BeforeAllTest {

  @Test
  def dummy(): Unit = {
    assertTrue(PipeConnection2.empty[BlockPos](_ => false).isEmpty)
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
    val connectionSetter: PipeConnection2[(Int, Int)] => ((Int, Int)) => Unit = c => t => map.get(t).foreach(_.connection = c)
    val c1 = new PipeConnection2[(Int, Int)](Set(t(0, 1)), _ => false)
    c1.foreach(connectionSetter(c1))
    assertTrue(c1.poses.contains(t(0, 1)))
    assertNotNull(map(t(0, 1)).connection)
    assertEquals(c1, map(t(0, 1)).connection)

    val c2 = PipeConnection2.add(c1, t(1, 1))
    c2.foreach(connectionSetter(c2))
    assertNotNull(map(t(0, 1)).connection)
    assertNotNull(map(t(1, 1)).connection)
    assertEquals(c2, map(t(0, 1)).connection)
    assertEquals(c2, map(t(1, 1)).connection)

    val c3 = PipeConnection2.empty[(Int, Int)](p => p._2 == 3)
    val c4 = map.keys.toSeq.sorted.foldLeft(c3)(PipeConnection2.add)
    c4.foreach(connectionSetter(c4))
    assertTrue(map.values.forall(_.connection == c4))
    assertEquals(2, c4.outputs(c4.poses.head).size)
    assertEquals(t(2, 3), c4.outputs(t(0, 0)).last)
    assertEquals(t(1, 3), c4.outputs(t(3, 3)).last)
  }

  @inline
  def t(i1: Int, i2: Int) = (i1, i2)

}


private object PipeConnection2Test {

  class Holder {
    var connection: PipeConnection2[(Int, Int)] = _

    def setConnection(p: PipeConnection2[(Int, Int)]): Unit = {
      this.connection = p
    }
  }

}