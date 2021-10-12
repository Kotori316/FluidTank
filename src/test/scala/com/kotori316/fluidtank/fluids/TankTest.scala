package com.kotori316.fluidtank.fluids

import cats.syntax.eq._
import com.kotori316.fluidtank.BeforeAllTest
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

object TankTest extends BeforeAllTest {
  @Test
  def makeInstance(): Unit = {
    val t = Tank(FluidAmount.BUCKET_WATER, 4000L)
    assertNotNull(t)
  }

  @Test
  def empty(): Unit = {
    assertTrue(Tank.EMPTY.isEmpty)

    val es: Seq[Executable] = for {
      f <- Seq(FluidAmount.BUCKET_WATER.setAmount(0), FluidAmount.BUCKET_LAVA.setAmount(0),
        FluidAmount.EMPTY.setAmount(5000))
      a <- Seq(0, 1000, 2000)
      tank = Tank(f, a)
    } yield () => assertTrue(tank.isEmpty, s"$tank is empty.")
    assertAll(es: _*)
  }

  @Test
  def nonEmpty(): Unit = {
    val es: Seq[Executable] = for {
      f <- Seq(FluidAmount.BUCKET_WATER, FluidAmount.BUCKET_LAVA,
        FluidAmount.BUCKET_WATER.setAmount(2000), FluidAmount.BUCKET_LAVA.setAmount(2000))
      a <- Seq(0, 1000, 2000)
      tank = Tank(f, a)
    } yield () => assertFalse(tank.isEmpty, s"$tank is not empty.")
    assertAll(es: _*)
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

  @Test
  def notEq1(): Unit = {
    val a = Tank(FluidAmount.BUCKET_WATER, 5000)
    assertAll(
      Seq(0, 1000, 2000, 500, 10000)
        .map(Tank(FluidAmount.BUCKET_WATER, _))
        .map[Executable](t => () => assertTrue(a =!= t)): _*
    )
    assertAll(
      Seq(0, 1000, 2000, 5000, 10000)
        .flatMap(i => Seq(FluidAmount.BUCKET_LAVA, FluidAmount.EMPTY).map(Tank(_, i)))
        .map[Executable](t => () => assertTrue(a =!= t)): _*
    )
  }
}
