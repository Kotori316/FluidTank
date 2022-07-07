package com.kotori316.fluidtank.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.kotori316.fluidtank.blocks.FluidSourceBlock;

public class FluidSourceItem extends BlockItem {
    public FluidSourceItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        if (!FluidSourceBlock.isCheatStack(stack)) {
            return "block.fluidtank.water_source";
        }
        return super.getDescriptionId(stack);
    }
}
