package com.kotori316.fluidtank.tank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.TankConstant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TiersTest extends BeforeAllTest {
    @ParameterizedTest
    @EnumSource(Tiers.class)
    void hasValidAmount(Tiers tier) {
        assertTrue(tier.amount() >= 0);
    }

    @ParameterizedTest
    @EnumSource(value = Tiers.class, mode = EnumSource.Mode.EXCLUDE, names = {"CREATIVE", "VOID", "Invalid"})
    void matchAmount(Tiers tier) {
        assertEquals(tier.buckets * 1000L, tier.amount());
    }

    @Test
    void changeConfigWood() {
        var pre = TankConstant.config.capacity.wood;
        try {
            TankConstant.config.capacity.wood = 12;
            assertEquals(12000L, Tiers.WOOD.amount());
        } finally {
            TankConstant.config.capacity.wood = pre;
        }
    }

    @Test
    void doNotAffectOther() {
        var pre = TankConstant.config.capacity.wood;
        try {
            TankConstant.config.capacity.wood = 12;
            assertEquals(Tiers.STONE.buckets * 1000L, Tiers.STONE.amount());
        } finally {
            TankConstant.config.capacity.wood = pre;
        }
    }

    @ParameterizedTest
    @EnumSource(value = Tiers.class, names = {"CREATIVE", "VOID", "Invalid"})
    void noConfigEntry(Tiers tier) {
        assertTrue(TankConstant.config.capacity.get(tier.name()).isEmpty());
    }
}
