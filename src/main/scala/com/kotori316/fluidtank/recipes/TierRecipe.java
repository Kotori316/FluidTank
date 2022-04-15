package com.kotori316.fluidtank.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import scala.jdk.javaapi.CollectionConverters;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.FluidKey;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.fluidtank.tiles.TileTank;

public class TierRecipe implements CraftingRecipe, IShapedRecipe<CraftingContainer> {
    private static final Logger LOGGER = Utils.getLogger(TierRecipe.class);
    public static final Serializer SERIALIZER = new Serializer();
    private static final int[] TANK_SLOTS = {0, 2, 6, 8};
    private static final int[] SUB_SLOTS = {1, 3, 5, 7};
    private final ResourceLocation id;
    private final Tier tier;
    private final Set<BlockTank> normalTankSet;
    private final Ingredient subItems;
    private final ItemStack result;
    private static final int recipeWidth = 3;
    private static final int recipeHeight = 3;

    public TierRecipe(ResourceLocation idIn, Tier tier, Ingredient subItems, ICondition.IContext context) {
        this(idIn, tier, subItems, getTankSet(tier, context));
    }

    public TierRecipe(ResourceLocation idIn, Tier tier, Ingredient subItems, Set<BlockTank> normalTankSet) {
        this.id = idIn;
        this.tier = tier;
        this.subItems = subItems;

        result = OptionConverters.toJava(ModObjects.tierToBlock().get(tier)).map(ItemStack::new).orElse(ItemStack.EMPTY);
        this.normalTankSet = normalTankSet;
        LOGGER.debug("{} instance({}) created for Tier {}({}).", getClass().getSimpleName(), idIn, tier, result);
    }

    static Set<BlockTank> getTankSet(Tier tier, ICondition.IContext context) {
        Set<Tier> tierSet = Arrays.stream(Tier.values()).filter(t -> t.rank() == tier.rank() - 1).collect(Collectors.toSet());
        return CollectionConverters.asJava(ModObjects.blockTanks()).stream().filter(b -> tierSet.contains(b.tier()))
            .filter(blockTank -> Config.content().usableUnavailableTankInRecipe().get() || blockTank.tier().hasWayToCreate(context))
            .collect(Collectors.toSet());
    }

    @Override
    public boolean matches(CraftingContainer inv, @Nullable Level worldIn) {
        return checkInv(inv);
    }

    private boolean checkInv(CraftingContainer inv) {
        for (int i = 0; i <= inv.getWidth() - recipeWidth; ++i) {
            for (int j = 0; j <= inv.getHeight() - recipeHeight; ++j) {
                if (this.checkMatch(inv, i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     * <p>Copied from {@link net.minecraft.world.item.crafting.ShapedRecipe}</p>
     */
    public boolean checkMatch(CraftingContainer craftingInventory, int w, int h) {
        NonNullList<Ingredient> ingredients = this.getIngredients();
        for (int i = 0; i < craftingInventory.getWidth(); ++i) {
            for (int j = 0; j < craftingInventory.getHeight(); ++j) {
                int k = i - w;
                int l = j - h;
                Ingredient ingredient;
                if (k >= 0 && l >= 0 && k < recipeWidth && l < recipeHeight) {
                    ingredient = ingredients.get(recipeWidth - k - 1 + l * recipeWidth);
                } else {
                    ingredient = Ingredient.EMPTY;
                }

                if (!ingredient.test(craftingInventory.getItem(i + j * craftingInventory.getWidth()))) {
                    return false;
                }
            }
        }

        // Items are placed correctly.
        List<ItemStack> tankStacks = IntStream.range(0, craftingInventory.getContainerSize())
            .mapToObj(craftingInventory::getItem)
            .filter(this.getTankItems())
            .toList();
        return tankStacks.size() == 4 &&
            tankStacks.stream().map(BlockItem::getBlockEntityData)
                .filter(Objects::nonNull)
                .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TileTank.NBT_Tank())))
                .filter(FluidAmount::nonEmpty)
                .map(FluidKey::from)
                .distinct()
                .count() <= 1;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        if (!this.checkInv(inv)) {
            LOGGER.error("Requested to return crafting result for invalid inventory. {}",
                IntStream.range(0, inv.getContainerSize()).mapToObj(inv::getItem).collect(Collectors.toList()));
            return ItemStack.EMPTY;
        }
        ItemStack result = getResultItem();
        FluidAmount fluidAmount = IntStream.range(0, inv.getContainerSize()).mapToObj(inv::getItem)
            .filter(s -> s.getItem() instanceof ItemBlockTank)
            .map(BlockItem::getBlockEntityData)
            .filter(Objects::nonNull)
            .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TileTank.NBT_Tank())))
            .filter(FluidAmount::nonEmpty)
            .reduce(FluidAmount::$plus).orElse(FluidAmount.EMPTY());

