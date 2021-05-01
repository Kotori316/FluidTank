package com.kotori316.fluidtank.recipes;

import java.util.Collections;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.tiles.Tiers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AccessRecipeTest extends BeforeAllTest {
    static Tiers[] tiers() {
        return Tiers.jList().stream().filter(Tiers::hasTagRecipe).toArray(Tiers[]::new);
    }

    @ParameterizedTest
    @MethodSource("tiers")
    void createTierRecipeInstance(Tiers tier) {
        TierRecipe recipe = new TierRecipe(new ResourceLocation(FluidTank.modID, "test_" + tier.lowerName()), tier, Ingredient.fromItems(Blocks.STONE));
        assertNotNull(recipe);
    }

    @Test
    void dummy() {
        assertTrue(tiers().length > 0);
        assertTrue(TierRecipeTest.fluids1().length > 0);
        assertTrue(ReservoirRecipeSerialize.tierAndIngredient().count() > 0);
        PacketBuffer buffer = new PacketBuffer(ByteBufAllocator.DEFAULT.buffer());
        assertNotNull(buffer);
    }

    static final class ReservoirRecipeSerialize extends BeforeAllTest {
        static Stream<Object> tierAndIngredient() {
            return Stream.of(Tiers.WOOD(), Tiers.STONE(), Tiers.IRON())
                .flatMap(t -> Stream.of(Items.BUCKET, Items.APPLE).map(Ingredient::fromItems)
                    .map(i -> new Object[]{t, i}));
        }

        @ParameterizedTest
        @MethodSource("tierAndIngredient")
        void serializePacket(Tiers t, Ingredient sub) {
            ReservoirRecipe recipe = new ReservoirRecipe(new ResourceLocation("test:reservoir_" + t.lowerName()), t, Collections.singletonList(sub));

            PacketBuffer buffer = new PacketBuffer(ByteBufAllocator.DEFAULT.buffer());
            ReservoirRecipe.SERIALIZER.write(buffer, recipe);
            ReservoirRecipe read = ReservoirRecipe.SERIALIZER.read(recipe.getId(), buffer);
            assertNotNull(read);
            assertAll(
                () -> assertEquals(recipe.getTier(), read.getTier()),
                () -> assertNotEquals(Items.AIR, read.getRecipeOutput().getItem()),
                () -> assertEquals(recipe.getRecipeOutput().getItem(), read.getRecipeOutput().getItem())
            );
        }

        @ParameterizedTest
        @MethodSource("tierAndIngredient")
        @Disabled("Deserialization of Ingredient is not available in test environment.")
        void serializeJson(Tiers t, Ingredient sub) {
            ReservoirRecipe recipe = new ReservoirRecipe(new ResourceLocation("test:reservoir_" + t.lowerName()), t, Collections.singletonList(sub));

            JsonObject object = new JsonObject();
            new ReservoirRecipe.FinishedRecipe(recipe).serialize(object);
            ReservoirRecipe read = ReservoirRecipe.SERIALIZER.read(recipe.getId(), object);
            assertNotNull(read);
            assertAll(
                () -> assertEquals(recipe.getTier(), read.getTier()),
                () -> assertNotEquals(Items.AIR, read.getRecipeOutput().getItem()),
                () -> assertEquals(recipe.getRecipeOutput().getItem(), read.getRecipeOutput().getItem())
            );
        }
    }

    static final class DummyContainer extends Container {

        DummyContainer() {
            super(null, 35);
            Inventory inventory = new Inventory(9);
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                addSlot(new Slot(inventory, i, 0, 0));
            }
        }

        @Override
        public boolean canInteractWith(PlayerEntity playerIn) {
            return false;
        }
    }
}
