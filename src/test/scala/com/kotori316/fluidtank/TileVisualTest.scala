package com.kotori316.fluidtank

import com.kotori316.fluidtank.tiles.TileTank
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class TileVisualTest extends BeforeAllTest {
  @ParameterizedTest
  @ValueSource(longs = Array(0, 500, 1000, 2000, 4000))
  def height1(amount: Long): Unit = {
    val (minY, maxY) = TileTank.getFluidHeight(4000, amount.toDouble, 0, 1, 0, isGaseous = false)
    assertEquals(0.0, minY)
    assertEquals(amount.toDouble / 4000d, maxY)
  }

  @ParameterizedTest
  @ValueSource(longs = Array(0, 500, 1000, 2000, 4000))
  def height2(amount: Long): Unit = {
    val (minY, maxY) = TileTank.getFluidHeight(4000, amount.toDouble, 0, 1, 0, isGaseous = true)
    assertEquals(1 - amount.toDouble / 4000d, minY)
    assertEquals(1, maxY)
  }

  @ParameterizedTest
  @ValueSource(booleans = Array(true, false))
  def excess1(isGaseous: Boolean): Unit = {
    val (minY, maxY) = TileTank.getFluidHeight(4000, 5000, 0, 1, 0, isGaseous)
    assertEquals(0.0, minY)
    assertEquals(1.0, maxY)
  }

  @Test
  def lowBound1(): Unit = {
    locally {
      val (minY, maxY) = TileTank.getFluidHeight(4000, 2000, 0.5, 1, 0, isGaseous = false)
      assertEquals(0.5, minY)
      assertEquals(0.75, maxY)
    }
    locally {
      val (minY, maxY) = TileTank.getFluidHeight(4000, 4000, 0.5, 1, 0, isGaseous = false)
      assertEquals(0.5, minY)
      assertEquals(1.0, maxY)
    }
  }

  @Test
  def lowBound2(): Unit = {
    locally {
      val (minY, maxY) = TileTank.getFluidHeight(4000, 2000, 0.5, 1, 0, isGaseous = true)
      assertEquals(0.75, minY)
      assertEquals(1.0, maxY)
    }
    locally {
      val (minY, maxY) = TileTank.getFluidHeight(4000, 4000, 0.5, 1, 0, isGaseous = true)
      assertEquals(0.5, minY)
      assertEquals(1.0, maxY)
    }
  }

  @Test
  def upBound1(): Unit = {
    locally {
      val (minY, maxY) = TileTank.getFluidHeight(4000, 2000, 0.0, 0.5, 0, isGaseous = false)
      assertEquals(0.0, minY)
      assertEquals(0.25, maxY)
    }
    locally {
      val (minY, maxY) = TileTank.getFluidHeight(4000, 4000, 0.0, 0.5, 0, isGaseous = false)
      assertEquals(0.0, minY)
      assertEquals(0.5, maxY)
    }
  }

  @Test
  def upBound2(): Unit = {
    locally {
      val (minY, maxY) = TileTank.getFluidHeight(4000, 2000, 0.0, 0.5, 0, isGaseous = true)
      assertEquals(0.25, minY)
      assertEquals(0.5, maxY)
    }
    locally {
      val (minY, maxY) = TileTank.getFluidHeight(4000, 4000, 0.0, 0.5, 0, isGaseous = true)
      assertEquals(0.0, minY)
      assertEquals(0.5, maxY)
    }
  }

  @ParameterizedTest
  @ValueSource(longs = Array(0L, 1L, 100, 250, 500))
  def minRatio1(amount: Long): Unit = {
    val (minY, maxY) = TileTank.getFluidHeight(4000, amount.toDouble, 0, 1, 1d / 8, isGaseous = false)
    assertEquals(0.0, minY)
    assertEquals(0.125, maxY)
  }

  @ParameterizedTest
  @ValueSource(longs = Array(0L, 1L, 100, 250, 500))
  def minRatio1Gas(amount: Long): Unit = {
    val (minY, maxY) = TileTank.getFluidHeight(4000, amount.toDouble, 0, 1, 1d / 8, isGaseous = true)
    assertEquals(0.875, minY)
    assertEquals(1, maxY)
  }

  @ParameterizedTest
  @ValueSource(longs = Array(1000L, 2000L, 4000L))
  def minRatio2(amount: Long): Unit = {
    val (minY, maxY) = TileTank.getFluidHeight(4000, amount.toDouble, 0, 1, 1d / 8, isGaseous = false)
    assertEquals(0.0, minY)
    assertEquals(amount / 4000d, maxY)
  }

  @ParameterizedTest
  @ValueSource(longs = Array(1000L, 2000L, 4000L))
  def minRatio2Gas(amount: Long): Unit = {
    val (minY, maxY) = TileTank.getFluidHeight(4000, amount.toDouble, 0, 1, 1d / 8, isGaseous = true)
    assertEquals(1 - amount / 4000d, minY)
    assertEquals(1, maxY)
  }

  @Test
  def defaultValue(): Unit = {
    val low: Double = Config.content.renderLowerBound.get()
    val up: Double = Config.content.renderUpperBound.get()
    val (minY, maxY) = TileTank.getFluidHeight(4000, 2000, low, up, low * 3, isGaseous = false)
    assertEquals(0.001, minY)
    assertEquals(0.5, maxY)
  }
}
