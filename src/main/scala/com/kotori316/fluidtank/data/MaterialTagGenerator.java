package com.kotori316.fluidtank.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.tank.MaterialTags;

final class MaterialTagGenerator extends TagsProvider<Item> {
    MaterialTagGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator, Registry.ITEM);
    }

    @Override
    protected void addTags() {
        tag(MaterialTags.MATERIAL_STONE).add(Items.STONE, Items.ANDESITE, Items.GRANITE, Items.DIORITE, Items.TUFF, Items.DEEPSLATE);
        tag(MaterialTags.MATERIAL_STAR).add(Items.NETHER_STAR);
    }

    @Override
    public String getName() {
        return "Item Tags of " + ModTank.modID;
    }
}
