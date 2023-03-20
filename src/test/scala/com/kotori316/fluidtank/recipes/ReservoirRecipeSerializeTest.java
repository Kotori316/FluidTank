package com.kotori316.fluidtank.recipes;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.tiles.Tier;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ReservoirRecipeSerializeTest extends BeforeAllTest {

    @Test
    void dummy() {
        assertTrue(tierAndIngredient().findAny().isPresent());
        FriendlyByteBuf buffer = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer());
        assertNotNull(buffer);
    }

    static Stream<Object[]> tierAndIngredient() {
        return Stream.of(Tier.WOOD, Tier.STONE, Tier.IRON)
            .flatMap(t -> Stream.of(Items.BUCKET, Items.APPLE).map(Ingredient::of)
                .map(i -> new Object[]{t, i, ICondition.IContext.EMPTY}));
    }

    @ParameterizedTest
    @MethodSource("tierAndIngredient")
    void serializePacket(Tier t, Ingredient sub, ICondition.IContext ignore1) {
        ReservoirRecipe recipe = new ReservoirRecipe(new ResourceLocation("test:reservoir_" + t.lowerName()), t, Collections.singletonList(sub));

        FriendlyByteBuf buffer = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer());
        ReservoirRecipe.SERIALIZER.toNetwork(buffer, recipe);
        ReservoirRecipe read = ReservoirRecipe.SERIALIZER.fromNetwork(recipe.getId(), buffer);
        assertNotNull(read);
        assertAll(
            () -> assertEquals(recipe.getTier(), read.getTier()),
            () -> assertNotEquals(Items.AIR, read.getResultItem(RegistryAccess.EMPTY).getItem()),
            () -> assertEquals(convertIngredients(recipe.getIngredients()), convertIngredients(read.getIngredients()), "All ingredients must be same."),
            () -> assertEquals(recipe.getResultItem(RegistryAccess.EMPTY).getItem(), read.getResultItem(RegistryAccess.EMPTY).getItem())
        );
    }

    @ParameterizedTest
    @MethodSource("tierAndIngredient")
    void serializeJson(Tier t, Ingredient sub, ICondition.IContext context) {
        ReservoirRecipe recipe = new ReservoirRecipe(new ResourceLocation("test:reservoir_" + t.lowerName()), t, Collections.singletonList(sub));

        JsonObject object = new JsonObject();
        new ReservoirRecipe.ReservoirFinishedRecipe(recipe).serializeRecipeData(object);
        ReservoirRecipe read = ReservoirRecipe.SERIALIZER.fromJson(recipe.getId(), object, context);
        assertNotNull(read);
        assertAll(
            () -> assertEquals(recipe.getTier(), read.getTier()),
            () -> assertNotEquals(Items.AIR, read.getResultItem(RegistryAccess.EMPTY).getItem()),
            () -> assertEquals(convertIngredients(recipe.getIngredients()), convertIngredients(read.getIngredients()), "All ingredients must be same."),
            () -> assertEquals(recipe.getResultItem(RegistryAccess.EMPTY).getItem(), read.getResultItem(RegistryAccess.EMPTY).getItem())
        );
    }

    static Set<JsonElement> convertIngredients(Collection<Ingredient> ingredients) {
        return ingredients.stream().map(Ingredient::toJson).collect(Collectors.toSet());
    }
}
