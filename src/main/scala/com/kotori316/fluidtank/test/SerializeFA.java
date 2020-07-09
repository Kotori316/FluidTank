package com.kotori316.fluidtank.test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import com.kotori316.fluidtank.FluidAmount;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerializeFA {
    @Test
    void withCodec() {
        Codec<FluidAmount> codec = FluidAmount.codecFA();
        assertAll(
            Stream.of(FluidAmount.BUCKET_LAVA(), FluidAmount.BUCKET_MILK(), FluidAmount.BUCKET_WATER())
                .map(f -> () -> assertTrue(codec.encodeStart(JsonOps.INSTANCE, f).result().isPresent()))
        );
    }

    @Test
    void withCodec2() {
        Codec<FluidAmount> codec = FluidAmount.codecFA();
        List<FluidAmount> list = Stream.of(FluidAmount.BUCKET_LAVA(), FluidAmount.BUCKET_MILK(), FluidAmount.BUCKET_WATER()).collect(Collectors.toList());
        assertIterableEquals(list, list.stream()
            .map(f -> codec.encodeStart(JsonOps.INSTANCE, f).flatMap(j -> codec.parse(JsonOps.INSTANCE, j)))
            .map(DataResult::result)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList()));
    }
}
