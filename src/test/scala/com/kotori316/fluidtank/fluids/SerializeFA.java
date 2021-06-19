package com.kotori316.fluidtank.fluids;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import scala.Option;

import com.kotori316.fluidtank.BeforeAllTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class SerializeFA extends BeforeAllTest {
    static List<FluidAmount> examples() {
        return Arrays.asList(
            // Milk is not vanilla fluid, so deserialization fails.
            FluidAmount.EMPTY(),
            FluidAmount.BUCKET_LAVA(), /*FluidAmount.BUCKET_MILK()*/ FluidAmount.BUCKET_WATER(),
            FluidAmount.apply(Fluids.LAVA, 2000L, Option.apply(new CompoundNBT())),
            FluidAmount.apply(Fluids.WATER, 4000L, Option.apply(new CompoundNBT())),
            FluidAmount.apply(Fluids.LAVA, 6000L, Option.apply(new CompoundNBT()))
        );
    }

    @Test
    void dummy() {
        assertNotEquals(0, examples().size());
    }

    @ParameterizedTest
    @MethodSource("com.kotori316.fluidtank.fluids.SerializeFA#examples")
    void serialize(FluidAmount amount) {
        assertNotNull(amount.write(new CompoundNBT()));
    }

    @ParameterizedTest
    @MethodSource("com.kotori316.fluidtank.fluids.SerializeFA#examples")
    void deserialize(FluidAmount amount) {
        FluidAmount deserialized = FluidAmount.fromNBT(amount.write(new CompoundNBT()));
        assertEquals(amount, deserialized);
    }

    @Test
    void nonNullCheck() {
        Stream<ResourceLocation> locationStream = Stream.of(
            "minecraft:empty",
            "what_am_i"
        ).map(ResourceLocation::tryCreate).filter(Objects::nonNull);
        assertAll(locationStream
            .map(ForgeRegistries.FLUIDS::getValue)
            .map(f -> () -> assertEquals(Fluids.EMPTY, f))
        );
    }
}