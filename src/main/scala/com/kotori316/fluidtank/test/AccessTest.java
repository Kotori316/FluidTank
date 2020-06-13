package com.kotori316.fluidtank.test;

import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessTest {
    @Test
    void fluid() {
        assertNotNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation("water")), "Water");
        assertNotNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation("lava")), "Lava");
    }

    @Test
    void tag() {
        assertTrue(FluidTags.WATER.getAllElements().isEmpty(), "Tag is empty.");
    }
}
