package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.fluids.Tank
import net.minecraft.core.BlockPos
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{Nested, Test}

class ConnectionTest {

  import ConnectionHelperTest._

  @Test
  def emptyConnection(): Unit = {
    val t1 = tile(BlockPos.ZERO, "")
    val t2 = tile(BlockPos.ZERO.above(), "")
    val connection = SCH.createConnection(Seq(t1, t2))

    assertEquals(Option.empty, connection.getContent)
  }

  @Test
  def moveFilled1(): Unit = {
    val t1 = tile(BlockPos.ZERO, "")
    t1.tank = Tank(stringAmount("A", 2000), 5000)
    val t2 = tile(BlockPos.ZERO.above(), "")
    t2.tank = Tank(stringAmount("A", 2000), 5000)

    Connection.createAndInit(Seq(t1, t2))
    val c = t1.connection
    assertEquals(c, t2.connection)
    assertTrue(c.isDefined)

    val connection = c.orNull
    assertEquals(Option(stringAmount("A", 4000)), connection.getContent)
    assertEquals(Tank(stringAmount("A", 4000), 5000), t1.tank)
    assertTrue(t2.tank.isEmpty)
  }

  @Test
  def moveFilled2(): Unit = {
    val t1 = tile(BlockPos.ZERO, "")
    t1.tank = Tank(stringAmount("", 0), 5000)
    val t2 = tile(BlockPos.ZERO.above(), "")
    t2.tank = Tank(stringAmount("A", 4000), 5000)

    Connection.createAndInit(Seq(t1, t2))
    val c = t1.connection
    assertEquals(c, t2.connection)
    assertTrue(c.isDefined)

    val connection = c.orNull
    assertEquals(Option(stringAmount("A", 4000)), connection.getContent)
    assertEquals(Tank(stringAmount("A", 4000), 5000), t1.tank)
    assertTrue(t2.tank.isEmpty)
  }

  @Test
  def removeEmpty(): Unit = {
    val t1 = tile(BlockPos.ZERO, "")
    val t2 = tile(BlockPos.ZERO.above(), "")
    val t3 = tile(BlockPos.ZERO.above(2), "")

    Connection.createAndInit(Seq(t1, t2, t3))
    val c = t1.connection.orNull
    assertTrue(c.getContent.isEmpty)
    assertEquals(3, c.sortedTanks.length)

    c.remove(t2)

    assertEquals(1, t1.connection.orNull.sortedTanks.length)
    assertEquals(1, t3.connection.orNull.sortedTanks.length)
  }

  @Nested
  class InitTest {
    @Test
    def contains1(): Unit = {
      val t1 = tile(BlockPos.ZERO, "1")
      val t2 = tile(BlockPos.ZERO.above(), "1")
      val t3 = tile(BlockPos.ZERO.above(2), "1")

      Connection.createAndInit(Seq(t1, t2, t3))
      assertEquals(t1.connection, t2.connection)
      assertEquals(t1.connection, t3.connection)
      assertEquals(stringAmount("1", 3), t1.connection.orNull.content)
    }

    @Test
    def contain2_1(): Unit = {
      val t1 = tile(BlockPos.ZERO, "1")
      val t2 = tile(BlockPos.ZERO.above(), "1")
      val t3 = tile(BlockPos.ZERO.above(2), "2")

      Connection.createAndInit(Seq(t1, t2, t3))
      assertEquals(t1.connection, t2.connection)
      assertNotEquals(t1.connection, t3.connection)
      assertEquals(stringAmount("1", 2), t1.connection.orNull.content)
      assertEquals(stringAmount("2", 1), t3.connection.orNull.content)
      assertEquals(2, t1.connection.orNull.sortedTanks.length)
      assertEquals(1, t3.connection.orNull.sortedTanks.length)
    }

    @Test
    def contain2_2(): Unit = {
      val t1 = tile(BlockPos.ZERO, "1")
      val t2 = tile(BlockPos.ZERO.above(), "")
      val t3 = tile(BlockPos.ZERO.above(2), "2")

      Connection.createAndInit(Seq(t1, t2, t3))
      assertEquals(t1.connection, t2.connection)
      assertNotEquals(t1.connection, t3.connection)
      assertEquals(stringAmount("1", 1), t1.connection.orNull.content)
      assertEquals(stringAmount("2", 1), t3.connection.orNull.content)
      assertEquals(2, t1.connection.orNull.sortedTanks.length)
      assertEquals(1, t3.connection.orNull.sortedTanks.length)
    }
  }
}
