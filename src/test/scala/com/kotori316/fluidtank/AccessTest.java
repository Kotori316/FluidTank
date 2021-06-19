package com.kotori316.fluidtank;

import java.util.stream.Stream;

import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class AccessTest extends BeforeAllTest {
    @Test
    void fluid() {
        assertNotNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation("water")), "Water");
        assertNotNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation("lava")), "Lava");
    }

    @ParameterizedTest
    @MethodSource("getCapabilities")
    void capability(Capability<?> cap, String name) {
        assertNull(cap, String.format("Accessing capability %s is unavailable", name));
    }

    static Object[] getCapabilities() {
        return Stream.of(
            Pair.of(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, "FLUID_HANDLER_CAPABILITY"),
            Pair.of(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, "FLUID_HANDLER_ITEM_CAPABILITY"),
            Pair.of(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, "ITEM_HANDLER_CAPABILITY"),
            Pair.of(CapabilityEnergy.ENERGY, "ENERGY"),
            Pair.of(CapabilityAnimation.ANIMATION_CAPABILITY, "ANIMATION_CAPABILITY")
        ).map(p -> new Object[]{p.getLeft(), p.getRight()}).toArray();
    }

    @Test
    void dummy() {
        assertTrue(getCapabilities().length > 0);
    }

    @Test
    @Disabled("Test Disabled: Accessing tag before bounding will cause crash.")
    void tag() {
        assertTrue(FluidTags.WATER.getAllElements().isEmpty(), "Tag is empty.");
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
    /*void notCompile() {
        1 + 1;
    }*/
}