        if (fluidAmount.nonEmpty()) {
            CompoundTag compound = new CompoundTag();

            CompoundTag tankTag = new CompoundTag();
            tankTag.putLong(TileTank.NBT_Capacity(), tier.amount());
            fluidAmount.write(tankTag);

            compound.put(TileTank.NBT_Tank(), tankTag);
            compound.put(TileTank.NBT_Tier(), tier.toNBTTag());

            Utils.setTileTag(result, compound);
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public ItemStack getResultItem() {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return allSlot().sorted(Comparator.comparing(Pair::getLeft)).map(Pair::getRight).collect(Collectors.toCollection(NonNullList::create));
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack item = inv.getItem(i);
            if (item.hasContainerItem() && !(item.getItem() instanceof ItemBlockTank)) {
                stacks.set(i, item.getContainerItem());
            }
        }

        return stacks;
    }

    public Ingredient getTankItems() {
        Stream<BlockTank> tankStream = this.normalTankSet.stream();
        return Ingredient.of(tankStream.map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public Ingredient getSubItems() {
        return subItems;
    }

    public Tier getTier() {
        return tier;
    }

    public Stream<Pair<Integer, Ingredient>> tankItemWithSlot() {
        return IntStream.of(TANK_SLOTS).mapToObj(value -> Pair.of(value, getTankItems()));
    }

    public Stream<Pair<Integer, Ingredient>> subItemWithSlot() {
        return IntStream.of(SUB_SLOTS).mapToObj(value -> Pair.of(value, getSubItems()));
    }

    public Stream<Pair<Integer, Ingredient>> allSlot() {
        return Stream.concat(Stream.of(Pair.of(4, Ingredient.EMPTY)), Stream.concat(tankItemWithSlot(), subItemWithSlot()));
    }

    @Override
    public int getRecipeWidth() {
        return 3;
    }

    @Override
    public int getRecipeHeight() {
        return 3;
    }

    public static final String KEY_TIER = "tier";
    public static final String KEY_SUB_ITEM = "sub_item";

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<TierRecipe> {
        public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "crafting_grade_up");

        public Serializer() {
            setRegistryName(LOCATION);
        }

        @Override
        public TierRecipe fromJson(ResourceLocation recipeId, JsonObject json, ICondition.IContext context) {
            Tier tier = Tier.byName(GsonHelper.getAsString(json, KEY_TIER)).orElse(Tier.Invalid);
            Ingredient subItem = Ingredient.fromJson(json.get(KEY_SUB_ITEM));
            if (subItem == Ingredient.EMPTY)
                LOGGER.warn("Empty ingredient was loaded for {}, data: {}", recipeId, json);
            LOGGER.debug("Serializer loaded {} from json for tier {}, sub {}.", recipeId, tier, Utils.convertIngredientToString(subItem));
            return new TierRecipe(recipeId, tier, subItem, context);
        }

        @Override
        @Deprecated
        public TierRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            LOGGER.warn("Trying to load recipe({}) without ICondition.IContext, which should not happen.", recipeId);
            return fromJson(recipeId, json, ICondition.IContext.EMPTY);
        }

        @Override
        public TierRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String tierName = buffer.readUtf();
            Tier tier = Tier.byName(tierName).orElseThrow(IllegalArgumentException::new);
            Ingredient subItem = Ingredient.fromNetwork(buffer);
            if (subItem == Ingredient.EMPTY)
                LOGGER.warn("Empty ingredient was loaded for {}", recipeId);
            LOGGER.debug("Serializer loaded {} from packet for tier {}, sub {}.", recipeId, tier, Utils.convertIngredientToString(subItem));
            var ingredientTanks = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readResourceLocation)
                .stream()
                .map(ForgeRegistries.BLOCKS::getValue)
                .mapMulti(Utils.cast(BlockTank.class))
                .collect(Collectors.toSet());
            return new TierRecipe(recipeId, tier, subItem, ingredientTanks);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TierRecipe recipe) {
            buffer.writeUtf(recipe.getTier().toString());
            recipe.getSubItems().toNetwork(buffer);
            var ingredientTanks = recipe.normalTankSet.stream().map(BlockTank::getRegistryName).toList();
            buffer.writeCollection(ingredientTanks, FriendlyByteBuf::writeResourceLocation);
            LOGGER.debug("Serialized {} to packet for tier {}.", recipe.id, recipe.tier);
        }

    }

    public static class TierFinishedRecipe implements FinishedRecipe {
        private final ResourceLocation recipeId;
        private final Tier tier;
        private final Ingredient ingredient;

        public TierFinishedRecipe(ResourceLocation recipeId, Tier tier) {
            this.recipeId = recipeId;
            this.tier = tier;
            this.ingredient = Ingredient.of(ItemTags.create(new ResourceLocation(this.tier.tagName())));
        }

        public TierFinishedRecipe(ResourceLocation recipeId, Tier tier, Ingredient ingredient) {
            this.recipeId = recipeId;
            this.tier = tier;
            this.ingredient = ingredient;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty(KEY_TIER, this.tier.lowerName());
            json.add(KEY_SUB_ITEM, ingredient.toJson());
        }

        @Override
        public ResourceLocation getId() {
            return recipeId;
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
