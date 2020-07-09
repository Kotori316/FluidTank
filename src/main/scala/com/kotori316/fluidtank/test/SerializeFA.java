package com.kotori316.fluidtank.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import org.junit.jupiter.api.Test;
import scala.Option;

import com.kotori316.fluidtank.FluidAmount;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerializeFA {
    private static final List<FluidAmount> examples = Arrays.asList(
        FluidAmount.BUCKET_LAVA(), FluidAmount.BUCKET_MILK(), FluidAmount.BUCKET_WATER(),
        FluidAmount.apply(Fluids.LAVA, 2000L, Option.apply(new CompoundNBT())),
        FluidAmount.apply(Fluids.WATER, 4000L, Option.apply(new CompoundNBT())),
        FluidAmount.apply(Fluids.LAVA, 6000L, Option.apply(new CompoundNBT()))
    );

    @Test
    void withCodec() {
        Codec<FluidAmount> codec = FluidAmount.codecFA();
        assertAll(
            examples.stream()
                .map(f -> () -> assertTrue(codec.encodeStart(JsonOps.INSTANCE, f).result().isPresent()))
        );
    }

    @Test
    void withCodec2() {
        Codec<FluidAmount> codec = FluidAmount.codecFA();
        List<FluidAmount> list = examples;
        assertIterableEquals(list, list.stream()
            .map(f -> codec.encodeStart(JsonOps.INSTANCE, f).flatMap(j -> codec.parse(JsonOps.INSTANCE, j)))
            .map(DataResult::result)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList()));
    }

    private static <T> Optional<T> encode(Codec<FluidAmount> c, DynamicOps<T> ops, FluidAmount a) {
        return c.encodeStart(ops, a).result();
    }

    @Test
    void compatibleCheckOfJson() {
        List<JsonElement> fromCodec = examples.stream()
            .map(f -> encode(FluidAmount.codecFA(), JsonOps.INSTANCE, f))
            .map(Optional::get)
            .collect(Collectors.toList());
        List<JsonElement> fromDynamic = examples.stream()
            .map(f -> FluidAmount.dynamicSerializableFA().serialize(f, JsonOps.INSTANCE))
            .map(Dynamic::getValue)
            .collect(Collectors.toList());
        assertIterableEquals(fromCodec, fromDynamic);
    }

    @Test
    void compatibleCheckOfNBT() {
        List<INBT> fromCodec = examples.stream()
            .map(f -> encode(FluidAmount.codecFA(), NBTDynamicOps.INSTANCE, f))
            .map(Optional::get)
            .collect(Collectors.toList());
        List<INBT> fromDynamic = examples.stream()
            .map(f -> FluidAmount.dynamicSerializableFA().serialize(f, NBTDynamicOps.INSTANCE))
            .map(Dynamic::getValue)
            .collect(Collectors.toList());
        assertIterableEquals(fromCodec, fromDynamic);
    }

    @Test
    void asCodecTest() {
        List<JsonElement> fromCodec = examples.stream()
            .map(f -> encode(FluidAmount.codecFA(), JsonOps.INSTANCE, f))
            .map(Optional::get)
            .collect(Collectors.toList());
        List<JsonElement> fromDynamicCodec = examples.stream()
            .map(f -> encode(FluidAmount.dynamicSerializableFA().asCodec(), JsonOps.INSTANCE, f))
            .map(Optional::get)
            .collect(Collectors.toList());
        assertIterableEquals(fromCodec, fromDynamicCodec);
    }

    @Test
    void asCodecTest2() {
        List<JsonElement> fromCodec = examples.stream()
            .map(f -> FluidAmount.dynamicSerializableFromCodecFA().serialize(f, JsonOps.INSTANCE))
            .map(Dynamic::getValue)
            .collect(Collectors.toList());
        List<JsonElement> fromDynamic = examples.stream()
            .map(f -> FluidAmount.dynamicSerializableFA().serialize(f, JsonOps.INSTANCE))
            .map(Dynamic::getValue)
            .collect(Collectors.toList());
        assertIterableEquals(fromCodec, fromDynamic);
    }
}
