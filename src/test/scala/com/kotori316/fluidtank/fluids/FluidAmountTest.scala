package com.kotori316.fluidtank.fluids

import cats.implicits._
import com.kotori316.fluidtank.BeforeAllTest
import net.minecraft.fluid.Fluids
import net.minecraft.item.{ItemStack, Items}
import net.minecraft.nbt.CompoundNBT
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import scala.util.chaining._

object FluidAmountTest {

  def empties(): Array[FluidAmount] = fluidKeys().map(k => k.toAmount(0L))

  def fluidKeys(): Array[FluidKey] = {
    val nbt = Option(new CompoundNBT().tap(_.putInt("b", 6)))
    Array(
      FluidKey(Fluids.WATER, None), FluidKey(Fluids.LAVA, None), FluidKey(Fluids.EMPTY, None),
      FluidKey(Fluids.WATER, nbt), FluidKey(Fluids.LAVA, nbt), FluidKey(Fluids.EMPTY, nbt),
    )
  }

  def fluidKeys2(): Array[Array[FluidKey]] = fluidKeys()
    .combinations(2)
    .map(_.toArray)
    .toArray

  def fluidKeyAmount(): Array[Object] = for {
    amount <- Array(0, 1000, 5000, Int.MaxValue, Int.MaxValue + 8L, Long.MaxValue)
    fluid <- fluidKeys()
  } yield Array(amount, fluid)

  def fluidKey2Amount(): Array[Object] = for {
    amount <- Array(0, 1000, 5000, Int.MaxValue, Int.MaxValue + 8L, Long.MaxValue)
    keys <- fluidKeys2()
  } yield Array(amount, keys(0), keys(1))

  def stackFluids(): Array[FluidAmount] = {
    val l = for {
      f <- List(FluidAmount.BUCKET_WATER, FluidAmount.BUCKET_LAVA)
      amount <- Iterator.iterate(1L)(_ * 10).take(10).filter(l => l < Int.MaxValue)
    } yield f.setAmount(amount)
    l.toArray
  }

  object Equals extends BeforeAllTest {
    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeys"))
    def equiv(key: FluidKey): Unit = {
      val a = key.toAmount(1000)
      val b = key.toAmount(1000)
      assertAll(
        () => assertTrue(a === a, "Equal to itself"),
        () => assertTrue(a === b, "Eq"),
        () => assertEquals(a, b, "Eq2"),
        () => assertTrue(a fluidEqual b),
        () => assertTrue(b fluidEqual a),
      )
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeys"))
    def eq2(key: FluidKey): Unit = {
      val a = key.toAmount(1000)
      val b = key.toAmount(2000)
      assertAll(
        () => assertNotEquals(a, b),
        () => assertTrue(a fluidEqual b),
        () => assertTrue(b fluidEqual a),
      )
    }

    @Test
    def eq3(): Unit = {
      val a = FluidAmount.EMPTY
      val b = FluidAmount(Fluids.WATER, 0, None)
      val c = FluidAmount.EMPTY.setAmount(2000)
      assertNotEquals(a, b)
      assertNotEquals(a, c)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKey2Amount"))
    def eq4(amount: Long, key1: FluidKey, key2: FluidKey): Unit = {
      val a = key1.toAmount(amount)
      val b = key2.toAmount(amount)
      assertFalse(a === b, f"Eq of $a and $b")
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeyAmount"))
    def eqNbt(amount: Long, key: FluidKey): Unit = {
      val nbt1 = new CompoundNBT()
      nbt1.putBoolean("b", true)
      nbt1.putString("name", "name")
      val nbt2 = new CompoundNBT()
      nbt2.putBoolean("b", false)
      nbt2.putString("name", "tag")
      assertNotEquals(nbt1, nbt2)

      val a = key.copy(tag = Some(nbt1)).toAmount(amount)
      val b = key.toAmount(amount)
      val c = key.copy(tag = Some(nbt2)).toAmount(amount)
      assertAll(
        () => assertTrue(a =!= b, s"Not eq due to nbt tag. $a, $b"),
        () => assertTrue(a =!= c, s"Not eq due to nbt tag. $a, $c"),
        () => assertTrue(b =!= c, s"Not eq due to nbt tag. $b, $c"),
      )
    }

    @Test
    def keyEqual(): Unit = {
      val tag1 = new CompoundNBT().tap(_.putInt("a", 1))
      val tag2 = new CompoundNBT().tap(_.putInt("a", 1))
      val key1 = FluidKey(Fluids.WATER, Some(tag1))
      val key2 = FluidKey(Fluids.WATER, Some(tag2))
      assertTrue(key1 === key2)
      tag1.putString("b", "a")
      assertNotEquals(tag1, tag2)
      assertTrue(FluidKey(Fluids.WATER, Some(tag1)) =!= key1)
      assertTrue(key1 === key2)
    }
  }

  object Converts extends BeforeAllTest {

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeys"))
    def convertStackWithNBT(key: FluidKey): Unit = {
      if (key.isEmpty) {
        assertEquals(FluidAmount.EMPTY, FluidAmount.fromStack(key.toAmount(1000).toStack))
        return
      }
      val nbt1 = new CompoundNBT()
      nbt1.putBoolean("b", true)
      nbt1.putString("name", "name")
      val a = key.copy(tag = Some(nbt1)).toAmount(1000)

      assertTrue(a === FluidAmount.fromStack(a.toStack), s"NBT data${a -> FluidAmount.fromStack(a.toStack)}")
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#stackFluids"))
    def convertStack(f: FluidAmount): Unit = {
      assertTrue(f === FluidAmount.fromStack(f.toStack), s"Reconvert $f, ${f.toStack}")
    }

  }

  object Empties extends BeforeAllTest {
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

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#empties"))
    def emptyTest(maybeEmpty: FluidAmount): Unit = {
      assertTrue(maybeEmpty.isEmpty)
    }
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
  def adder2(): Unit = {
    {
      val a = FluidAmount.BUCKET_LAVA
      assertEquals(a.setAmount(2000), cats.Semigroup[FluidAmount].combine(a, a))
      assertEquals(a.setAmount(3000), cats.Semigroup[FluidAmount].combineN(a, 3))
      assertEquals(a.setAmount(5000), cats.Semigroup[FluidAmount].combineN(a, 5))
    }
    {
      val wl = FluidAmount.BUCKET_WATER |+| FluidAmount.BUCKET_LAVA
      assertEquals(FluidAmount.BUCKET_WATER.setAmount(2000), wl)
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
    val zeros = List(FluidAmount.EMPTY, FluidAmount.BUCKET_LAVA.setAmount(0), FluidAmount.BUCKET_WATER.setAmount(0), FluidAmount.BUCKET_MILK.setAmount(0),
      cats.Monoid[FluidAmount].empty)
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
