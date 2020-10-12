package com.kotori316.fluidtank

import cats._
import cats.implicits._
import com.google.gson.JsonElement
import com.kotori316.fluidtank.DynamicSerializable._
import com.kotori316.fluidtank.tiles.Tiers
import com.mojang.serialization.{JsonOps, Dynamic => SerializeDynamic}
import net.minecraft.nbt.{INBT, NBTDynamicOps}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class TierTest extends BeforeAllTest {
  implicit val eqNBT: Eq[INBT] = Eq.fromUniversalEquals
  implicit val eqJson: Eq[JsonElement] = Eq.fromUniversalEquals

  @Test def tierSerialize(): Unit = {
    val tiers = Tiers.list.toList
    val serializedJson = tiers.map(_.toJson)

    val d = serializedJson
      .map(new SerializeDynamic(JsonOps.INSTANCE, _))
      .map(DynamicSerializable[Tiers].deserialize)
    assertEquals(tiers, d)

    val serializedNBT = tiers.map(_.toNBT)
    val convertedToJson = serializedNBT.map(j => SerializeDynamic.convert(NBTDynamicOps.INSTANCE, JsonOps.INSTANCE, j))
    val convertedToNBT = serializedJson.map(j => SerializeDynamic.convert(JsonOps.INSTANCE, NBTDynamicOps.INSTANCE, j))
    assertTrue(serializedNBT === convertedToNBT)
    assertTrue(serializedJson === convertedToJson)
    assertEquals(serializedJson, convertedToJson)
    assertEquals(serializedNBT, convertedToNBT)
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
  def allInstanceIsNotSame(): Unit = {
    val as = Tiers.list.toList.combinations(2).map[Executable] { buf =>
      val List(a, b) = buf
      () => assertTrue(a =!= b, s"$a =!= $b")
    }.toSeq
    assertAll(as: _*)
  }

  @Test
  def codecAndSerialize(): Unit = {
    val tiers = Tiers.list.toList
    val tests = tiers.map[Executable] { t =>
      () =>
        assertEquals(java.util.Optional.of(Tiers.TierDynamicSerialize.serialize(t)(JsonOps.INSTANCE).getValue),
          Tiers.TierCodec.encodeStart(JsonOps.INSTANCE, t).result())
    }
    assertAll(tests: _*)
  }
}