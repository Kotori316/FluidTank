package com.kotori316.fluidtank.tiles

import net.minecraft.nbt.NBTTagCompound

import scala.collection.JavaConverters._
import scala.collection.mutable

class Tiers private(val rank: Int, buckets: Int, override val toString: String, val meta: Int, val oreName: String) {
    val amount = buckets * 1000
    Tiers.map.put(toString, this)
    Tiers.list.append(this)
    Tiers.rankList(rank) = Tiers.rankList(rank) + 1

    override def hashCode(): Int = (rank << 20 + amount) ^ toString.hashCode

    def toNBTTag: NBTTagCompound = {
        val nbt = new NBTTagCompound
        nbt.setString("string", toString)
        nbt
    }
}

object Tiers {
    val rankList = Array.fill(9)(0)

    val map = mutable.Map.empty[String, Tiers]
    val list = mutable.ArrayBuffer.empty[Tiers]

    val Invalid = new Tiers(0, 0, "Invalid", 0, "Unknown")
    val WOOD = new Tiers(1, 1 << 2, "Wood", 0, "logWood")
    val STONE = new Tiers(2, 1 << 4, "Stone", 0, "stone")
    val IRON = new Tiers(3, 1 << 8, "Iron", 0, "ingotIron")
    val GOLD = new Tiers(4, 1 << 12, "Gold", 0, "ingotGold")
    val DIAMOND = new Tiers(5, 1 << 14, "Diamond", 0, "gemDiamond")
    val EMERALD = new Tiers(6, 1 << 16, "Emerald", 0, "gemEmerald")
    val STAR = new Tiers(7, 1 << 20, "Star", 0, "netherStar")
    val CREATIVE = new Tiers(8, 0, "Creative", 0, "Unknown") {
        override val amount: Int = Int.MaxValue
    }

    val COPPER = new Tiers(2, 1 << 5, "Copper", 1, "ingotCopper")
    val TIN = new Tiers(2, 1 << 6, "Tin", 2, "ingotTin")
    val BRONZE = new Tiers(3, 1 << 9, "Bronze", 1, "ingotBronze")
    val LEAD = new Tiers(3, 1 << 8, "Lead", 2, "ingotLead")
    val SILVER = new Tiers(3, 1 << 10, "Silver", 3, "ingotSilver")

    def jList: java.util.List[Tiers] = list.asJava

    def fromNBT(nbt: NBTTagCompound): Tiers = {
        val key = nbt.getString("string")
        map.getOrElse(key, {
            println("Invaluid pattern returned.")
            (new Exception).printStackTrace()
            WOOD
        })
    }
}
