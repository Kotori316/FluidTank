package com.kotori316.fluidtank

import cats._
import cats.implicits._
import com.google.gson.JsonElement
import com.kotori316.fluidtank.tiles.Tiers
import net.minecraft.nbt.INBT
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

import scala.annotation.tailrec

class TierTest extends BeforeAllTest {
  implicit val eqNBT: Eq[INBT] = Eq.fromUniversalEquals
  implicit val eqJson: Eq[JsonElement] = Eq.fromUniversalEquals

  @tailrec
  final def check(tiers: List[Tiers], deserialized: List[Option[Tiers]], names: List[String]): Unit = {
    val t :: tRest = tiers
    val d :: dRest = deserialized
    val n :: nRest = names
    assertEquals(Option(t), d, f"Tier $t, Actual $d, name=$n")
    if (tRest.nonEmpty && dRest.nonEmpty && nRest.nonEmpty)
      check(tRest, dRest, nRest)
  }

  @Test
  def tierSerialize1(): Unit = {
    val tiers = Tiers.list.toList
    val names = tiers.map(_.lowerName)
    val deserialized = names.map(Tiers.byName)

    check(tiers, deserialized, names)
  }

  @Test
  def tierSerialize2(): Unit = {
    val tiers = Tiers.list.toList
    val names = tiers.map(_.toString)
    val deserialized = names.map(Tiers.byName)

    check(tiers, deserialized, names)
  }

  @Test
  def tierSerialize3(): Unit = {
    val tiers = Tiers.list.toList
    val names = tiers.map(_.toString.toUpperCase)
    val deserialized = names.map(Tiers.byName)

    check(tiers, deserialized, names)
  }

  @Test
  def recipeIfTagDefined(): Unit = {
    val tiers = Tiers.list.toList
    val tests = tiers.map[Executable] { t =>
      val predicate: String => Boolean = if (t.hasTagRecipe) s => s.contains(":") && s.toLowerCase.contains(t.lowerName) else _ => true
      () => assertTrue(predicate(t.tagName), s"Tag check of $t.")
    }
    assertAll(tests: _*)
  }

  @Test
  def tierMaxIsLessThanNextTierMin(): Unit = {
    val max = Tiers.list.groupBy(_.rank).map { case (i, value) => i -> value.maxBy(_.amount) }.toSeq.sortBy(_._1)
    val min = Tiers.list.groupBy(_.rank).map { case (i, value) => i -> value.minBy(_.amount) }.toSeq.sortBy(_._1)
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
    Tiers.list.toList.combinations(2).map[Executable] { buf =>
      val List(a, b) = buf
      () => assertTrue(a =!= b, s"$a =!= $b")
    }.toSeq: _*
  )

}
