package com.kotori316.fluidtank.recipes;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.tiles.Tiers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void createConvertRecipeInstance() {
        ConvertInvisibleRecipe recipe = new ConvertInvisibleRecipe(new ResourceLocation(FluidTank.modID, "test_convert"));
        assertNotNull(recipe);
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
