package com.kotori316.fluidtank.fluids

import cats.kernel.laws.discipline.{HashTests, MonoidTests}
import com.kotori316.fluidtank.BeforeAllTest
import net.minecraft.fluid.Fluids
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.function.Executable
import org.scalacheck.util.{ConsoleReporter, Pretty}
import org.scalacheck.{Arbitrary, Cogen, Gen, Properties}

object FluidAmountMonoidLawTest extends BeforeAllTest {
  implicit val arb: Arbitrary[FluidAmount] = Arbitrary[FluidAmount] {
    for {
      f <- Gen.oneOf(Seq(Fluids.LAVA, Fluids.WATER, Fluids.EMPTY))
      l <- if (f == Fluids.EMPTY) Gen.const(0L) else Gen.posNum[Long]
    } yield FluidAmount(f, l, None)
  }

  implicit val coGenFA: Cogen[FluidAmount] = Cogen(_.amount)

  private def executeTest(properties: Properties): Unit = {
    val parameters = org.scalacheck.Test.Parameters.default.withTestCallback(ConsoleReporter(1))
    val results = org.scalacheck.Test.checkProperties(parameters, properties)
    val executions = results.map[Executable] { case (name, result) =>
      if (result.passed) () => ()
      else () => fail(s"Test of [$name] failed. ${Pretty.pretty(result)}")
    }.toSeq
    assertAll(executions: _*)
  }

  @org.junit.jupiter.api.Test
  def hash(): Unit = executeTest(HashTests[FluidAmount].hash.all)

  @org.junit.jupiter.api.Test
  def monoid(): Unit = executeTest(MonoidTests[FluidAmount].monoid.all)
}
