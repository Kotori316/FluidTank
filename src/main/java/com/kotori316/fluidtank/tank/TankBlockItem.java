package com.kotori316.fluidtank.tank;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

import com.kotori316.fluidtank.ModTank;

public class TankBlockItem extends BlockItem {
    public TankBlockItem(TankBlock block) {
        super(block, new Item.Settings().group(ModTank.CREATIVE_TAB));
    }

    public boolean hasRecipe() {
        return true;
    }
}
