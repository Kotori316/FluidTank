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
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.fluidtank.tiles.TileTank;

public class ReservoirRecipe extends ShapelessRecipe {
    public static final RecipeSerializer<ReservoirRecipe> SERIALIZER = new Serializer();
    public static final String GROUP = "fluidtank:reservoirs";
    private final Tier tier;
    private final List<Ingredient> subIngredients;

    public ReservoirRecipe(ResourceLocation idIn, Tier tier, List<Ingredient> subIngredients) {
        super(idIn, GROUP, findOutput(tier), findIngredients(tier, subIngredients));
        this.tier = tier;
        this.subIngredients = subIngredients;
    }

    ReservoirRecipe(ResourceLocation idIn, Tier tier) {
        // Helper method for test.
        this(idIn, tier, Collections.singletonList(Ingredient.of(Items.BUCKET)));
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack result = super.assemble(inv);
        IntStream.range(0, inv.getContainerSize())
            .mapToObj(inv::getItem)
            .filter(s -> s.getItem() instanceof ItemBlockTank)
            .filter(ItemStack::hasTag)
            .map(s -> s.getTagElement(TileTank.NBT_BlockTag()))
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(nbt -> result.addTagElement(TileTank.NBT_BlockTag(), nbt));
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    private static ItemStack findOutput(Tier tier) {
        return CollectionConverters.asJava(ModObjects.itemReservoirs()).stream().filter(i -> i.tier() == tier).findFirst().map(ItemStack::new).orElseThrow(
            () -> new IllegalStateException("Reservoir of " + tier + " not found.")
        );
    }

    Tier getTier() { // For test
        return tier;
    }

    private static NonNullList<Ingredient> findIngredients(Tier tier, List<Ingredient> subIngredients) {
        NonNullList<Ingredient> recipeItemsIn = NonNullList.create();
        Stream<BlockTank> tankStream = CollectionConverters.asJava(ModObjects.blockTanks()).stream().filter(b -> b.tier() == tier);

        Ingredient tankIngredient = Ingredient.of(tankStream.map(ItemStack::new));
        recipeItemsIn.add(tankIngredient);
        recipeItemsIn.addAll(subIngredients);
        return recipeItemsIn;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ReservoirRecipe> {
        public Serializer() {
            setRegistryName(new ResourceLocation(FluidTank.modID, "reservoir_recipe"));
        }

        @Override
        public ReservoirRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Tier tier = Tier.byName(GsonHelper.getAsString(json, "tier")).orElse(Tier.Invalid);
            List<Ingredient> ingredientList;
            if (json.has("sub"))
                ingredientList = StreamSupport.stream(GsonHelper.getAsJsonArray(json, "sub").spliterator(), false)
                    .map(CraftingHelper::getIngredient)
                    .collect(Collectors.toList());
            else
                ingredientList = Collections.singletonList(Ingredient.of(Items.BUCKET));
            if (ingredientList.isEmpty() || ingredientList.size() > 8) {
                FluidTank.LOGGER.error("Too many or too few items to craft reservoir. Size: {}, {}, Recipe: {}",
                    ingredientList.size(), ingredientList, recipeId);
            }
            return new ReservoirRecipe(recipeId, tier, ingredientList);
        }

        @Override
        public ReservoirRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String tierName = buffer.readUtf();
            Tier tier = Tier.byName(tierName).orElseThrow(IllegalArgumentException::new);
            int subIngredientCount = buffer.readVarInt();
            List<Ingredient> ingredients = IntStream.range(0, subIngredientCount)
                .mapToObj(i -> Ingredient.fromNetwork(buffer))
                .collect(Collectors.toList());
            return new ReservoirRecipe(recipeId, tier, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ReservoirRecipe recipe) {
            buffer.writeUtf(recipe.tier.toString());
            buffer.writeVarInt(recipe.subIngredients.size());
            recipe.subIngredients.forEach(i -> i.toNetwork(buffer));
        }
    }

    public static class ReservoirFinishedRecipe implements FinishedRecipe {
        private final ReservoirRecipe recipe;

        public ReservoirFinishedRecipe(ReservoirRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("tier", recipe.tier.lowerName());
            JsonArray subIngredients = new JsonArray();
            recipe.subIngredients.stream().map(Ingredient::toJson).forEach(subIngredients::add);
            json.add("sub", subIngredients);
        }

        @Override
        public ResourceLocation getId() {
            return recipe.getId();
        }

        @Override
        public RecipeSerializer<?> getType() {
            return SERIALIZER;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return new ResourceLocation("");
        }
    }
}
