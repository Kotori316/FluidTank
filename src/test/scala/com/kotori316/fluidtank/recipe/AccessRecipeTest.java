package com.kotori316.fluidtank.recipe;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.items.TankItemFluidHandler;
import com.kotori316.fluidtank.recipes.TierRecipe;
import com.kotori316.fluidtank.tiles.Tiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessRecipeTest extends BeforeAllTest {
    private static final BlockTank woodTank = ModObjects.blockTanks().head();
    TierRecipe recipe;
    CraftingInventory inventory;

    @BeforeEach
    void setupEach() {
        recipe = new TierRecipe(new ResourceLocation(FluidTank.modID, "access_tier"), Tiers.STONE(), Ingredient.fromItems(Blocks.STONE));
        inventory = new CraftingInventory(new DummyContainer(), 3, 3);
    }

    @Test
    void createInstance() {
        TierRecipe recipe = new TierRecipe(new ResourceLocation(FluidTank.modID, "test_1"), Tiers.STONE(), Ingredient.fromItems(Blocks.STONE));
        assertNotNull(recipe);
        assertNotNull(this.recipe);
    }

    @Test
    void fromEmptyTank() {
        ItemStack stack = new ItemStack(woodTank);
        for (int i : new int[]{0, 2, 6, 8}) {
            inventory.setInventorySlotContents(i, stack);
        }
        for (int i : new int[]{1, 3, 5, 7}) {
            inventory.setInventorySlotContents(i, new ItemStack(Blocks.STONE));
        }

        assertTrue(recipe.matches(inventory, null));
        ItemStack result = recipe.getCraftingResult(inventory);
        assertFalse(result.hasTag());
    }

    @Test
    void mixWaterLava() {
        {
            ItemStack stack = new ItemStack(woodTank);
            new TankItemFluidHandler((ItemBlockTank) stack.getItem(), stack).fill(FluidAmount.BUCKET_WATER().toStack(), IFluidHandler.FluidAction.EXECUTE);
            inventory.setInventorySlotContents(0, stack);
        }
        {
            ItemStack stack = new ItemStack(woodTank);
            new TankItemFluidHandler((ItemBlockTank) stack.getItem(), stack).fill(FluidAmount.BUCKET_LAVA().toStack(), IFluidHandler.FluidAction.EXECUTE);
            inventory.setInventorySlotContents(2, stack);
        }
        ItemStack stack = new ItemStack(woodTank);
        for (int i : new int[]{6, 8}) {
            inventory.setInventorySlotContents(i, stack);
        }
        for (int i : new int[]{1, 3, 5, 7}) {
            inventory.setInventorySlotContents(i, new ItemStack(Blocks.STONE));
        }
        assertFalse(recipe.matches(inventory, null));
    }

    static FluidAmount[] fluids1() {
        return LongStream.of(500, 1000, 2000, 3000, 4000).boxed().flatMap(l ->
            Stream.of(FluidAmount.BUCKET_WATER(), FluidAmount.BUCKET_LAVA())
                .map(f -> f.setAmount(l))).toArray(FluidAmount[]::new);
    }

    @ParameterizedTest
    @MethodSource("fluids1")
    void combineFluidTest(FluidAmount amount) {
        {
            ItemStack stack = new ItemStack(woodTank);
            new TankItemFluidHandler((ItemBlockTank) stack.getItem(), stack).fill(amount.toStack(), IFluidHandler.FluidAction.EXECUTE);
            inventory.setInventorySlotContents(0, stack);
        }
        ItemStack stack = new ItemStack(woodTank);
        for (int i : new int[]{2, 6, 8}) {
            inventory.setInventorySlotContents(i, stack);
        }
        for (int i : new int[]{1, 3, 5, 7}) {
            inventory.setInventorySlotContents(i, new ItemStack(Blocks.STONE));
        }
        assertTrue(recipe.matches(inventory, null));
        ItemStack result = recipe.getCraftingResult(inventory);
        TankItemFluidHandler filled = new TankItemFluidHandler((ItemBlockTank) result.getItem(), result);
        filled.init();
        assertEquals(amount, filled.getFluid());
    }

    private static final class DummyContainer extends Container {

        private DummyContainer() {
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
