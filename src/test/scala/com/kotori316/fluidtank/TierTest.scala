package com.kotori316.fluidtank

import java.util.Optional

import cats._
import cats.implicits._
import com.google.gson.JsonElement
import com.kotori316.fluidtank.tiles.Tier
import net.minecraft.nbt.Tag
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.{EnumSource, MethodSource}

import scala.jdk.javaapi.StreamConverters

class TierTest extends BeforeAllTest {
  implicit val eqNBT: Eq[Tag] = Eq.fromUniversalEquals
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

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.fluidtank.TierTest#ranks"))
  def tierMaxIsLessThanNextTierMin(less: Tier, grater: Tier): Unit = {
    assertAll(
      () => assertTrue(less.rank < grater.rank, s"Rank check of ${less.rank}, ${grater.rank}."),
      () => assertTrue(less.amount * 4 <= grater.amount, s"Amount check of $less, $grater."),
    )
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.fluidtank.TierTest#combinationOfTier"))
  def allInstanceIsNotSame(a: Tier, b: Tier): Unit = {
    assertTrue(a =!= b, s"$a =!= $b")
  }

  @ParameterizedTest
  @EnumSource(classOf[Tier], names = Array("WOOD", "STONE", "IRON", "GOLD", "DIAMOND", "EMERALD", "STAR", "CREATIVE", "VOID", "COPPER"))
  def availableInVanilla(tier: Tier): Unit = {
    assertTrue(tier.isAvailableInVanilla, s"$tier must be available in vanilla.")
  }
}

object TierTest {
  def ranks(): java.util.stream.Stream[Array[Object]] = {
    val graterTiers = Tier.list.groupBy(_.rank).map { case (_, value) => value.minBy(_.amount) }.toSeq.sortBy(_.rank)
    val lessTiers = Tier.list.groupBy(_.rank).map { case (_, value) => value.maxBy(_.amount) }.toSeq.sortBy(_.rank)
    StreamConverters.asJavaSeqStream((lessTiers.init zip graterTiers.tail).map { case (less, grater) => Array(less, grater) })
  }

  def combinationOfTier(): java.util.stream.Stream[Array[Object]] = {
    StreamConverters.asJavaSeqStream(
      Tier.list.toList.combinations(2).map(_.toArray)
    )
  }
}
