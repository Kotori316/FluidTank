package com.kotori316.fluidtank.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import scala.jdk.javaapi.CollectionConverters;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.tiles.Tiers;
import com.kotori316.fluidtank.tiles.TileTank;

public class ReservoirRecipe extends ShapelessRecipe {
    public static final IRecipeSerializer<ReservoirRecipe> SERIALIZER = new Serializer();
    public static final String GROUP = "fluidtank:reservoirs";
    private final Tiers tier;
    private final List<Ingredient> subIngredients;

    public ReservoirRecipe(ResourceLocation idIn, Tiers tier, List<Ingredient> subIngredients) {
        super(idIn, GROUP, findOutput(tier), findIngredients(tier, subIngredients));
        this.tier = tier;
        this.subIngredients = subIngredients;
    }

    ReservoirRecipe(ResourceLocation idIn, Tiers tier) {
        // Helper method for test.
        this(idIn, tier, Collections.singletonList(Ingredient.fromItems(Items.BUCKET)));
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack result = super.getCraftingResult(inv);
        IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot)
            .filter(s -> s.getItem() instanceof ItemBlockTank)
            .filter(ItemStack::hasTag)
            .map(s -> s.getChildTag(TileTank.NBT_BlockTag()))
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(nbt -> result.setTagInfo(TileTank.NBT_BlockTag(), nbt));
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

    Tiers getTier() { // For test
        return tier;
    }

    private static NonNullList<Ingredient> findIngredients(Tiers tier, List<Ingredient> subIngredients) {
        NonNullList<Ingredient> recipeItemsIn = NonNullList.create();
        Stream<BlockTank> tankStream = CollectionConverters.asJava(ModObjects.blockTanks()).stream().filter(b -> b.tier() == tier);

        Ingredient tankIngredient = Ingredient.fromStacks(tankStream.map(ItemStack::new));
        recipeItemsIn.add(tankIngredient);
        recipeItemsIn.addAll(subIngredients);
        return recipeItemsIn;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ReservoirRecipe> {
        public Serializer() {
            setRegistryName(new ResourceLocation(FluidTank.modID, "reservoir_recipe"));
        }

        @Override
        public ReservoirRecipe read(ResourceLocation recipeId, JsonObject json) {
            Tiers tier = OptionConverters.toJava(Tiers.byName(JSONUtils.getString(json, "tier"))).orElse(Tiers.Invalid());
            List<Ingredient> ingredientList;
            if (json.has("sub"))
                ingredientList = StreamSupport.stream(JSONUtils.getJsonArray(json, "sub").spliterator(), false)
                    .map(CraftingHelper::getIngredient)
                    .collect(Collectors.toList());
            else
                ingredientList = Collections.singletonList(Ingredient.fromItems(Items.BUCKET));
            if (ingredientList.isEmpty() || ingredientList.size() > 8) {
                FluidTank.LOGGER.error("Too many or too few items to craft reservoir. Size: {}, {}, Recipe: {}",
                    ingredientList.size(), ingredientList, recipeId);
            }
            return new ReservoirRecipe(recipeId, tier, ingredientList);
        }

        @Override
        public ReservoirRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            String tierName = buffer.readString();
            Tiers tier = Tiers.byName(tierName).get();
            int subIngredientCount = buffer.readVarInt();
            List<Ingredient> ingredients = IntStream.range(0, subIngredientCount)
                .mapToObj(i -> Ingredient.read(buffer))
                .collect(Collectors.toList());
            return new ReservoirRecipe(recipeId, tier, ingredients);
        }

        @Override
        public void write(PacketBuffer buffer, ReservoirRecipe recipe) {
            buffer.writeString(recipe.tier.toString());
            buffer.writeVarInt(recipe.subIngredients.size());
            recipe.subIngredients.forEach(i -> i.write(buffer));
        }
    }

    public static class FinishedRecipe implements IFinishedRecipe {
        private final ReservoirRecipe recipe;

        public FinishedRecipe(ReservoirRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void serialize(JsonObject json) {
            json.addProperty("tier", recipe.tier.lowerName());
            JsonArray subIngredients = new JsonArray();
            recipe.subIngredients.stream().map(Ingredient::serialize).forEach(subIngredients::add);
            json.add("sub", subIngredients);
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
