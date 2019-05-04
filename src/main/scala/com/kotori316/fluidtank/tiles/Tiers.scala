package com.kotori316.fluidtank.tiles

import java.util.Collections

import net.minecraft.nbt.NBTTagCompound

import scala.collection.JavaConverters._
import scala.collection.mutable

class Tiers private(val rank: Int, buckets: Int, override val toString: String, val oreName: String, val hasOreRecipe: Boolean) {
  val amount: Long = buckets * 1000
  Tiers.list.append(this)

  override def hashCode(): Int = rank.hashCode ^ amount.hashCode ^ toString.hashCode

  def toNBTTag: NBTTagCompound = {
    val nbt = new NBTTagCompound
    nbt.setString("string", toString)
    nbt
  }
}

object Tiers {
  val list = mutable.ArrayBuffer.empty[Tiers]

  val Invalid = new Tiers(0, 0, "Invalid", "Unknown", hasOreRecipe = false)
  val WOOD = new Tiers(1, 1 << 2, "Wood", "minecraft:logs", hasOreRecipe = false)
  val STONE = new Tiers(2, 1 << 4, "Stone", "forge:stone", hasOreRecipe = true)
  val IRON = new Tiers(3, 1 << 8, "Iron", "forge:ingots/iron", hasOreRecipe = true)
  val GOLD = new Tiers(4, 1 << 12, "Gold", "forge:ingots/gold", hasOreRecipe = true)
  val DIAMOND = new Tiers(5, 1 << 14, "Diamond", "forge:gems/diamond", hasOreRecipe = true)
  val EMERALD = new Tiers(6, 1 << 16, "Emerald", "forge:gems/emerald", hasOreRecipe = true)
  val STAR = new Tiers(7, 1 << 20, "Star", "fluidtank:star", hasOreRecipe = true)
  val CREATIVE = new Tiers(8, 0, "Creative", "Unknown", hasOreRecipe = false) {
    override val amount: Long = Long.MaxValue
  }

  val COPPER = new Tiers(2, 1 << 5, "Copper", "forge:ingots/copper", hasOreRecipe = true)
  val TIN = new Tiers(2, 1 << 6, "Tin", "forge:ingots/tin", hasOreRecipe = true)
  val BRONZE = new Tiers(3, 1 << 9, "Bronze", "forge:ingots/bronze", hasOreRecipe = true)
  val LEAD = new Tiers(3, 1 << 8, "Lead", "forge:ingots/lead", hasOreRecipe = true)
  val SILVER = new Tiers(3, 1 << 10, "Silver", "forge:ingots/silver", hasOreRecipe = true)

  val rankList = list.groupBy(_.rank).map { case (r, ts) => (r, ts.size) }
  val nameToTierMap = list.map(t => (t.toString, t)).toMap

  def jList: java.util.List[Tiers] = Collections.unmodifiableList(list.asJava)

  def fromNBT(nbt: NBTTagCompound): Tiers = {
    val key = nbt.getString("string")
    nameToTierMap.getOrElse(key, {
      println("Invalid pattern returned.")
      (new Exception).printStackTrace()
      WOOD
    })
  }
}
