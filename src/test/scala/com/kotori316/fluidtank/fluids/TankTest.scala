package com.kotori316.fluidtank.fluids

import cats.syntax.eq._
import com.kotori316.fluidtank.BeforeAllTest
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import scala.jdk.javaapi.StreamConverters

object TankTest extends BeforeAllTest {
  @Test
  def makeInstance(): Unit = {
    val t = Tank(FluidAmount.BUCKET_WATER, 4000L)
    assertNotNull(t)
  }

  def emptyTankSeq(): java.util.stream.Stream[Tank] = {
    val tanks = for {
      f <- Seq(FluidAmount.BUCKET_WATER.setAmount(0), FluidAmount.BUCKET_LAVA.setAmount(0), FluidAmount.EMPTY.setAmount(5000))
      a <- Range.inclusive(0, 2000, 500)
    } yield Tank(f, a)
    StreamConverters.asJavaSeqStream(Tank.EMPTY +: tanks)
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.fluidtank.fluids.TankTest#emptyTankSeq"))
  def empty(tank: Tank): Unit = {
    assertTrue(tank.isEmpty, s"$tank is empty.")
  }

  def nonEmptyTankSeq(): java.util.stream.Stream[Tank] = {
    val tanks = for {
      f <- Seq(FluidAmount.BUCKET_WATER, FluidAmount.BUCKET_LAVA, FluidAmount.BUCKET_WATER.setAmount(2000), FluidAmount.BUCKET_LAVA.setAmount(2000))
      a <- Range.inclusive(0, 2000, 500)
    } yield Tank(f, a)
    StreamConverters.asJavaSeqStream(tanks)
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.fluidtank.fluids.TankTest#nonEmptyTankSeq"))
  def nonEmpty(tank: Tank): Unit = {
    assertFalse(tank.isEmpty, s"$tank is not empty.")
  }

  @Test
  def eq1(): Unit = {
    val a = Tank(FluidAmount.BUCKET_WATER, 5000)
    val b = Tank(FluidAmount.BUCKET_WATER, 5000)
    assertAll(
      () => assertEquals(a, b),
      () => assertTrue(cats.Eq[Tank].eqv(a, b)),
      () => assertTrue(a === b),
    )
  }

  def not5000Water(): java.util.stream.Stream[Tank] = {
    StreamConverters.asJavaSeqStream(
      Seq(0, 1000, 2000, 500, 10000).map(Tank(FluidAmount.BUCKET_WATER, _)) ++
        Seq(0, 1000, 2000, 5000, 10000).flatMap(i => Seq(FluidAmount.BUCKET_LAVA, FluidAmount.EMPTY).map(Tank(_, i)))
    )
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.fluidtank.fluids.TankTest#not5000Water"))
  def notEq1(t: Tank): Unit = {
    val a = Tank(FluidAmount.BUCKET_WATER, 5000)
    assertTrue(a =!= t, s"a=$a, t=$t")
  }
}
