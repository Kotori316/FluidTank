package com.kotori316.fluidtank.fluids

import cats.implicits.catsSyntaxEq
import com.kotori316.fluidtank._
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.material.Fluids
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import scala.reflect.ClassTag

class GenericAmountTest extends BeforeAllTest {

  import GenericAmountTest.GenericAccessString

  @Test
  def otherTypeOfAmount(): Unit = {
    val a1 = new GenericAmount("A", 15, Option.empty)
    val f1 = new FluidAmount(Fluids.WATER, 15, Option.empty)

    assertNotEquals(a1.asInstanceOf[GenericAmount[_]], f1.asInstanceOf[GenericAmount[_]])
  }

  @Test
  def setAmount(): Unit = {
    val a1 = new GenericAmount("A", 15, Option.empty)
    val a2 = a1.setAmount(1000)

    assertEquals(15, a1.amount)
    assertTrue(a2 === new GenericAmount("A", 1000, Option.empty))
    assertEquals(a2, new GenericAmount("A", 1000, Option.empty))
  }

  @Test
  def respectNbt(): Unit = {
    val a1 = new GenericAmount("A", 15, Option.empty)
    val a2 = new GenericAmount("A", 15, Option(new CompoundTag()))

    assertTrue(a1 =!= a2)
  }

  @ParameterizedTest
  @ValueSource(longs = Array(0, -1, 1, 100, 15))
  def hash(amount: Long): Unit = {
    val a1 = new GenericAmount("A", amount, Option.empty)
    val a2 = new GenericAmount("A", amount, Option.empty)

    assertEquals(a1.##, a2.##)
  }

}

object GenericAmountTest {

  implicit val GenericAccessString: GenericAccess[String] = GenericAccessStringImpl

  object GenericAccessStringImpl extends GenericAccess[String] {
    override def isEmpty(a: String): Boolean = a.isEmpty

    override def isGaseous(a: String): Boolean = a.startsWith(" ")

    override def getKey(a: String): ResourceLocation = new ResourceLocation(a.toLowerCase)

    override def empty: String = ""

    override def write(amount: GenericAmount[String], tag: CompoundTag): CompoundTag = {
      tag.putString("content", amount.c)
      tag
    }

    override def classTag: ClassTag[String] = implicitly[ClassTag[String]]
  }
}
