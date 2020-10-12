package com.kotori316.fluidtank;

import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class AccessTest extends BeforeAllTest {
    @Test
    void fluid() {
        assertNotNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation("water")), "Water");
        assertNotNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation("lava")), "Lava");
    }

    @Test
    @Disabled("Access tag before bounding will cause crash.")
    void tag() {
        assertTrue(FluidTags.WATER.getAllElements().isEmpty(), "Tag is empty.");
    }

    @Test
    @Disabled
    void failTest() {
        fail("Fail Test");
    }

    /*void notCompile() {
        1 + 1;
    }*/
}
