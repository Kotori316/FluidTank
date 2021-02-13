package com.kotori316.fluidtank.fluid;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.fluids.FluidAmount;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SerializeFA extends BeforeAllTest {
    private static final List<FluidAmount> examples = Arrays.asList(
        // Milk is not vanilla fluid, so deserialization fails.
        FluidAmount.BUCKET_LAVA(), /*FluidAmount.BUCKET_MILK()*/ FluidAmount.BUCKET_WATER(),
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
        List<FluidAmount> result = list.stream()
            .map(f -> codec.encodeStart(JsonOps.INSTANCE, f).flatMap(j -> codec.parse(JsonOps.INSTANCE, j)))
            .map(DataResult::result)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
        assertIterableEquals(list, result);
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

    @Test
    void errorCase1() {
        // Empty input.
        JsonElement element = new JsonObject();
        errorInput(element);
        errorInput(new JsonArray());
        errorInput(JsonNull.INSTANCE);
    }

    private static void errorInput(JsonElement element) {
        String message = "Got empty fluid from " + element;
        assertAll(
            () -> assertEquals(FluidAmount.EMPTY(), FluidAmount.dynamicSerializableFA().deserialize(new Dynamic<>(JsonOps.INSTANCE, element)), message + " by default."),
            () -> assertEquals(FluidAmount.EMPTY(), FluidAmount.dynamicSerializableFromCodecFA().deserialize(new Dynamic<>(JsonOps.INSTANCE, element)), message + " by converted codec."),
            () -> assertEquals(Optional.empty(), FluidAmount.codecFA().parse(JsonOps.INSTANCE, element).result(), message + " by codec.")
        );
    }

    @Test
    void errorCase2() {
        // Invalid input
        errorInput(new JsonPrimitive("fluid"));
        errorInput(new JsonPrimitive(151163521));
        errorInput(new JsonPrimitive(false));
    }

    @Test
    void errorCase3() {
        // invalid fluid name.
        JsonObject object = new JsonObject();
        object.addProperty("fluid", "dummy:dummy");
        object.addProperty("amount", 46000L);

        errorInput(object);

        assertTrue(FluidAmount.codecFA().parse(JsonOps.INSTANCE, object)
            .error()
            .map(DataResult.PartialResult::message)
            .filter(s -> s.contains("dummy:dummy") && s.contains("No fluid"))
            .isPresent()
        );
    }
}
