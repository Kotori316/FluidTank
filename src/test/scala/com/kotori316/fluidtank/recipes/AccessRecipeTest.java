package com.kotori316.fluidtank.recipes;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.tiles.Tier;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AccessRecipeTest extends BeforeAllTest {
    static Stream<Tier> tiers() {
        return Arrays.stream(Tier.values()).filter(Tier::hasTagRecipe);
    }

    static Stream<Object[]> tierWithContext() {
        return tiers().map(t -> new Object[]{t, ICondition.IContext.EMPTY});
    }

    @ParameterizedTest
    @MethodSource("tierWithContext")
    void createTierRecipeInstance(Tier tier, ICondition.IContext context) {
        TierRecipe recipe = new TierRecipe(new ResourceLocation(FluidTank.modID, "test_" + tier.lowerName()), tier, Ingredient.of(Blocks.STONE), context);
        assertNotNull(recipe);
    }

    @Test
    void dummy() {
        assertTrue(tiers().findAny().isPresent());
        assertTrue(tierWithContext().findAny().isPresent());
        assertTrue(TierRecipeTest.fluids1().length > 0);
        assertTrue(ReservoirRecipeSerializeTest.tierAndIngredient().findAny().isPresent());
        FriendlyByteBuf buffer = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer());
        assertNotNull(buffer);
    }

    static final class ReservoirRecipeSerializeTest extends BeforeAllTest {
        static Stream<Object> tierAndIngredient() {
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
                () -> assertNotEquals(Items.AIR, read.getResultItem().getItem()),
                () -> assertEquals(recipe.getResultItem().getItem(), read.getResultItem().getItem())
            );
        }

        @ParameterizedTest
        @MethodSource("tierAndIngredient")
        @Disabled("Deserialization of Ingredient is not available in test environment.")
        void serializeJson(Tier t, Ingredient sub, ICondition.IContext context) {
            ReservoirRecipe recipe = new ReservoirRecipe(new ResourceLocation("test:reservoir_" + t.lowerName()), t, Collections.singletonList(sub));

            JsonObject object = new JsonObject();
            new ReservoirRecipe.ReservoirFinishedRecipe(recipe).serializeRecipeData(object);
            ReservoirRecipe read = ReservoirRecipe.SERIALIZER.fromJson(recipe.getId(), object, context);
            assertNotNull(read);
            assertAll(
                () -> assertEquals(recipe.getTier(), read.getTier()),
                () -> assertNotEquals(Items.AIR, read.getResultItem().getItem()),
                () -> assertEquals(recipe.getResultItem().getItem(), read.getResultItem().getItem())
            );
        }
    }

    static final class DummyContainer extends AbstractContainerMenu {

        DummyContainer() {
            super(null, 35);
            SimpleContainer inventory = new SimpleContainer(9);
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                addSlot(new Slot(inventory, i, 0, 0));
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }
    }
}
