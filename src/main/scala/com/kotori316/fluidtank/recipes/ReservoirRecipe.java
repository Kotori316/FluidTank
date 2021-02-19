package com.kotori316.fluidtank.recipes;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import javax.annotation.Nullable;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.tiles.Tiers;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

public class ReservoirRecipe extends ShapelessRecipe {
    public static final IRecipeSerializer<ReservoirRecipe> SERIALIZER = new Serializer();
    public static final String GROUP = "fluidtank:reservoirs";
    private final Tiers tier;

    public ReservoirRecipe(ResourceLocation idIn, Tiers tier) {
        super(idIn, GROUP, findOutput(tier), findIngredients(tier));
        this.tier = tier;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack result = super.getCraftingResult(inv);
        IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot)
            .filter(s -> s.getItem() instanceof ItemBlockTank)
            .filter(ItemStack::hasTag)
            .map(s -> s.getChildTag(TileTankNoDisplay.NBT_BlockTag()))
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(nbt -> result.setTagInfo(TileTankNoDisplay.NBT_BlockTag(), nbt));
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    private static ItemStack findOutput(Tiers tier) {
        return CollectionConverters.asJava(ModObjects.itemReservoirs()).stream().filter(i -> i.tier() == tier).findFirst().map(ItemStack::new).orElseThrow(
            () -> new IllegalStateException("Reservoir of " + tier + " not found.")
        );
    }

    private static NonNullList<Ingredient> findIngredients(Tiers tier) {
        NonNullList<Ingredient> recipeItemsIn = NonNullList.create();
        Stream<BlockTank> tankStream = CollectionConverters.asJava(ModObjects.blockTanks()).stream().filter(b -> b.tier() == tier);
        Stream<BlockTank> invisibleStream = CollectionConverters.asJava(ModObjects.blockTanksInvisible()).stream().filter(b -> b.tier() == tier);

        Ingredient tankIngredient;
        if (Config.content().usableInvisibleInRecipe().get()) {
            tankIngredient = Ingredient.fromStacks(Stream.concat(tankStream, invisibleStream).map(ItemStack::new));
        } else {
            tankIngredient = Ingredient.fromStacks(tankStream.map(ItemStack::new));
        }
        recipeItemsIn.add(tankIngredient);
        recipeItemsIn.add(Ingredient.fromItems(Items.BUCKET));
        return recipeItemsIn;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ReservoirRecipe> {
        public Serializer() {
            setRegistryName(new ResourceLocation(FluidTank.modID, "reservoir_recipe"));
        }

        @Override
        public ReservoirRecipe read(ResourceLocation recipeId, JsonObject json) {
            Tiers tier = Tiers.TierDynamicSerialize().deserialize(new Dynamic<>(JsonOps.INSTANCE, json.get("tier")));
            return new ReservoirRecipe(recipeId, tier);
        }

        @Override
        public ReservoirRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            String tierName = buffer.readString();
            Tiers tier = Tiers.byName(tierName).get();
            return new ReservoirRecipe(recipeId, tier);
        }

        @Override
        public void write(PacketBuffer buffer, ReservoirRecipe recipe) {
            buffer.writeString(recipe.tier.toString());
        }
    }

    public static class FinishedRecipe implements IFinishedRecipe {
        private final ReservoirRecipe recipe;

        public FinishedRecipe(ReservoirRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void serialize(JsonObject json) {
            json.add("tier", Tiers.TierDynamicSerialize().serialize(recipe.tier, JsonOps.INSTANCE).getValue());
        }

        @Override
        public ResourceLocation getID() {
            return recipe.getId();
        }

        @Override
        public IRecipeSerializer<?> getSerializer() {
            return SERIALIZER;
        }

        @Nullable
        @Override
        public JsonObject getAdvancementJson() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementID() {
            return new ResourceLocation("");
        }
    }
}
