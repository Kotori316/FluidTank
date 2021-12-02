package com.kotori316.fluidtank;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class AccessTest extends BeforeAllTest {
    @Test
    void fluid() {
        assertNotNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation("water")), "Water");
        assertNotNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation("lava")), "Lava");
    }

    @Test
    @Disabled("Test Disabled: Accessing tag before bounding will cause crash.")
    void tag() {
        assertTrue(FluidTags.WATER.getValues().isEmpty(), "Tag is empty.");
    }

    @Test
    @Disabled("Test Disabled: Dummy fail check test.")
    void failTest() {
        fail("Fail Test");
    }

    @Test
    void configAccess() {
        assertTrue(Config.content().debug());
        assertFalse(Config.content().removeRecipe());
    }

    @Nested
    class ShowFluidStack {
        @Test
        void water500() {
            var stack = new FluidStack(Fluids.WATER, 500);
            var str = com.kotori316.fluidtank.package$.MODULE$.showFluidStack().show(stack);
            assertTrue(str.contains("500"), str);
            assertTrue(str.contains("water"), str);
        }

        @Test
        void lava1600() {
            var stack = new FluidStack(Fluids.LAVA, 1600);
            var str = com.kotori316.fluidtank.package$.MODULE$.showFluidStack().show(stack);
            assertTrue(str.contains("1600"), str);
            assertTrue(str.contains("lava"), str);
        }

        @Test
        void empty300() {
            var stack = new FluidStack(Fluids.EMPTY, 300);
            var str = com.kotori316.fluidtank.package$.MODULE$.showFluidStack().show(stack);
            assertTrue(str.contains("300"), str);
            assertTrue(str.contains("air"), str);
        }
    }
}
