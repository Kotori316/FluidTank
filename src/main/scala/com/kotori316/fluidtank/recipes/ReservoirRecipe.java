package com.kotori316.fluidtank.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.tiles.Tier;

public class ReservoirRecipe extends ShapelessRecipe {
    private static final Logger LOGGER = Utils.getLogger(ReservoirRecipe.class);
    public static final RecipeSerializer<ReservoirRecipe> SERIALIZER = new Serializer();
    public static final String GROUP = "fluidtank:reservoirs";
    private final Tier tier;
    private final List<Ingredient> subIngredients;

    public ReservoirRecipe(ResourceLocation idIn, Tier tier, List<Ingredient> subIngredients) {
        super(idIn, GROUP, findOutput(tier), findIngredients(tier, subIngredients));
        this.tier = tier;
        this.subIngredients = subIngredients;
        LOGGER.debug("{} instance({}) created for Tier {}({}).", getClass().getSimpleName(), idIn, tier, getResultItem());
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
            .map(BlockItem::getBlockEntityData)
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(nbt -> Utils.setTileTag(result, nbt));
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

        Ingredient tankIngredient = Ingredient.of(new ItemStack(ModObjects.tierToBlock().apply(tier)));
        recipeItemsIn.add(tankIngredient);
        recipeItemsIn.addAll(subIngredients);
        return recipeItemsIn;
    }

    public static class Serializer implements RecipeSerializer<ReservoirRecipe> {
        public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "reservoir_recipe");

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
                LOGGER.error("Too many or too few items to craft reservoir. Size: {}, {}, Recipe: {}",
                    ingredientList.size(), Utils.convertIngredientToString(ingredientList), recipeId);
            }
            LOGGER.debug("Serializer loaded {} from json for tier {}, sub {}.", recipeId, tier, Utils.convertIngredientToString(ingredientList));
            return new ReservoirRecipe(recipeId, tier, ingredientList);
        }

        @Override
        public ReservoirRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String tierName = buffer.readUtf();
            Tier tier = Tier.byName(tierName).orElseThrow(IllegalArgumentException::new);
            List<Ingredient> ingredients = buffer.readCollection(ArrayList::new, Ingredient::fromNetwork);
            LOGGER.debug("Serializer loaded {} from packet for tier {}, sub {}.", recipeId, tier, Utils.convertIngredientToString(ingredients));
            return new ReservoirRecipe(recipeId, tier, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ReservoirRecipe recipe) {
            buffer.writeUtf(recipe.tier.toString());
            buffer.writeCollection(recipe.subIngredients, (buf, ingredient) -> ingredient.toNetwork(buf));
            LOGGER.debug("Serialized {} to packet for tier {}.", recipe.getId(), recipe.getTier());
        }
    }

    record ReservoirFinishedRecipe(ReservoirRecipe recipe) implements FinishedRecipe {

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
