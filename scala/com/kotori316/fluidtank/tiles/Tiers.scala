package com.kotori316.fluidtank.tiles

import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable

sealed class Tiers private(val rank: Int, a: Int, override val toString: String) {
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

    val Invalid = new Tiers(0, 0, "Invalid")
    val WOOD = new Tiers(1, 1 << 2, "Wood")
    val STONE = new Tiers(2, 1 << 4, "Stone")
    val IRON = new Tiers(3, 1 << 8, "Iron")
    val GOLD = new Tiers(4, 1 << 12, "Gold")
    val DIAMOND = new Tiers(5, 1 << 14, "Diamond")
    val EMERALD = new Tiers(6, 1 << 16, "Emerald")

    val COPPER = new Tiers(2, 1 << 6, "Copper")
    val TIN = new Tiers(2, 1 << 7, "Tin")
    val BRONZE = new Tiers(3, 1 << 9, "Bronze")
    val LEAD = new Tiers(3, 1 << 8, "Lead")
    val SILVER = new Tiers(3, 1 << 10, "Silver")

    def fromNBT(nbt: NBTTagCompound): Tiers = {
        val key = nbt.getString("string")
        map.getOrElse(key, Invalid)
    }
}
