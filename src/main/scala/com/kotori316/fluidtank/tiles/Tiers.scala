package com.kotori316.fluidtank.tiles

import java.util.Collections

import cats.kernel.Hash
import com.kotori316.fluidtank.DynamicSerializable._
import com.kotori316.fluidtank._
import com.mojang.datafixers
import com.mojang.datafixers.types.DynamicOps
import net.minecraft.nbt.INBT

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

class Tiers private(val rank: Int, buckets: Int, override val toString: String, val tagName: String, val hasTagRecipe: Boolean) {
  val lowerName: String = toString.toLowerCase
  val amount: Long = buckets * 1000
  Tiers.list.append(this)

  override def hashCode(): Int = rank.hashCode ^ amount.hashCode ^ toString.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case that: Tiers => this.rank == that.rank && this.amount == that.amount && this.toString == that.toString
    case _ => false
  }

  def toNBTTag: INBT = this.asInstanceOf[Tiers].toNBT
}

object Tiers {
  val list: ArrayBuffer[Tiers] = mutable.ArrayBuffer.empty[Tiers]

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
  val VOID = new Tiers(0, 0, "Void", "Unknown", hasTagRecipe = false)

  val COPPER = new Tiers(2, 1 << 5, "Copper", "forge:ingots/copper", hasTagRecipe = true)
  val TIN = new Tiers(2, 1 << 6, "Tin", "forge:ingots/tin", hasTagRecipe = true)
  val BRONZE = new Tiers(3, 1 << 9, "Bronze", "forge:ingots/bronze", hasTagRecipe = true)
  val LEAD = new Tiers(3, 1 << 8, "Lead", "forge:ingots/lead", hasTagRecipe = true)
  val SILVER = new Tiers(3, 1 << 10, "Silver", "forge:ingots/silver", hasTagRecipe = true)

  def jList: java.util.List[Tiers] = Collections.unmodifiableList(list.asJava)

  def fromNBT(nbt: INBT): Tiers = TierDynamicSerialize.deserializeFromNBT(nbt)

  def byName(s: String): Option[Tiers] = list.find(_.toString.equalsIgnoreCase(s))

  implicit val EqTiers: Hash[Tiers] = Hash.fromUniversalHashCode

  implicit val TierDynamicSerialize: DynamicSerializable[Tiers] = new DynamicSerializable[Tiers] {
    override def serialize[DataType](t: Tiers)(ops: DynamicOps[DataType]): datafixers.Dynamic[DataType] = {
      new datafixers.Dynamic[DataType](ops, ops.createString(t.lowerName))
    }

    override def deserialize[DataType](d: datafixers.Dynamic[DataType]): Tiers = {
      (d.asString().toScala orElse d.get("string").asString().toScala)
        .flatMap(byName)
        .getOrElse {
          FluidTank.LOGGER.error(s"The tag '${d.getValue}' doesn't have tier data.", new IllegalArgumentException("Invalid tier name."))
          WOOD
        }
    }
  }
}
