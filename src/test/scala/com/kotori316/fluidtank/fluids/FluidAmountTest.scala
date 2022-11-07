package com.kotori316.fluidtank.fluids

import cats.implicits._
import com.kotori316.fluidtank.BeforeAllTest
import com.kotori316.fluidtank.fluids.FluidAmount.monoidFA
import net.minecraft.nbt.{CompoundTag, LongTag, StringTag}
import net.minecraft.world.item.{ItemStack, Items}
import net.minecraft.world.level.material.Fluids
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.{Nested, Test}
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import scala.util.chaining._

class FluidAmountTest extends BeforeAllTest {

  @Nested
  class Equals extends BeforeAllTest {
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
      val nbt1 = new CompoundTag()
      nbt1.putBoolean("b", true)
      nbt1.putString("name", "name")
      val nbt2 = new CompoundTag()
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
      val tag1 = new CompoundTag().tap(_.putInt("a", 1))
      val tag2 = new CompoundTag().tap(_.putInt("a", 1))
      val key1 = FluidKey(Fluids.WATER, Some(tag1))
      val key2 = FluidKey(Fluids.WATER, Some(tag2))
      assertTrue(key1 === key2)
      tag1.putString("b", "a")
      assertNotEquals(tag1, tag2)
      assertTrue(FluidKey(Fluids.WATER, Some(tag1)) =!= key1)
      assertTrue(key1 === key2)
    }
  }

  @Nested
  class Converts extends BeforeAllTest {

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeys"))
    def convertStackWithNBT(key: FluidKey): Unit = {
      if (key.isEmpty) {
        val amount = FluidAmount.fromStack(key.toAmount(1000).toStack)
        assertEquals(FluidAmount.EMPTY, amount)
        return
      }
      val nbt1 = new CompoundTag()
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

  @Nested
  class Empties extends BeforeAllTest {
    @Test
    def empty(): Unit = {
      val tag = Some {
        val a = new CompoundTag()
        a.putInt("e", 1)
        a.putInt("4", 2)
        a
      }
      assertAll(
        () => assertTrue(FluidAmount.EMPTY.isEmpty),
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

  @Nested
  class Monoid extends BeforeAllTest {
    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeysNonEmpty"))
    def times1(key: FluidKey): Unit = {
      val a = key.toAmount(FluidAmount.AMOUNT_BUCKET)
      assertAll(
        () => assertEquals(FluidAmount.AMOUNT_BUCKET, a.amount),
        () => assertEquals(FluidAmount.AMOUNT_BUCKET * 2, (a + a).amount),
        () => assertEquals(FluidAmount.AMOUNT_BUCKET * 2, (a * 2).amount),
        () => assertTrue((a |+| a) === a * 2, "a * 2"),
        () => assertEquals(FluidAmount.AMOUNT_BUCKET * 3, (a + a + a).amount),
        () => assertEquals(FluidAmount.AMOUNT_BUCKET * 3, (a * 3).amount),
        () => assertTrue((a |+| a |+| a) === a * 3, "a * 3"),
        () => assertEquals(FluidAmount.AMOUNT_BUCKET, a.amount),
      )
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeysNonEmpty"))
    def times2(key: FluidKey): Unit = {
      val a = key.toAmount(FluidAmount.AMOUNT_BUCKET)
      val a2 = a * 2
      val a3 = a * 3
      val a5 = a * 5
      val combine2 = cats.Semigroup[FluidAmount].combine(a, a)
      val combine3 = cats.Semigroup[FluidAmount].combineN(a, 3)
      val combine5 = cats.Semigroup[FluidAmount].combineN(a, 5)
      assertAll(
        () => assertEquals(a.setAmount(2000), cats.Semigroup[FluidAmount].combine(a, a)),
        () => assertEquals(a2, combine2),
        () => assertEquals(a.setAmount(3000), cats.Semigroup[FluidAmount].combineN(a, 3)),
        () => assertEquals(a3, combine3),
        () => assertEquals(a.setAmount(5000), cats.Semigroup[FluidAmount].combineN(a, 5)),
        () => assertEquals(a5, combine5),
      )
    }

    @Test
    def adder(): Unit = {
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
      val wl = FluidAmount.BUCKET_WATER |+| FluidAmount.BUCKET_LAVA
      val expected = FluidAmount.BUCKET_WATER.setAmount(2000)
      assertEquals(expected, wl)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeysNonEmpty"))
    def adderEmpty1(key: FluidKey): Unit = {
      val a = key.toAmount(3000)
      assertEquals(a, a + FluidAmount.EMPTY)
      assertEquals(a, FluidAmount.EMPTY + a)
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeysNonEmpty"))
    def adderEmpty2(key: FluidKey): Unit = {
      val a = key.toAmount(3000)
      val e = FluidAmount(FluidAmount.EMPTY.fluid, nbt = Option(new CompoundTag()), amount = 2000L)

      assertEquals(3000L, (e |+| a).amount)
      assertEquals(a, a |+| e)
      assertEquals(a, e |+| a)
    }

    @Test
    def adder0Fluid(): Unit = {
      val zeros = FluidAmountTest.fluidKeys().toSeq.map(_.toAmount(0)) :+ cats.Monoid[FluidAmount].empty

      assertTrue(zeros.forall(_.isEmpty))

      val nonZero = FluidAmountTest.fluidKeysNonEmpty().toSeq.map(_.toAmount(1000))

      val assertions: Seq[Executable] = for {
        zero <- zeros
        hasContent <- nonZero
        d <- List(true, false)
      } yield () => assertEquals(hasContent, if (d) zero + hasContent else hasContent + zero)

      assertAll(assertions: _*)
    }
  }

  @Nested
  class SerializeTag extends BeforeAllTest {
    @Test
    def serializeEmpty(): Unit = {
      val tag = FluidAmount.EMPTY.write(new CompoundTag())
      assertAll(
        () => assertEquals(StringTag.TYPE, tag.get(FluidAmount.NBT_fluid).getType),
        () => assertEquals(LongTag.TYPE, tag.get(FluidAmount.NBT_amount).getType),
        () => assertNull(tag.get(FluidAmount.NBT_tag)),
      )
    }

    @Test
    def serializeTagged(): Unit = {
      val fluid = FluidAmount(Fluids.WATER, 2000L, Some(new CompoundTag().tap(_.putInt("tt", 4))))
      val tag = fluid.write(new CompoundTag())
      assertAll(
        () => assertEquals(StringTag.TYPE, tag.get(FluidAmount.NBT_fluid).getType),
        () => assertEquals(LongTag.TYPE, tag.get(FluidAmount.NBT_amount).getType),
        () => assertEquals(CompoundTag.TYPE, tag.get(FluidAmount.NBT_tag).getType),
      )
    }

    @ParameterizedTest
    @MethodSource(Array("com.kotori316.fluidtank.fluids.FluidAmountTest#fluidKeyAmount"))
    def serializeNonEmpty(amount: Long, key: FluidKey): Unit = {
      val fluid = key.toAmount(amount)
      val tag = fluid.write(new CompoundTag())
      assertAll(
        () => assertEquals(StringTag.TYPE, tag.get(FluidAmount.NBT_fluid).getType),
        () => assertEquals(LongTag.TYPE, tag.get(FluidAmount.NBT_amount).getType),
        () => assertTrue(tag.get(FluidAmount.NBT_tag) == null || tag.get(FluidAmount.NBT_tag).getType == CompoundTag.TYPE),
      )
    }
  }
}

object FluidAmountTest {

  def empties(): Array[FluidAmount] = fluidKeys().map(k => k.toAmount(0L))

  def fluidKeys(): Array[FluidKey] = {
    val nbt = Option(new CompoundTag().tap(_.putInt("b", 6)))
    Array(
      FluidKey(Fluids.WATER, None), FluidKey(Fluids.LAVA, None), FluidKey(Fluids.EMPTY, None),
      FluidKey(Fluids.WATER, nbt), FluidKey(Fluids.LAVA, nbt), FluidKey(Fluids.EMPTY, nbt),
    )
  }

  def fluidKeysNonEmpty(): Array[FluidKey] = {
    val nbt1 = Option(new CompoundTag().tap(_.putInt("b", 6)))
    val nbt2 = Option(new CompoundTag().tap(_.putString("v", "a")))
    val nbt3 = for {a <- nbt1; aa = a.copy(); b <- nbt2} yield aa merge b
    Array(
      FluidKey(Fluids.WATER, None), FluidKey(Fluids.LAVA, None),
      FluidKey(Fluids.WATER, nbt1), FluidKey(Fluids.LAVA, nbt1),
      FluidKey(Fluids.WATER, nbt2), FluidKey(Fluids.LAVA, nbt2),
      FluidKey(Fluids.WATER, nbt3), FluidKey(Fluids.LAVA, nbt3),
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

}
