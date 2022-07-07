package com.kotori316.fluidtank.tiles;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import com.kotori316.fluidtank.FluidTank;

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
    TIN(2, 48, "Tin", "forge:ingots/tin", false),
    BRONZE(3, 384, "Bronze", "forge:ingots/bronze", false),
    LEAD(3, 192, "Lead", "forge:ingots/lead", false),
    SILVER(3, 1024, "Silver", "forge:ingots/silver", false),
    ;
    private static final String UNKNOWN_TAG = "Unknown";
    private final int rank;
    private final long defaultAmount;
    private final String string;
    private final String lowerName;
    private final String tagName;
    private final boolean availableInVanilla;

    Tier(int rank, int buckets, String name) {
        this(rank, buckets, name, UNKNOWN_TAG, true);
    }

    Tier(int rank, int buckets, String name, String tagName, boolean availableInVanilla) {
        this.rank = rank;
        this.defaultAmount = buckets * 1000L;
        this.string = name;
        this.lowerName = name.toLowerCase(Locale.ROOT);
        this.tagName = tagName;
        this.availableInVanilla = availableInVanilla;
    }

    Tier(int rank, long amount, String name) {
        this.rank = rank;
        this.defaultAmount = amount;
        this.string = name;
        this.lowerName = name.toLowerCase(Locale.ROOT);
        this.tagName = UNKNOWN_TAG;
        this.availableInVanilla = true;
    }

    public int rank() {
        return rank;
    }

    public long amount() {
        return FluidTank.config.capacity.get(name()).orElse(defaultAmount);
    }

    public long getDefaultAmount() {
        return defaultAmount;
    }

    public boolean hasTagRecipe() {
        return !UNKNOWN_TAG.equals(this.tagName);
    }

    public String tagName() {
        return tagName;
    }

    public String lowerName() {
        return lowerName;
    }

    public StringTag toNBTTag() {
        return StringTag.valueOf(lowerName);
    }

    public boolean hasWayToCreate() {
        return isAvailableInVanilla();
    }

    public boolean isNormalTier() {
        return 0 < this.rank && this.rank < CREATIVE.rank;
    }

    public boolean isAvailableInVanilla() {
        return availableInVanilla;
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

    public static Tier fromNBT(Tag nbt) {
        if (nbt instanceof StringTag stringNBT) {
            return byName(stringNBT.getAsString()).orElse(Invalid);
        } else {
            return Invalid;
        }
    }
}
