package com.kotori316.fluidtank.tiles

import java.util.Collections

import net.minecraft.nbt.NBTTagCompound

import scala.collection.JavaConverters._
import scala.collection.mutable

class Tiers private(val rank: Int,
                    buckets: Int,
                    override val toString: String,
                    val meta: Int,
                    val oreName: String,
                    val hasOreRecipe: Boolean) {
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

  val Invalid = new Tiers(0, 0, "Invalid", 0, "Unknown", hasOreRecipe = false)
  val WOOD = new Tiers(1, 1 << 2, "Wood", 0, "logWood", hasOreRecipe = false)
  val STONE = new Tiers(2, 1 << 4, "Stone", 0, "stone", hasOreRecipe = true)
  val IRON = new Tiers(3, 1 << 8, "Iron", 0, "ingotIron", hasOreRecipe = true)
  val GOLD = new Tiers(4, 1 << 12, "Gold", 0, "ingotGold", hasOreRecipe = true)
  val DIAMOND = new Tiers(5, 1 << 14, "Diamond", 0, "gemDiamond", hasOreRecipe = true)
  val EMERALD = new Tiers(6, 1 << 16, "Emerald", 0, "gemEmerald", hasOreRecipe = true)
  val STAR = new Tiers(7, 1 << 20, "Star", 0, "netherStar", hasOreRecipe = true)
  val CREATIVE = new Tiers(8, 0, "Creative", 0, "Unknown", hasOreRecipe = false) {
    override val amount: Long = Long.MaxValue
  }

  val COPPER = new Tiers(2, 1 << 5, "Copper", 1, "ingotCopper", hasOreRecipe = true)
  val TIN = new Tiers(2, 1 << 6, "Tin", 2, "ingotTin", hasOreRecipe = true)
  val BRONZE = new Tiers(3, 1 << 9, "Bronze", 1, "ingotBronze", hasOreRecipe = true)
  val LEAD = new Tiers(3, 1 << 8, "Lead", 2, "ingotLead", hasOreRecipe = true)
  val SILVER = new Tiers(3, 1 << 10, "Silver", 3, "ingotSilver", hasOreRecipe = true)

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
