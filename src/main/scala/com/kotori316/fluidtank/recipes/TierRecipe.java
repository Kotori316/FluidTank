package com.kotori316.fluidtank.recipes;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.kotori316.fluidtank.tiles.Tiers;
import com.kotori316.fluidtank.tiles.TileTank;

public class TierRecipe implements ICraftingRecipe, IShapedRecipe<CraftingInventory> {
    private static final Logger LOGGER = LogManager.getLogger(TierRecipe.class);
    public static final Serializer SERIALIZER = new Serializer();
    private static final int[] TANK_SLOTS = {0, 2, 6, 8};
    private static final int[] SUB_SLOTS = {1, 3, 5, 7};
    private final ResourceLocation id;
    private final Tiers tier;
    private final Set<BlockTank> normalTankSet;
    private final Ingredient subItems;
    private final ItemStack result;
    private static final int recipeWidth = 3;
    private static final int recipeHeight = 3;

    public TierRecipe(ResourceLocation idIn, Tiers tier, Ingredient subItems) {
        id = idIn;
        this.tier = tier;
        this.subItems = subItems;

        result = CollectionConverters.asJava(ModObjects.blockTanks()).stream().filter(b -> b.tier() == tier).findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
        Set<Tiers> tiersSet = Tiers.jList().stream().filter(t -> t.rank() == tier.rank() - 1).collect(Collectors.toSet());
        normalTankSet = CollectionConverters.asJava(ModObjects.blockTanks()).stream().filter(b -> tiersSet.contains(b.tier()))
            .filter(TierRecipe::filterTier).collect(Collectors.toSet());
        LOGGER.debug("Recipe instance({}) created for Tier {}.", idIn, tier);
    }

    @Override
    public boolean matches(CraftingInventory inv, @Nullable World worldIn) {
        return checkInv(inv);
    }

    private boolean checkInv(CraftingInventory inv) {
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
     * <p>Copied from {@link net.minecraft.item.crafting.ShapedRecipe}</p>
     */
    public boolean checkMatch(CraftingInventory craftingInventory, int w, int h) {
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

                if (!ingredient.test(craftingInventory.getStackInSlot(i + j * craftingInventory.getWidth()))) {
                    return false;
                }
            }
        }

        // Items are placed correctly.
        List<ItemStack> tankStacks = IntStream.range(0, craftingInventory.getSizeInventory())
            .mapToObj(craftingInventory::getStackInSlot)
            .filter(this.getTankItems())
            .collect(Collectors.toList());
        return tankStacks.size() == 4 &&
            tankStacks.stream().map(stack -> stack.getChildTag(TileTank.NBT_BlockTag()))
                .filter(Objects::nonNull)
                .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TileTank.NBT_Tank())))
                .filter(FluidAmount::nonEmpty)
                .map(FluidKey::from)
                .distinct()
                .count() <= 1;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        if (!this.checkInv(inv)) {
            LOGGER.error("Requested to return crafting result for invalid inventory. {}",
                IntStream.range(0, inv.getSizeInventory()).mapToObj(inv::getStackInSlot).collect(Collectors.toList()));
            return ItemStack.EMPTY;
        }
        ItemStack result = getRecipeOutput();
        FluidAmount fluidAmount = IntStream.range(0, inv.getSizeInventory()).mapToObj(inv::getStackInSlot)
            .filter(s -> s.getItem() instanceof ItemBlockTank)
            .map(stack -> stack.getChildTag(TileTank.NBT_BlockTag()))
            .filter(Objects::nonNull)
            .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TileTank.NBT_Tank())))
            .filter(FluidAmount::nonEmpty)
            .reduce(FluidAmount::$plus).orElse(FluidAmount.EMPTY());

        if (fluidAmount.nonEmpty()) {
            CompoundNBT compound = new CompoundNBT();

            CompoundNBT tankTag = new CompoundNBT();
            tankTag.putInt(TileTank.NBT_Capacity(), Utils.toInt(tier.amount()));
            fluidAmount.write(tankTag);

            compound.put(TileTank.NBT_Tank(), tankTag);
            compound.put(TileTank.NBT_Tier(), tier.toNBTTag());

            result.setTagInfo(TileTank.NBT_BlockTag(), compound);
        }

        return result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public ItemStack getRecipeOutput() {
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
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack item = inv.getStackInSlot(i);
            if (item.hasContainerItem() && !(item.getItem() instanceof ItemBlockTank)) {
                stacks.set(i, item.getContainerItem());
            }
        }

        return stacks;
    }

    public Ingredient getTankItems() {
        Stream<BlockTank> tankStream = this.normalTankSet.stream();
        return Ingredient.fromStacks(tankStream.map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public Ingredient getSubItems() {
        return subItems;
    }

    public Tiers getTier() {
        return tier;
    }

    private static boolean filterTier(BlockTank blockTank) {
        if (Config.content().usableUnavailableTankInRecipe().get())
            return true;
        else
            return blockTank.tier().hasWayToCreate();
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

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<TierRecipe> {
        public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "crafting_grade_up");

        public Serializer() {
            setRegistryName(LOCATION);
        }

        @Override
        public TierRecipe read(ResourceLocation recipeId, JsonObject json) {
            Tiers tier = OptionConverters.toJava(Tiers.byName(JSONUtils.getString(json, KEY_TIER))).orElse(Tiers.Invalid());
            Ingredient subItem = Ingredient.deserialize(json.get(KEY_SUB_ITEM));
            if (subItem == Ingredient.EMPTY)
                LOGGER.warn("Empty ingredient was loaded for {}, data: {}", recipeId, json);
            LOGGER.debug("Serializer loaded {} from json for tier {}, sub {}.", recipeId, tier, subItem);
            return new TierRecipe(recipeId, tier, subItem);
        }

        @Override
        public TierRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            String tierName = buffer.readString();
            Tiers tier = Tiers.byName(tierName).get();
            Ingredient subItem = Ingredient.read(buffer);
            if (subItem == Ingredient.EMPTY)
                LOGGER.warn("Empty ingredient was loaded for {}", recipeId);
            LOGGER.debug("Serializer loaded {} from packet for tier {}, sub {}..", recipeId, tier, subItem);
            return new TierRecipe(recipeId, tier, subItem);
        }

        @Override
        public void write(PacketBuffer buffer, TierRecipe recipe) {
            buffer.writeString(recipe.getTier().toString());
            recipe.getSubItems().write(buffer);
            LOGGER.debug("Serialized {} to packet for tier {}.", recipe.id, recipe.tier);
        }

    }

    public static class FinishedRecipe implements IFinishedRecipe {
        private final ResourceLocation recipeId;
        private final Tiers tiers;

        public FinishedRecipe(ResourceLocation recipeId, Tiers tiers) {
            this.recipeId = recipeId;
            this.tiers = tiers;
        }

        @Override
        public void serialize(JsonObject json) {
            Ingredient ingredient = Ingredient.fromTag(ItemTags.makeWrapperTag(this.tiers.tagName()));

            json.addProperty(KEY_TIER, this.tiers.lowerName());
            json.add(KEY_SUB_ITEM, ingredient.serialize());
        }

        @Override
        public ResourceLocation getID() {
            return recipeId;
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
