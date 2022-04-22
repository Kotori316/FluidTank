package com.kotori316.fluidtank.tank;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.TankConstant;

public enum Tiers {
    Invalid(0, 0, "Invalid", null),
    WOOD(1, 1 << 2, "Wood", null),
    STONE(2, 1 << 4, "Stone", MaterialTags.MATERIAL_STONE.location()),
    IRON(3, 1 << 8, "Iron", ConventionalItemTags.IRON_INGOTS.location()),
    GOLD(4, 1 << 12, "Gold", ConventionalItemTags.GOLD_INGOTS.location()),
    DIAMOND(5, 1 << 14, "Diamond", ConventionalItemTags.DIAMONDS.location()),
    EMERALD(6, 1 << 16, "Emerald", ConventionalItemTags.EMERALDS.location()),
    STAR(7, 1 << 20, "Star", MaterialTags.MATERIAL_STAR.location()),
    CREATIVE(8, 0, "Creative", null) {
        @Override
        public long amount() {
            return Long.MAX_VALUE;
        }
    },
    VOID(0, 0, "Void", null),
    COPPER(2, 1 << 5, "Copper", ConventionalItemTags.COPPER_INGOTS.location()),
    TIN(2, 1 << 6, "Tin", new ResourceLocation("c:tin_ingots")),
    BRONZE(3, 1 << 9, "Bronze", new ResourceLocation("c:bronze_ingots")),
    LEAD(3, 1 << 8, "Lead", new ResourceLocation("c:lead_ingots")),
    SILVER(3, 1 << 10, "Silver", new ResourceLocation("c:silver_ingots")),
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
    @Nullable
    private final ResourceLocation tagName;
    private final String name;

    Tiers(int rank, int buckets, String name, @Nullable ResourceLocation tagName) {
        this.rank = rank;
        this.buckets = buckets;
        this.name = name;
        this.tagName = tagName;
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
        return TankConstant.config.capacity.get(name()).orElse(buckets) * FluidAmount.AMOUNT_BUCKET();
    }

    public TagKey<Item> getTag() {
        if (this.tagName == null)
            throw new IllegalStateException("Can't create tag for tier " + this);
        return TagKey.create(Registry.ITEM_REGISTRY, this.tagName);
    }

    public boolean hasTagRecipe() {
        return this.tagName != null;
    }
}
