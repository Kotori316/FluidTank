package com.kotori316.fluidtank.tiles

import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable

sealed class Tiers private(val rank: Int, a: Int, override val toString: String, val meta: Int) {
    val amount = a * 1000
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
    val rankList = Array(0, 0, 0, 0, 0, 0, 0)

    val map = mutable.Map.empty[String, Tiers]
    val list = mutable.ArrayBuffer.empty[Tiers]

    val Invalid = new Tiers(0, 0, "Invalid", 0)
    val WOOD = new Tiers(1, 1 << 2, "Wood", 0)
    val STONE = new Tiers(2, 1 << 4, "Stone", 0)
    val IRON = new Tiers(3, 1 << 8, "Iron", 0)
    val GOLD = new Tiers(4, 1 << 12, "Gold", 0)
    val DIAMOND = new Tiers(5, 1 << 14, "Diamond", 0)
    val EMERALD = new Tiers(6, 1 << 16, "Emerald", 0)

    val COPPER = new Tiers(2, 1 << 6, "Copper", 1)
    val TIN = new Tiers(2, 1 << 7, "Tin", 2)
    val BRONZE = new Tiers(3, 1 << 9, "Bronze", 1)
    val LEAD = new Tiers(3, 1 << 8, "Lead", 2)
    val SILVER = new Tiers(3, 1 << 10, "Silver", 3)

    def fromNBT(nbt: NBTTagCompound): Tiers = {
        val key = nbt.getString("string")
        map.getOrElse(key, Invalid)
    }
}
