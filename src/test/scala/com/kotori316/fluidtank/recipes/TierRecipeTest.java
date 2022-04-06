package com.kotori316.fluidtank.recipes;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.Tier;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TierRecipeTest extends BeforeAllTest {

    private static final BlockTank woodTank = ModObjects.tierToBlock().apply(Tier.WOOD);

    TierRecipe recipe;
    CraftingContainer inventory;

    @BeforeEach
    void setupEach() {
        recipe = new TierRecipe(new ResourceLocation(FluidTank.modID, "access_tier"), Tier.STONE, Ingredient.of(Blocks.STONE), Set.of(woodTank));
        inventory = new CraftingContainer(new AccessRecipeTest.DummyContainer(), 3, 3);
    }

    @ParameterizedTest
    @MethodSource("tierWithContext")
    void createTierRecipeInstance(Tier tier, ICondition.IContext context) {
        TierRecipe recipe = new TierRecipe(new ResourceLocation(FluidTank.modID, "test_" + tier.lowerName()), tier, Ingredient.of(Blocks.STONE), context);
        assertNotNull(recipe);
    }

    @Test
    void fromEmptyTank() {
        ItemStack stack = new ItemStack(woodTank);
        for (int i : new int[]{0, 2, 6, 8}) {
            inventory.setItem(i, stack);
        }
        for (int i : new int[]{1, 3, 5, 7}) {
            inventory.setItem(i, new ItemStack(Blocks.STONE));
        }

        assertTrue(recipe.matches(inventory, null));
        ItemStack result = recipe.assemble(inventory);
        assertFalse(result.hasTag());
    }

    @Test
    void mixWaterLava() {
        {
            ItemStack stack = new ItemStack(woodTank);
            RecipeInventoryUtil.getFluidHandler(stack).fill(FluidAmount.BUCKET_WATER().toStack(), IFluidHandler.FluidAction.EXECUTE);
            inventory.setItem(0, stack);
        }
        {
            ItemStack stack = new ItemStack(woodTank);
            RecipeInventoryUtil.getFluidHandler(stack).fill(FluidAmount.BUCKET_LAVA().toStack(), IFluidHandler.FluidAction.EXECUTE);
            inventory.setItem(2, stack);
        }
        ItemStack stack = new ItemStack(woodTank);
        for (int i : new int[]{6, 8}) {
            inventory.setItem(i, stack);
        }
        for (int i : new int[]{1, 3, 5, 7}) {
            inventory.setItem(i, new ItemStack(Blocks.STONE));
        }
        assertFalse(recipe.matches(inventory, null));
    }

    static FluidAmount[] fluids1() {
        return LongStream.of(500, 1000, 2000, 3000, 4000).boxed().flatMap(l ->
            Stream.of(FluidAmount.BUCKET_WATER(), FluidAmount.BUCKET_LAVA())
                .map(f -> f.setAmount(l))).toArray(FluidAmount[]::new);
    }

    static Stream<Object[]> tierWithContext() {
        return Arrays.stream(Tier.values()).filter(Tier::hasTagRecipe).map(t -> new Object[]{t, ICondition.IContext.EMPTY});
    }

    @ParameterizedTest
    @MethodSource("fluids1")
    void combineFluidTest(FluidAmount amount) {
        {
            ItemStack stack = new ItemStack(woodTank);
            RecipeInventoryUtil.getFluidHandler(stack).fill(amount.toStack(), IFluidHandler.FluidAction.EXECUTE);
            inventory.setItem(0, stack);
        }
        ItemStack stack = new ItemStack(woodTank);
        for (int i : new int[]{2, 6, 8}) {
            inventory.setItem(i, stack);
        }
        for (int i : new int[]{1, 3, 5, 7}) {
            inventory.setItem(i, new ItemStack(Blocks.STONE));
        }
        assertTrue(recipe.matches(inventory, null));
        ItemStack result = recipe.assemble(inventory);
        assertEquals(amount, RecipeInventoryUtil.getFluidHandler(result).getFluid());
    }

    @ParameterizedTest
    @MethodSource("fluids1")
    void combine2FluidTest(FluidAmount amount) {
        {
            ItemStack stack = new ItemStack(woodTank);
            RecipeInventoryUtil.getFluidHandler(stack).fill(amount.toStack(), IFluidHandler.FluidAction.EXECUTE);
            inventory.setItem(0, stack);
            inventory.setItem(2, stack);
        }
        ItemStack stack = new ItemStack(woodTank);
        for (int i : new int[]{6, 8}) {
            inventory.setItem(i, stack);
        }
        for (int i : new int[]{1, 3, 5, 7}) {
            inventory.setItem(i, new ItemStack(Blocks.STONE));
        }
        assertTrue(recipe.matches(inventory, null));
        ItemStack result = recipe.assemble(inventory);
        assertEquals(amount.setAmount(amount.amount() * 2), RecipeInventoryUtil.getFluidHandler(result).getFluid());
    }

    @Test
    void mixNBTFluid() {
        FluidStack a = new FluidStack(Fluids.WATER, 1000);
        {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("name", "a");
            nbt.putInt("amount", 1000);
            a.setTag(nbt);
        }
        FluidStack b = new FluidStack(Fluids.WATER, 2000);
        {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("name", "b");
            nbt.putInt("amount", 2000);
            b.setTag(nbt);
        }
        ItemStack stack1 = new ItemStack(woodTank);
        RecipeInventoryUtil.getFluidHandler(stack1).fill(a, IFluidHandler.FluidAction.EXECUTE);
        ItemStack stack2 = new ItemStack(woodTank);
        RecipeInventoryUtil.getFluidHandler(stack2).fill(b, IFluidHandler.FluidAction.EXECUTE);
        ItemStack stack = new ItemStack(woodTank);
        inventory.setItem(0, stack1);
        inventory.setItem(2, stack2);
        inventory.setItem(6, stack);
        inventory.setItem(8, stack);
        for (int i : new int[]{1, 3, 5, 7}) {
            inventory.setItem(i, new ItemStack(Blocks.STONE));
        }

        assertFalse(recipe.matches(inventory, null));
    }

    @ParameterizedTest
    @MethodSource("tierWithContext")
    @Disabled("Accessing tag before bounded is not allowed.")
    void serializeJson(Tier tier, ICondition.IContext context) {
        TierRecipe recipe = new TierRecipe(new ResourceLocation(FluidTank.modID, "test_" + tier.lowerName()),
            tier, Ingredient.of(ItemTags.create(new ResourceLocation(tier.tagName()))), Set.of(woodTank));
        JsonObject object = new JsonObject();
        TierRecipe.TierFinishedRecipe tierFinishedRecipe = new TierRecipe.TierFinishedRecipe(recipe.getId(), tier);
        tierFinishedRecipe.serializeRecipeData(object);
        TierRecipe read = TierRecipe.SERIALIZER.fromJson(recipe.getId(), object, context);
        assertNotNull(read);
        assertAll(
            () -> assertEquals(recipe.getTier(), read.getTier()),
            () -> assertNotEquals(Items.AIR, read.getResultItem().getItem()),
            () -> assertEquals(recipe.getResultItem().getItem(), read.getResultItem().getItem()),
            () -> assertEquals(recipe.getSubItems().toJson(), read.getSubItems().toJson())
        );
    }

    @ParameterizedTest
    @MethodSource("tierWithContext")
    void serializePacket(Tier tier, ICondition.IContext ignore) {
        TierRecipe recipe = new TierRecipe(new ResourceLocation(FluidTank.modID, "test_" + tier.lowerName()),
            tier, Ingredient.of(Blocks.STONE), Set.of(woodTank));
        FriendlyByteBuf buffer = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer());
        TierRecipe.SERIALIZER.toNetwork(buffer, recipe);
        TierRecipe read = TierRecipe.SERIALIZER.fromNetwork(recipe.getId(), buffer);
        assertNotNull(read);
        assertAll(
            () -> assertEquals(recipe.getTier(), read.getTier()),
            () -> assertNotEquals(Items.AIR, read.getResultItem().getItem()),
            () -> assertEquals(recipe.getResultItem().getItem(), read.getResultItem().getItem()),
            () -> assertEquals(recipe.getSubItems().toJson(), read.getSubItems().toJson())
        );
    }
}
