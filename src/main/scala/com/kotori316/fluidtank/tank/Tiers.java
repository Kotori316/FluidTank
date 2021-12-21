package com.kotori316.fluidtank.tank;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.fluidtank.TankConstant;

public enum Tiers {
    Invalid(0, 0, "Invalid", "Unknown", false),
    WOOD(1, 1 << 2, "Wood", "minecraft:logs", false),
    STONE(2, 1 << 4, "Stone", "forge:stone", true, () ->
        Ingredient.of(Blocks.STONE, Blocks.ANDESITE, Blocks.GRANITE, Blocks.DIORITE, Blocks.TUFF, Blocks.DEEPSLATE)),
    IRON(3, 1 << 8, "Iron", "forge:ingots/iron", true, () -> Ingredient.of(Items.IRON_INGOT)),
    GOLD(4, 1 << 12, "Gold", "forge:ingots/gold", true, () -> Ingredient.of(Items.GOLD_INGOT)),
    DIAMOND(5, 1 << 14, "Diamond", "forge:gems/diamond", true, () -> Ingredient.of(Items.DIAMOND)),
    EMERALD(6, 1 << 16, "Emerald", "forge:gems/emerald", true, () -> Ingredient.of(Items.EMERALD)),
    STAR(7, 1 << 20, "Star", "forge:nether_stars", true, () -> Ingredient.of(Items.NETHER_STAR)),
    CREATIVE(8, 0, "Creative", "Unknown", false) {
        @Override
        public long amount() {
            return Long.MAX_VALUE;
        }
    },
    VOID(0, 0, "Void", "Unknown", false),
    COPPER(2, 1 << 5, "Copper", "c:copper_ingots", true, () -> Ingredient.of(Items.COPPER_INGOT)),
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
        this.alternative = alternative;
    }

    @Override
    public String toString() {
        return name;
    }

    public CompoundTag toNBTTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("string", toString());
        return nbt;
    }

    public static Tiers fromNBT(CompoundTag tag) {
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
        return TankConstant.config.capacity.get(name()).orElse(buckets) * 1000L;
    }

    public boolean hasTagRecipe() {
        return hasTagRecipe;
    }

    public Ingredient getAlternative() {
        return alternative.get();
    }
}
