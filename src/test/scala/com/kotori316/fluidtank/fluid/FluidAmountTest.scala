package com.kotori316.fluidtank.fluid

import cats.implicits._
import com.kotori316.fluidtank.BeforeAllTest
import com.kotori316.fluidtank.fluids.FluidAmount
import net.minecraft.fluid.Fluids
import net.minecraft.item.{ItemStack, Items}
import net.minecraft.nbt.CompoundNBT
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import scala.util.chaining._

private[fluid] final class FluidAmountTest extends BeforeAllTest {
  @Test
  def equiv(): Unit = {
    val a = FluidAmount(Fluids.WATER, 1000, None)
    assertTrue(a === a, "Equal to itself")
    val b = FluidAmount(Fluids.WATER, 1000, None)
    assertTrue(a === b, "Eq")
    assertEquals(a, b, "Eq2")
    assertTrue(a fluidEqual b)
    assertTrue(b fluidEqual a)
  }

  @Test
  def eqNbt(): Unit = {
    val nbt1 = new CompoundNBT()
    nbt1.putBoolean("b", true)
    nbt1.putString("name", "name")
    val nbt2 = new CompoundNBT()
    nbt2.putBoolean("b", false)
    nbt2.putString("name", "tag")
    assertNotEquals(nbt1, nbt2)

    val a = FluidAmount(Fluids.WATER, 1000, Some(nbt1))
    val b = FluidAmount.BUCKET_WATER
    assertTrue(a =!= b, "Not eq due to nbt tag.")
  }

  @Test
  def convertStackWithNBT(): Unit = {
    val nbt1 = new CompoundNBT()
    nbt1.putBoolean("b", true)
    nbt1.putString("name", "name")
    val a = FluidAmount(Fluids.WATER, 1000, Some(nbt1))

    assertTrue(a === FluidAmount.fromStack(a.toStack), s"NBT data${a -> FluidAmount.fromStack(a.toStack)}")
  }

  @Test
  def convertStack(): Unit = {
    val fa = for {
      f <- List(FluidAmount.BUCKET_WATER, FluidAmount.BUCKET_LAVA)
      amount <- Iterator.iterate(1L)(_ * 10).take(10).filter(l => l < Int.MaxValue)
    } yield f.setAmount(amount)

    assertAll(fa.map[Executable] { f =>
      () => assertTrue(f === FluidAmount.fromStack(f.toStack), s"Reconvert $f, ${f.toStack}")
    }: _*)
  }

  @Test
  def empty(): Unit = {
    val tag = Some {
      val a = new CompoundNBT()
      a.putInt("e", 1)
      a.putInt("4", 2)
      a
    }
    assertAll(
      () => assertTrue(FluidAmount.EMPTY.isEmpty),
      () => assertTrue(FluidAmount.EMPTY.copy(nbt = tag).isEmpty),
      () => assertTrue(FluidAmount(Fluids.EMPTY, 10000, None).isEmpty),
      () => assertTrue(FluidAmount(Fluids.EMPTY, 10000, tag).isEmpty),
      () => assertTrue(FluidAmount(Fluids.LAVA, 0, None).isEmpty),
      () => assertTrue(FluidAmount.BUCKET_LAVA.setAmount(0).isEmpty),
      () => assertTrue((FluidAmount.BUCKET_WATER - FluidAmount.BUCKET_WATER).isEmpty),
      () => assertTrue(FluidAmount.fromItem(new ItemStack(Items.BUCKET)).isEmpty),
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
      assertTrue(FluidAmount.BUCKET_WATER.setAmount(FluidAmount.AMOUNT_BUCKET * 2) === wl)
      assertEquals(Fluids.WATER, wl.fluid)
    }
    locally {
      val lw = FluidAmount.BUCKET_LAVA + FluidAmount.BUCKET_WATER
      assertTrue(FluidAmount.BUCKET_LAVA.setAmount(FluidAmount.AMOUNT_BUCKET * 2) === lw)
      assertEquals(Fluids.LAVA, lw.fluid)
    }
  }

  @Test
  def adderEmpty(): Unit = {
    val tag = Option(new CompoundNBT().tap(_.putInt("a", 1)).tap(_.putBoolean("b", true)))
    locally {
      val a = FluidAmount.BUCKET_WATER.copy(nbt = tag, amount = 3000L)
      assertEquals(a, a + FluidAmount.EMPTY)
      assertEquals(a, FluidAmount.EMPTY + a)
    }
    locally {
      val a = FluidAmount.BUCKET_LAVA.copy(nbt = tag, amount = 3000L)
      val e = FluidAmount.EMPTY.copy(nbt = Option(new CompoundNBT()), amount = 2000L)

      assertEquals(3000L, (e + a).amount)
      assertEquals(a, a + e)
      assertEquals(a, e + a)
    }
  }

  @Test
  def adder0Fluid(): Unit = {
    val zeros = List(FluidAmount.EMPTY, FluidAmount.BUCKET_LAVA.setAmount(0), FluidAmount.BUCKET_WATER.setAmount(0), FluidAmount.BUCKET_MILK.setAmount(0))
    assertTrue(zeros.forall(_.isEmpty))

    val nonZero = List(FluidAmount.BUCKET_WATER, FluidAmount.BUCKET_MILK, FluidAmount.BUCKET_LAVA)

    val assertions: List[Executable] = for {
      zero <- zeros
      hasContent <- nonZero
      d <- List(true, false)
    } yield () => assertEquals(hasContent, if (d) zero + hasContent else hasContent + zero)

    assertAll(assertions: _*)
  }
}
