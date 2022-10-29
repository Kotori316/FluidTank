package com.kotori316.fluidtank.integration;

import java.util.Locale;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.GenericAmount;
import com.kotori316.fluidtank.tiles.Tier;

import static com.kotori316.fluidtank.integration.Localize.FLUID_NULL;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionAmount;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionCapacity;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionComparator;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionFluidName;
import static com.kotori316.fluidtank.integration.Localize.NBT_Creative;
import static com.kotori316.fluidtank.integration.Localize.NBT_Tier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizeTest extends BeforeAllTest {
    static CompoundTag createTankData(Tier tier, GenericAmount<Fluid> fluid) {
        long capacity = tier.amount();
        var tag = new CompoundTag();
        tag.putString(NBT_Tier, tier.toString());
        tag.putString(NBT_ConnectionFluidName,
            Optional.of(fluid).filter(GenericAmount::nonEmpty).map(GenericAmount::getLocalizedName).orElse(FLUID_NULL));
        tag.putBoolean(NBT_Creative, tier == Tier.CREATIVE);
        tag.putLong(NBT_ConnectionAmount, fluid.amount());
        tag.putLong(NBT_ConnectionCapacity, capacity);
        var comparatorLevel = fluid.isEmpty() ? 0 : Mth.floor(((double) fluid.amount()) / capacity * 14) + 1;
        tag.putInt(NBT_ConnectionComparator, comparatorLevel);

        return tag;
    }

    static void shouldContain(String c, String expect) {
        assertTrue(c.contains(expect), "Should contain '%s' in '%s'".formatted(expect, c));
    }

    @Nested
    class ShortTest {
        @Test
        void empty() {
            var data = createTankData(Tier.WOOD, FluidAmount.EMPTY());
            var text = Localize.getTooltipText(data, null, true, true, Locale.US);

            assertEquals(1, text.size());
            var c = text.get(0).getString();
            shouldContain(c, "None");
            shouldContain(c, "0");
        }

        @Test
        void oneBucket() {
            var data = createTankData(Tier.WOOD, FluidAmount.BUCKET_WATER());
            var text = Localize.getTooltipText(data, null, true, true, Locale.US);

            assertEquals(1, text.size());
            var c = text.get(0).getString();
            shouldContain(c, "water");
            shouldContain(c, "1.0K");
        }

        @ParameterizedTest
        @ValueSource(ints = {1200, 1299, 1250, 1249, 1251})
        void water12k(int amount) {
            var data = createTankData(Tier.GOLD, FluidAmount.BUCKET_WATER().setAmount(amount));
            var text = Localize.getTooltipText(data, null, true, true, Locale.US);

            assertEquals(1, text.size());
            var c = text.get(0).getString();
            shouldContain(c, "water");
            shouldContain(c, "1.2K");
        }

        @ParameterizedTest
        @ValueSource(ints = {2_000_000, 2_090_000, 2_099_999})
        void lava2M(int amount) {
            var data = createTankData(Tier.EMERALD, FluidAmount.BUCKET_LAVA().setAmount(amount));
            var text = Localize.getTooltipText(data, null, true, true, Locale.US);

            assertEquals(1, text.size());
            var c = text.get(0).getString();
            shouldContain(c, "lava");
            shouldContain(c, "2.0M");
        }
    }

    @Nested
    class NotCompactTest {
        @Test
        void empty() {
            var data = createTankData(Tier.WOOD, FluidAmount.EMPTY());
            var text = Localize.getTooltipText(data, null, true, false, Locale.US);

            assertEquals(1, text.size());
            var c = text.get(0).getString();
            shouldContain(c, "None");
            shouldContain(c, "0");
        }

        @ParameterizedTest
        @ValueSource(ints = {1200, 1299, 1250, 1249, 1251})
        void water12k(int amount) {
            var data = createTankData(Tier.GOLD, FluidAmount.BUCKET_WATER().setAmount(amount));
            var text = Localize.getTooltipText(data, null, true, false, Locale.US);

            assertEquals(1, text.size());
            var c = text.get(0).getString();
            shouldContain(c, "water");
            shouldContain(c, String.valueOf(amount));
            assertFalse(c.contains("1.2K"), "Should not contain compact int.");
        }
    }
}
