package com.kotori316.fluidtank.recipes;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.Tiers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TierRecipeTest {

    private static final BlockTank woodTank = ModObjects.blockTanks().head();

    TierRecipe recipe;
    CraftingInventory inventory;

    @BeforeEach
    void setupEach() {
        recipe = new TierRecipe(new ResourceLocation(FluidTank.modID, "access_tier"), Tiers.STONE(), Ingredient.fromItems(Blocks.STONE));
        inventory = new CraftingInventory(new AccessRecipeTest.DummyContainer(), 3, 3);
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
            RecipeInventoryUtil.getFluidHandler(stack).fill(FluidAmount.BUCKET_WATER().toStack(), IFluidHandler.FluidAction.EXECUTE);
            inventory.setInventorySlotContents(0, stack);
        }
        {
            ItemStack stack = new ItemStack(woodTank);
            RecipeInventoryUtil.getFluidHandler(stack).fill(FluidAmount.BUCKET_LAVA().toStack(), IFluidHandler.FluidAction.EXECUTE);
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
            RecipeInventoryUtil.getFluidHandler(stack).fill(amount.toStack(), IFluidHandler.FluidAction.EXECUTE);
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
        assertEquals(amount, RecipeInventoryUtil.getFluidHandler(result).getFluid());
    }

    @ParameterizedTest
    @MethodSource("fluids1")
    void combine2FluidTest(FluidAmount amount) {
        {
            ItemStack stack = new ItemStack(woodTank);
            RecipeInventoryUtil.getFluidHandler(stack).fill(amount.toStack(), IFluidHandler.FluidAction.EXECUTE);
            inventory.setInventorySlotContents(0, stack);
            inventory.setInventorySlotContents(2, stack);
        }
        ItemStack stack = new ItemStack(woodTank);
        for (int i : new int[]{6, 8}) {
            inventory.setInventorySlotContents(i, stack);
        }
        for (int i : new int[]{1, 3, 5, 7}) {
            inventory.setInventorySlotContents(i, new ItemStack(Blocks.STONE));
        }
        assertTrue(recipe.matches(inventory, null));
        ItemStack result = recipe.getCraftingResult(inventory);
        assertEquals(amount.setAmount(amount.amount() * 2), RecipeInventoryUtil.getFluidHandler(result).getFluid());
    }

    @Test
    void mixNBTFluid() {
        FluidStack a = new FluidStack(Fluids.WATER, 1000);
        {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("name", "a");
            nbt.putInt("amount", 1000);
            a.setTag(nbt);
        }
        FluidStack b = new FluidStack(Fluids.WATER, 2000);
        {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("name", "b");
            nbt.putInt("amount", 2000);
            b.setTag(nbt);
        }
        ItemStack stack1 = new ItemStack(woodTank);
        RecipeInventoryUtil.getFluidHandler(stack1).fill(a, IFluidHandler.FluidAction.EXECUTE);
        ItemStack stack2 = new ItemStack(woodTank);
        RecipeInventoryUtil.getFluidHandler(stack2).fill(b, IFluidHandler.FluidAction.EXECUTE);
        ItemStack stack = new ItemStack(woodTank);
        inventory.setInventorySlotContents(0, stack1);
        inventory.setInventorySlotContents(2, stack2);
        inventory.setInventorySlotContents(6, stack);
        inventory.setInventorySlotContents(8, stack);
        for (int i : new int[]{1, 3, 5, 7}) {
            inventory.setInventorySlotContents(i, new ItemStack(Blocks.STONE));
        }

        assertFalse(recipe.matches(inventory, null));
    }

    @ParameterizedTest
    @MethodSource("com.kotori316.fluidtank.recipes.AccessRecipeTest#tiers")
    @Disabled("Accessing tag before bounded is not allowed.")
    void serializeJson(Tiers tier) {
        TierRecipe recipe = new TierRecipe(new ResourceLocation(FluidTank.modID, "test_" + tier.lowerName()),
            tier, Ingredient.fromItems(Blocks.STONE));
        JsonObject object = new JsonObject();
        TierRecipe.FinishedRecipe finishedRecipe = new TierRecipe.FinishedRecipe(recipe.getId(), tier);
        finishedRecipe.serialize(object);
        TierRecipe read = TierRecipe.SERIALIZER.read(recipe.getId(), object);
        assertNotNull(read);
        assertAll(
            () -> assertEquals(recipe.getTier(), read.getTier()),
            () -> assertNotEquals(Items.AIR, read.getRecipeOutput().getItem()),
            () -> assertEquals(recipe.getRecipeOutput().getItem(), read.getRecipeOutput().getItem()),
            () -> assertEquals(recipe.getSubItems().serialize(), read.getSubItems().serialize())
        );
    }

    @ParameterizedTest
    @MethodSource("com.kotori316.fluidtank.recipes.AccessRecipeTest#tiers")
    void serializePacket(Tiers tier) {
        TierRecipe recipe = new TierRecipe(new ResourceLocation(FluidTank.modID, "test_" + tier.lowerName()),
            tier, Ingredient.fromItems(Blocks.STONE));
        PacketBuffer buffer = new PacketBuffer(ByteBufAllocator.DEFAULT.buffer());
        TierRecipe.SERIALIZER.write(buffer, recipe);
        TierRecipe read = TierRecipe.SERIALIZER.read(recipe.getId(), buffer);
        assertNotNull(read);
        assertAll(
            () -> assertEquals(recipe.getTier(), read.getTier()),
            () -> assertNotEquals(Items.AIR, read.getRecipeOutput().getItem()),
            () -> assertEquals(recipe.getRecipeOutput().getItem(), read.getRecipeOutput().getItem()),
            () -> assertEquals(recipe.getSubItems().serialize(), read.getSubItems().serialize())
        );
    }
}
