package com.kotori316.fluidtank.tank;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.TankConfig;
import com.kotori316.fluidtank.TankConstant;

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
            assertEquals(12000L, Tier.WOOD.amount());
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

    @ParameterizedTest
    @MethodSource("fieldNames")
    void validFieldTest(String name, Field field) throws ReflectiveOperationException {
        long viaField = field.getLong(TankConstant.config.capacity);
        var viaMethod = TankConstant.config.capacity.get(name);
        assertTrue(viaMethod.isPresent(), "Field(%s) has value but not declared in field.".formatted(name));
        assertEquals(viaField, viaMethod.getAsLong());
    }

    static Stream<Object[]> fieldNames() {
        var clazz = TankConfig.Capacity.class;
        var fields = clazz.getFields();
        return Arrays.stream(fields).map(f -> new Object[]{f.getName(), f});
    }

    @ParameterizedTest
    @MethodSource("fieldNames")
    void ignoreCase(String name, Field field) {
        var lowerCase = TankConstant.config.capacity.get(name.toLowerCase());
        var upperCase = TankConstant.config.capacity.get(name.toUpperCase());
        assertEquals(lowerCase, upperCase, field.getName());
    }
}
