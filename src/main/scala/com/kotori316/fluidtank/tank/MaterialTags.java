package com.kotori316.fluidtank.tank;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import com.kotori316.fluidtank.ModTank;

public final class MaterialTags {
    public static final TagKey<Item> MATERIAL_STONE = create("material_stone");
    public static final TagKey<Item> MATERIAL_STAR = create("material_star");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(ModTank.modID, name));
    }
}
