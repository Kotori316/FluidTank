package com.kotori316.fluidtank.tiles

import java.util.Collections

import cats.kernel.Eq
import com.mojang.datafixers
import com.mojang.datafixers.types.DynamicOps
import net.minecraft.nbt.CompoundNBT

import scala.collection.mutable
import scala.jdk.CollectionConverters._

class Tiers private(val rank: Int, buckets: Int, override val toString: String, val tagName: String, val hasTagRecipe: Boolean) {
  val amount: Long = buckets * 1000
  Tiers.list.append(this)

  override def hashCode(): Int = rank.hashCode ^ amount.hashCode ^ toString.hashCode

  def toNBTTag: CompoundNBT = this.asInstanceOf[Tiers].toNBT.asInstanceOf[CompoundNBT]
}

object Tiers {
  val list = mutable.ArrayBuffer.empty[Tiers]

  val Invalid = new Tiers(0, 0, "Invalid", "Unknown", hasTagRecipe = false)
  val WOOD = new Tiers(1, 1 << 2, "Wood", "minecraft:logs", hasTagRecipe = false)
  val STONE = new Tiers(2, 1 << 4, "Stone", "forge:stone", hasTagRecipe = true)
  val IRON = new Tiers(3, 1 << 8, "Iron", "forge:ingots/iron", hasTagRecipe = true)
  val GOLD = new Tiers(4, 1 << 12, "Gold", "forge:ingots/gold", hasTagRecipe = true)
  val DIAMOND = new Tiers(5, 1 << 14, "Diamond", "forge:gems/diamond", hasTagRecipe = true)
  val EMERALD = new Tiers(6, 1 << 16, "Emerald", "forge:gems/emerald", hasTagRecipe = true)
  val STAR = new Tiers(7, 1 << 20, "Star", "forge:nether_stars", hasTagRecipe = true)
  val CREATIVE: Tiers = new Tiers(8, 0, "Creative", "Unknown", hasTagRecipe = false) {
    override val amount: Long = Long.MaxValue
  }

  val COPPER = new Tiers(2, 1 << 5, "Copper", "forge:ingots/copper", hasTagRecipe = true)
  val TIN = new Tiers(2, 1 << 6, "Tin", "forge:ingots/tin", hasTagRecipe = true)
  val BRONZE = new Tiers(3, 1 << 9, "Bronze", "forge:ingots/bronze", hasTagRecipe = true)
  val LEAD = new Tiers(3, 1 << 8, "Lead", "forge:ingots/lead", hasTagRecipe = true)
  val SILVER = new Tiers(3, 1 << 10, "Silver", "forge:ingots/silver", hasTagRecipe = true)

  val nameToTierMap = list.map(t => (t.toString, t)).toMap

  def jList: java.util.List[Tiers] = Collections.unmodifiableList(list.asJava)

  def fromNBT(nbt: CompoundNBT): Tiers = TierDynamicSerialize.deserializeFromNBT(nbt)

  implicit val EqTiers: Eq[Tiers] = Eq.fromUniversalEquals

  import com.kotori316.fluidtank._

  implicit val TierDynamicSerialize: DynamicSerializable[Tiers] = new DynamicSerializable[Tiers] {
    override def serialize[DataType](t: Tiers)(ops: DynamicOps[DataType]): datafixers.Dynamic[DataType] = {
      val map = ops.set(ops.emptyMap(), "string", ops.createString(t.toString))
      new datafixers.Dynamic[DataType](ops, map)
    }

    override def deserialize[DataType](d: datafixers.Dynamic[DataType]): Tiers = {
      (d.get("string").asString().asScala orElse d.asString().asScala)
        .flatMap(s => list.find(_.toString.equalsIgnoreCase(s)))
        .getOrElse {
          FluidTank.LOGGER.error(s"The tag '${d.getValue}' isn't have tier data.", new IllegalArgumentException("Invalid tier name."))
          WOOD
        }
    }
  }
}
