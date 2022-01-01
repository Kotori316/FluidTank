package com.kotori316.fluidtank;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsTest extends BeforeAllTest {

    @Test
    void setTileTag() {
        var tileTag = createTag();
        var stack = new ItemStack(Blocks.DISPENSER);
        assertFalse(stack.hasTag());
        Utils.setTileTag(stack, tileTag);
        assertTrue(stack.hasTag());
        assertEquals(tileTag, BlockItem.getBlockEntityData(stack));
    }

    static CompoundTag createTag() {
        var tag = new CompoundTag();
        tag.putString("key1", "value1");
        tag.putInt("key2", 2);
        return tag;
    }
}
