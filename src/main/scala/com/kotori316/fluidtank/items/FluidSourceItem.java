package com.kotori316.fluidtank.items;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ResourceLocation;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.blocks.FluidSourceBlock;

public class FluidSourceItem extends BlockItem {
    public FluidSourceItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
        addPropertyOverride(new ResourceLocation(FluidTank.modID, "source_cheat"), (stack, world, entity) ->
            FluidSourceBlock.isCheatStack(stack) ? 1f : 0f);
    }
}
