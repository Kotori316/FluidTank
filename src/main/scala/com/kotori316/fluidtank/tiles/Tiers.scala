package com.kotori316.fluidtank.tiles

import java.util.Collections

import cats._
import cats.data._
import com.kotori316.fluidtank.DynamicSerializable._
import com.kotori316.fluidtank._
import com.mojang.serialization.Codec
import net.minecraft.nbt.INBT

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

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
  val WOOD = new Tiers(1, 4, "Wood", "minecraft:logs", hasTagRecipe = false)
  val STONE = new Tiers(2, 16, "Stone", "forge:stone", hasTagRecipe = true)
  val IRON = new Tiers(3, 256, "Iron", "forge:ingots/iron", hasTagRecipe = true)
  val GOLD = new Tiers(4, 4096, "Gold", "forge:ingots/gold", hasTagRecipe = true)
  val DIAMOND = new Tiers(5, 1 << 14, "Diamond", "forge:gems/diamond", hasTagRecipe = true)
  val EMERALD = new Tiers(6, 1 << 16, "Emerald", "forge:gems/emerald", hasTagRecipe = true)
  val STAR = new Tiers(7, 1 << 20, "Star", "forge:nether_stars", hasTagRecipe = true)
  val CREATIVE: Tiers = new Tiers(8, 0, "Creative", "Unknown", hasTagRecipe = false) {
    override val amount: Long = Long.MaxValue
  }
  val VOID = new Tiers(0, 0, "Void", "Unknown", hasTagRecipe = false)

  val COPPER = new Tiers(2, 40, "Copper", "forge:ingots/copper", hasTagRecipe = true)
  val TIN = new Tiers(2, 48, "Tin", "forge:ingots/tin", hasTagRecipe = true)
  val BRONZE = new Tiers(3, 384, "Bronze", "forge:ingots/bronze", hasTagRecipe = true)
  val LEAD = new Tiers(3, 192, "Lead", "forge:ingots/lead", hasTagRecipe = true)
  val SILVER = new Tiers(3, 1024, "Silver", "forge:ingots/silver", hasTagRecipe = true)

  def jList: java.util.List[Tiers] = Collections.unmodifiableList(list.asJava)

  def fromNBT(nbt: INBT): Tiers = TierDynamicSerialize.deserializeFromNBT(nbt)

  def byName(s: String): Option[Tiers] = list.find(_.toString.equalsIgnoreCase(s))

  implicit val EqTiers: Hash[Tiers] = Hash.fromUniversalHashCode

  implicit val TierCodec: Codec[Tiers] = Codec.STRING.comapFlatMap[Tiers](
    s => (byName(s) match {
      case Some(value) => Ior.right(value)
      case None => Ior.both(s"Invalid tier name, $s.", WOOD)
    }).toResult,
    tier => tier.lowerName
  )

  implicit val TierDynamicSerialize: DynamicSerializable[Tiers] = new DynamicSerializable.DynamicSerializableFromCodec[Tiers](TierCodec, WOOD)
}
