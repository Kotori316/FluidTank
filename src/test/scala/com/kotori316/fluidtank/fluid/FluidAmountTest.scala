package com.kotori316.fluidtank.fluid

import com.kotori316.fluidtank.BeforeAllTest
import com.kotori316.fluidtank.fluids.FluidAmount
import net.minecraft.world.level.material.Fluids
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertTrue}
import org.junit.jupiter.api.{DynamicTest, Test, TestFactory}

import scala.jdk.javaapi.CollectionConverters

private[fluid] class FluidAmountTest extends BeforeAllTest {
  @Test
  def equiv(): Unit = {
    val a = FluidAmount(Fluids.WATER, FluidAmount.AMOUNT_BUCKET, None)
    assertTrue(a == a, "Equal to itself")
    val b = FluidAmount(Fluids.WATER, FluidAmount.AMOUNT_BUCKET, None)
    assertTrue(a == b, "Eq")
    assertEquals(a, b, "Eq2")
    assertTrue(a fluidEqual b)
    assertTrue(b fluidEqual a)
  }

  @Test
  def empty(): Unit = {
    assertAll(
      () => assertTrue(FluidAmount.EMPTY.isEmpty),
      () => assertTrue(FluidAmount.EMPTY.copy().isEmpty),
      // () => assertTrue(FluidAmount(FluidKeys.EMPTY.withAmount(BCAmount.BUCKET.mul(10))).isEmpty), // Amount of empty fluid is 0.
      () => assertTrue(FluidAmount.BUCKET_LAVA.setAmount(0).isEmpty),
      () => assertTrue((FluidAmount.BUCKET_WATER - FluidAmount.BUCKET_WATER).isEmpty),
      // () => assertTrue(FluidAmount.fromItem(new ItemStack(Items.BUCKET)).isEmpty), // Not available in Fabric
    )
  }

  @Test
  def adder(): Unit = {
    {
      val a = FluidAmount.BUCKET_WATER
      assertEquals(FluidAmount.AMOUNT_BUCKET, a.amount)
      assertEquals(FluidAmount.AMOUNT_BUCKET * 2, (a + a).amount)
      assertEquals(FluidAmount.AMOUNT_BUCKET * 3, (a + a + a).amount)
      assertEquals(FluidAmount.AMOUNT_BUCKET, a.amount)
    }
    locally {
      val wl = FluidAmount.BUCKET_WATER + FluidAmount.BUCKET_LAVA
      assertTrue(FluidAmount.BUCKET_WATER.setAmount(FluidAmount.AMOUNT_BUCKET * 2) == wl)
      assertEquals(Fluids.WATER, wl.fluid)
    }
    locally {
      val lw = FluidAmount.BUCKET_LAVA + FluidAmount.BUCKET_WATER
      assertTrue(FluidAmount.BUCKET_LAVA.setAmount(FluidAmount.AMOUNT_BUCKET * 2) == lw)
      assertEquals(Fluids.LAVA, lw.fluid)
    }
  }

  @Test
  def adderEmpty(): Unit = {
    locally {
      val a = FluidAmount.BUCKET_WATER.setAmount(3000L)
      assertEquals(a, a + FluidAmount.EMPTY)
      assertEquals(a, FluidAmount.EMPTY + a)
    }
    locally {
      val a = FluidAmount.BUCKET_LAVA.setAmount(3000L)
      val e = FluidAmount.EMPTY.setAmount(2000L)

      assertEquals(3000L, (e + a).amount)
      assertEquals(a, a + e)
      assertEquals(a, e + a)
    }
  }

  @TestFactory
  def adder0Fluid() = {
    val zeros = List(FluidAmount.BUCKET_LAVA.setAmount(0), FluidAmount.BUCKET_WATER.setAmount(0))
    assertTrue(zeros.forall(_.isEmpty))

    val nonZero = List(FluidAmount.BUCKET_WATER, FluidAmount.BUCKET_LAVA)

    val assertions1: List[DynamicTest] = for {
      zero <- zeros
      hasContent <- nonZero
      d <- List(true, false)
      left = if (d) zero else hasContent
      right = if (d) hasContent else zero
    } yield DynamicTest.dynamicTest(s"$left + $right", () => assertEquals(hasContent, left + right))
    val assertions2: List[DynamicTest] = for {
      zero <- List(FluidAmount.EMPTY)
      hasContent <- nonZero
      d <- List(true, false)
    } yield DynamicTest.dynamicTest(if (d) s"$zero + $hasContent" else s"$hasContent + $zero",
      () => assertEquals(hasContent, if (d) zero + hasContent else hasContent + zero))

    CollectionConverters.asJava(assertions1 ++ assertions2)
  }
}
