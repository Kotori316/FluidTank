package com.kotori316.fluidtank

import cats._
import cats.implicits._
import com.google.gson.JsonElement
import com.kotori316.fluidtank.DynamicSerializable._
import com.kotori316.fluidtank.tiles.Tiers
import com.mojang.datafixers.Dynamic
import com.mojang.datafixers.types.JsonOps
import net.minecraft.nbt.{INBT, NBTDynamicOps}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class TierTest {
  implicit val eqNBT: Eq[INBT] = Eq.fromUniversalEquals
  implicit val eqJson: Eq[JsonElement] = Eq.fromUniversalEquals

  @Test def tierSerialize(): Unit = {
    val tiers = Tiers.list.toList
    val serializedJson = tiers.map(_.toJson)

    val d = serializedJson
      .map(new Dynamic(JsonOps.INSTANCE, _))
      .map(DynamicSerializable[Tiers].deserialize)
    assertEquals(tiers, d)

    val serializedNBT = tiers.map(_.toNBT)
    val convertedToJson = serializedNBT.map(j => Dynamic.convert(NBTDynamicOps.INSTANCE, JsonOps.INSTANCE, j))
    val convertedToNBT = serializedJson.map(j => Dynamic.convert(JsonOps.INSTANCE, NBTDynamicOps.INSTANCE, j))
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
}