package com.kotori316.fluidtank.tank;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import org.apache.commons.lang3.tuple.Pair;

public enum Tiers {
    Invalid(0, 0, "Invalid", "Unknown", false),
    WOOD(1, 1 << 2, "Wood", "minecraft:logs", false),
    STONE(2, 1 << 4, "Stone", "forge:stone", true, () ->
        Ingredient.ofItems(Blocks.STONE, Blocks.ANDESITE, Blocks.GRANITE, Blocks.DIORITE, Blocks.TUFF, Blocks.DEEPSLATE)),
    IRON(3, 1 << 8, "Iron", "forge:ingots/iron", true, () -> Ingredient.ofItems(Items.IRON_INGOT)),
    GOLD(4, 1 << 12, "Gold", "forge:ingots/gold", true, () -> Ingredient.ofItems(Items.GOLD_INGOT)),
    DIAMOND(5, 1 << 14, "Diamond", "forge:gems/diamond", true, () -> Ingredient.ofItems(Items.DIAMOND)),
    EMERALD(6, 1 << 16, "Emerald", "forge:gems/emerald", true, () -> Ingredient.ofItems(Items.EMERALD)),
    STAR(7, 1 << 20, "Star", "forge:nether_stars", true, () -> Ingredient.ofItems(Items.NETHER_STAR)),
    CREATIVE(8, 0, "Creative", "Unknown", false) {
        @Override
        public long amount() {
            return Long.MAX_VALUE;
        }
    },
    COPPER(2, 1 << 5, "Copper", "c:copper_ingots", true, () -> Ingredient.ofItems(Items.COPPER_INGOT)),
    TIN(2, 1 << 6, "Tin", "c:tin_ingots", true),
    BRONZE(3, 1 << 9, "Bronze", "c:bronze_ingots", true),
    LEAD(3, 1 << 8, "Lead", "c:lead_ingots", true),
    SILVER(3, 1 << 10, "Silver", "c:silver_ingots", true),
    ;
    public static final Map<String, Tiers> TIERS_MAP;

    static {
        TIERS_MAP = Collections.unmodifiableMap(
            Stream.of(Tiers.values()).map(tiers -> Pair.of(tiers.toString(), tiers))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue))
        );
    }

    public final int rank;
    public final int buckets;
    public final String tagName;
    private final String name;
    private final boolean hasTagRecipe;
    private final long amount;
    private final Supplier<Ingredient> alternative;

    Tiers(int rank, int buckets, String name, String tagName, boolean hasTagRecipe) {
        this(rank, buckets, name, tagName, hasTagRecipe, () -> Ingredient.EMPTY);
    }

    Tiers(int rank, int buckets, String name, String tagName, boolean hasTagRecipe, Supplier<Ingredient> alternative) {
        this.rank = rank;
        this.buckets = buckets;
        this.name = name;
        this.tagName = tagName;
        this.hasTagRecipe = hasTagRecipe;
        this.amount = buckets * 1000L;
        this.alternative = alternative;
    }

    @Override
    public String toString() {
        return name;
    }

    public NbtCompound toNBTTag() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("string", toString());
        return nbt;
    }

    public static Tiers fromNBT(NbtCompound tag) {
        if (tag == null) {
            return Invalid;
        }
        String name = tag.getString("string");
        return TIERS_MAP.getOrDefault(name, WOOD);
    }

    public static Stream<Tiers> stream() {
        return Stream.of(Tiers.values());
    }

    public long amount() {
        return amount;
    }

    public boolean hasTagRecipe() {
        return hasTagRecipe;
    }

    public Ingredient getAlternative() {
        return alternative.get();
    }
}
