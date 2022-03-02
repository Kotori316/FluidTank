package com.kotori316.fluidtank;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.Tank;
import com.kotori316.fluidtank.fluids.TankHandler;
import com.kotori316.fluidtank.recipes.RecipeInventoryUtil;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

final class AccessTest extends BeforeAllTest {
    @Test
    void fluid() {
        assertNotNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation("water")), "Water");
        assertNotNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation("lava")), "Lava");
    }

    @SuppressWarnings("deprecation")
    @Test
    @Disabled("Test Disabled: Accessing tag before bounding will cause crash.")
    void tag() {
        var a = Registry.FLUID.getTag(FluidTags.WATER)
            .stream()
            .flatMap(HolderSet.Named::stream)
            .map(Holder::value)
            .toList();
        assertTrue(a.isEmpty(), "Tag is empty.");
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
    class ShowFluidStackTest {
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

    @Nested
    class AccessFluidUtilTest {
        @Test
        void mock1() {
            var stack = new ItemStack(ModObjects.blockTanks().head());
            var handler = RecipeInventoryUtil.getFluidHandler(stack);
            try (var mocked = mockStatic(FluidUtil.class)) {
                mocked.when(() -> FluidUtil.getFluidHandler(stack)).thenReturn(LazyOptional.of(() -> handler));

                var h2 = assertDoesNotThrow(() -> FluidUtil.getFluidHandler(stack));
                assertTrue(h2.isPresent());
                assertEquals(handler, h2.orElseThrow(AssertionError::new));
            }
        }

        @Test
        void mock2() {
            var stack = new ItemStack(ModObjects.blockTanks().head(), 5);
            var handler = RecipeInventoryUtil.getFluidHandler(stack);
            try (var mocked = mockStatic(FluidUtil.class)) {
                mocked.when(() -> FluidUtil.getFluidHandler(any(ItemStack.class))).thenReturn(LazyOptional.of(() -> handler));

                var h2 = assertDoesNotThrow(() -> FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(stack, 1)));
                assertTrue(h2.isPresent());
                assertEquals(handler, h2.orElseThrow(AssertionError::new));
            }
        }

        @Test
        @Disabled("Test Disabled: This doesn't work because FluidUtil.tryFillContainer returns null as the class is mocked.")
        void fillMock() {
            var stack = new ItemStack(ModObjects.blockTanks().head());
            var handler = RecipeInventoryUtil.getFluidHandler(stack);
            var tank = TankHandler.apply(Tank.apply(FluidAmount.BUCKET_WATER().setAmount(3000L), 8000L));
            try (var mocked = mockStatic(FluidUtil.class)) {
                mocked.when(() -> FluidUtil.getFluidHandler(any(ItemStack.class))).thenReturn(LazyOptional.of(() -> handler));

                var result = FluidUtil.tryFillContainer(stack, tank, 2000, null, false);

                assertEquals(Tank.apply(FluidAmount.BUCKET_WATER().setAmount(3000L), 8000L), tank.getTank());
                assertTrue(handler.getFluid().isEmpty());
                assertTrue(result.isSuccess());
                var after = RecipeInventoryUtil.getFluidHandler(result.getResult());
                assertEquals(FluidAmount.BUCKET_WATER().setAmount(2000), after.getFluid());
            }
        }

        @Test
        void mockCapability() {
            var stack = new ItemStack(ModObjects.blockTanks().head());
            assertDoesNotThrow(() -> stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
        }

        @Test
        void mockCapability2() {
            var stack = new ItemStack(ModObjects.blockTanks().head());
            var tank = TankHandler.apply(Tank.apply(FluidAmount.BUCKET_WATER().setAmount(3000L), 8000L));
            var result = FluidUtil.tryFillContainer(stack, tank, 2000, null, false);

            assertEquals(Tank.apply(FluidAmount.BUCKET_WATER().setAmount(3000L), 8000L), tank.getTank());
            assertTrue(RecipeInventoryUtil.getFluidHandler(stack).getFluid().isEmpty());
            assertTrue(result.isSuccess());
        }

        @Test
        void mockCapability3() {
            var stack = new ItemStack(ModObjects.blockTanks().head());
            var tank = TankHandler.apply(Tank.apply(FluidAmount.BUCKET_WATER().setAmount(3000L), 8000L));
            var result = FluidUtil.tryFillContainer(stack, tank, 2000, null, true);

            assertEquals(Tank.apply(FluidAmount.BUCKET_WATER().setAmount(1000L), 8000L), tank.getTank());
            assertTrue(RecipeInventoryUtil.getFluidHandler(stack).getFluid().isEmpty()); // Input item is not modified.
            assertTrue(result.isSuccess());
            var after = RecipeInventoryUtil.getFluidHandler(result.getResult());
            assertEquals(FluidAmount.BUCKET_WATER().setAmount(2000L), after.getFluid());
        }
    }
}
