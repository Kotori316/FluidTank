package com.kotori316.fluidtank.tiles;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;

import com.kotori316.fluidtank.recipes.TagCondition;

public enum Tier {
    Invalid(0, 0, "Invalid"),
    WOOD(1, 1 << 2, "Wood"),
    STONE(2, 1 << 4, "Stone", "forge:stone", true),
    IRON(3, 1 << 8, "Iron", "forge:ingots/iron", true),
    GOLD(4, 1 << 12, "Gold", "forge:ingots/gold", true),
    DIAMOND(5, 1 << 14, "Diamond", "forge:gems/diamond", true),
    EMERALD(6, 1 << 16, "Emerald", "forge:gems/emerald", true),
    STAR(7, 1 << 20, "Star", "forge:nether_stars", true),
    CREATIVE(8, Long.MAX_VALUE, "Creative"),
    VOID(0, 0, "Void"),
    COPPER(2, 40, "Copper", "forge:ingots/copper", true),
    TIN(2, 48, "Tin", "forge:ingots/tin", true),
    BRONZE(3, 384, "Bronze", "forge:ingots/bronze", true),
    LEAD(3, 192, "Lead", "forge:ingots/lead", true),
    SILVER(3, 1024, "Silver", "forge:ingots/silver", true),
    ;
    private final int rank;
    private final long amount;
    private final String string;
    private final String lowerName;
    private final String tagName;
    private final boolean hasTagRecipe;

    Tier(int rank, int buckets, String name) {
        this(rank, buckets, name, "Unknown", false);
    }

    Tier(int rank, int buckets, String name, String tagName, boolean hasTagRecipe) {
        this.rank = rank;
        this.amount = buckets * 1000L;
        this.string = name;
        this.lowerName = name.toLowerCase(Locale.ROOT);
        this.tagName = tagName;
        this.hasTagRecipe = hasTagRecipe;
    }

    Tier(int rank, long amount, String name) {
        this.rank = rank;
        this.amount = amount;
        this.string = name;
        this.lowerName = name.toLowerCase(Locale.ROOT);
        this.tagName = "Unknown";
        this.hasTagRecipe = false;
    }

    public int rank() {
        return rank;
    }

    public long amount() {
        return amount;
    }

    public boolean hasTagRecipe() {
        return hasTagRecipe;
    }

    public String tagName() {
        return tagName;
    }

    public String lowerName() {
        return lowerName;
    }

    public StringNBT toNBTTag() {
        return StringNBT.valueOf(lowerName);
    }

    public boolean hasWayToCreate() {
        return !hasTagRecipe || new TagCondition(tagName).test();
    }

    public boolean isNormalTier() {
        return 0 < this.rank && this.rank < CREATIVE.rank;
    }

    @Override
    public String toString() {
        return this.string;
    }

    public static scala.collection.immutable.Seq<Tier> list() {
        return scala.collection.immutable.ArraySeq.unsafeWrapArray(values());
    }

    public static Optional<Tier> byName(String s) {
        return Arrays.stream(values())
            .filter(t -> t.toString().equalsIgnoreCase(s))
            .findFirst();
    }

    public static Tier fromNBT(INBT nbt) {
        if (nbt instanceof StringNBT) {
            StringNBT stringNBT = (StringNBT) nbt;
            return byName(stringNBT.getString()).orElse(Invalid);
        } else {
            return Invalid;
        }
    }
}
