package com.kotori316.fluidtank.tank;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.TankConfig;
import com.kotori316.fluidtank.tiles.Tier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TiersTest extends BeforeAllTest {
    @ParameterizedTest
    @EnumSource(Tier.class)
    void hasValidAmount(Tier tier) {
        assertTrue(tier.amount() >= 0);
    }

    @ParameterizedTest
    @EnumSource(value = Tier.class, mode = EnumSource.Mode.EXCLUDE, names = {"CREATIVE", "VOID", "Invalid"})
    void matchAmount(Tier tier) {
        assertEquals(tier.getDefaultAmount(), tier.amount());
    }

    @Test
    void changeConfigWood() {
        var pre = FluidTank.config.capacity.wood;
        try {
            FluidTank.config.capacity.wood = 12000;
            assertEquals(12000L, Tier.WOOD.amount());
        } finally {
            FluidTank.config.capacity.wood = pre;
        }
    }

    @Test
    void doNotAffectOther() {
        var pre = FluidTank.config.capacity.wood;
        try {
            FluidTank.config.capacity.wood = 12000;
            assertEquals(Tier.STONE.getDefaultAmount(), Tier.STONE.amount());
        } finally {
            FluidTank.config.capacity.wood = pre;
        }
    }

    @ParameterizedTest
    @EnumSource(value = Tier.class, names = {"CREATIVE", "VOID", "Invalid"})
    void noConfigEntry(Tier tier) {
        assertTrue(FluidTank.config.capacity.get(tier.name()).isEmpty());
    }

    @ParameterizedTest
    @MethodSource("fieldNames")
    void validFieldTest(String name, Field field) throws ReflectiveOperationException {
        long viaField = field.getLong(FluidTank.config.capacity);
        var viaMethod = FluidTank.config.capacity.get(name);
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
        var lowerCase = FluidTank.config.capacity.get(name.toLowerCase(Locale.ROOT));
        var upperCase = FluidTank.config.capacity.get(name.toUpperCase(Locale.ROOT));
        assertEquals(lowerCase, upperCase, field.getName());
    }
}
