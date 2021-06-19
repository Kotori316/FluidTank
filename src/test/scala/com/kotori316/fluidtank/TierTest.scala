package com.kotori316.fluidtank

import java.util.Optional

import cats._
import cats.implicits._
import com.google.gson.JsonElement
import com.kotori316.fluidtank.tiles.Tier
import net.minecraft.nbt.INBT
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class TierTest extends BeforeAllTest {
  implicit val eqNBT: Eq[INBT] = Eq.fromUniversalEquals
  implicit val eqJson: Eq[JsonElement] = Eq.fromUniversalEquals

  final def check(tier: Tier, name: String): Unit = {
    val deserialized = Tier.byName(name)
    assertEquals(Optional.of(tier), deserialized, f"Tier $tier, Actual $deserialized, name=$name")
  }

  @ParameterizedTest
  @EnumSource
  def tierSerialize1(tier: Tier): Unit = check(tier, tier.lowerName)

  @ParameterizedTest
  @EnumSource
  def tierSerialize2(tier: Tier): Unit = check(tier, tier.toString)

  @ParameterizedTest
  @EnumSource
  def tierSerialize3(tier: Tier): Unit = check(tier, tier.toString.toUpperCase)

  @ParameterizedTest
  @EnumSource
  def recipeIfTagDefined(t: Tier): Unit = {
    if (t.hasTagRecipe) {
      val s = t.tagName
      assertTrue(s.contains(":") && s.toLowerCase.contains(t.lowerName), s"Tag check of $t.")
    }
  }

  @Test
  def tierMaxIsLessThanNextTierMin(): Unit = {
    val max = Tier.list.groupBy(_.rank).map { case (i, value) => i -> value.maxBy(_.amount) }.toSeq.sortBy(_._1)
    val min = Tier.list.groupBy(_.rank).map { case (i, value) => i -> value.minBy(_.amount) }.toSeq.sortBy(_._1)
    assertAll((max.dropRight(1) zip min.drop(1)).flatMap {
      case ((maxL, maxAmount), (minL, minAmount)) =>
        List.apply[Executable](
          () => assertTrue(maxL < minL, s"Rank check of $maxL, $minL."),
          () => assertTrue(maxAmount.amount * 4 <= minAmount.amount, s"Amount check of $maxAmount, $minAmount."),
        )
    }: _*)
  }

  @Test
  def allInstanceIsNotSame(): Unit = assertAll(
    Tier.list.toList.combinations(2).map[Executable] { buf =>
      val List(a, b) = buf
      () => assertTrue(a =!= b, s"$a =!= $b")
    }.toSeq: _*
  )

}
