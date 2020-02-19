package com.kotori316.fluidtank.recipes;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.tiles.Tiers;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

public class TierRecipe implements ICraftingRecipe, IShapedRecipe<CraftingInventory> {
    private static final Logger LOGGER = LogManager.getLogger(TierRecipe.class);
    public static final Serializer SERIALIZER = new Serializer();
    private static final int[] TANK_SLOTS = {0, 2, 6, 8};
    private static final int[] SUB_SLOTS = {1, 3, 5, 7};
    private final ResourceLocation id;
    private final Tiers tier;
    private final Ingredient tankItems;
    private final Ingredient subItems;
    private final ItemStack result;
    private static final int recipeWidth = 3;
    private static final int recipeHeight = 3;
    private final List<Ingredient> recipeItems;

    public TierRecipe(ResourceLocation idIn, Tiers tier, Ingredient subItems) {
        id = idIn;
        this.tier = tier;
        this.subItems = subItems;

        result = CollectionConverters.asJava(ModObjects.blockTanks()).stream().filter(b -> b.tier() == tier).findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
        Set<Tiers> tiersSet = Tiers.jList().stream().filter(t -> t.rank() == tier.rank() - 1).collect(Collectors.toSet());
        Set<BlockTank> tanks = CollectionConverters.asJava(ModObjects.blockTanks()).stream().filter(b -> tiersSet.contains(b.tier())).collect(Collectors.toSet());
        Set<BlockTank> invTanks = CollectionConverters.asJava(ModObjects.blockTanksInvisible()).stream().filter(b -> tiersSet.contains(b.tier())).collect(Collectors.toSet());
        tankItems = Ingredient.fromStacks(Stream.concat(tanks.stream(), invTanks.stream()).map(ItemStack::new).toArray(ItemStack[]::new));
        LOGGER.debug("Recipe instance({}) created for Tier {}.", idIn, tier);
        recipeItems = getIngredients();
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
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
        for (int i = 0; i < craftingInventory.getWidth(); ++i) {
            for (int j = 0; j < craftingInventory.getHeight(); ++j) {
                int k = i - w;
                int l = j - h;
                Ingredient ingredient;
                if (k >= 0 && l >= 0 && k < recipeWidth && l < recipeHeight) {
                    ingredient = this.recipeItems.get(recipeWidth - k - 1 + l * recipeWidth);
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
            tankStacks.stream().map(stack -> stack.getChildTag(TileTankNoDisplay.NBT_BlockTag()))
                .filter(Objects::nonNull)
                .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TileTankNoDisplay.NBT_Tank())))
                .filter(FluidAmount::nonEmpty)
                .map(FluidAmount::fluid)
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
            .map(stack -> stack.getChildTag(TileTankNoDisplay.NBT_BlockTag()))
            .filter(Objects::nonNull)
            .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TileTankNoDisplay.NBT_Tank())))
            .filter(FluidAmount::nonEmpty)
            .reduce(FluidAmount::$plus).orElse(FluidAmount.EMPTY());

        if (fluidAmount.nonEmpty()) {
            CompoundNBT compound = new CompoundNBT();

            CompoundNBT tankTag = new CompoundNBT();
            tankTag.putInt(TileTankNoDisplay.NBT_Capacity(), Utils.toInt(tier.amount()));
            fluidAmount.write(tankTag);

            compound.put(TileTankNoDisplay.NBT_Tank(), tankTag);
            compound.put(TileTankNoDisplay.NBT_Tier(), tier.toNBTTag());

            result.setTagInfo(TileTankNoDisplay.NBT_BlockTag(), compound);
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

    public Ingredient getTankItems() {
        return tankItems;
    }

    public Ingredient getSubItems() {
        return subItems;
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

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<TierRecipe> {
        public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "crafting_grade_up");

        public Serializer() {
            setRegistryName(LOCATION);
        }

        @Override
        public TierRecipe read(ResourceLocation recipeId, JsonObject json) {
            String t = JSONUtils.getString(json, "tier");
            Tiers tiers = Tiers.jList().stream()
                .filter(tier -> tier.toString().equalsIgnoreCase(t))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid tier: %s", t)));
            Ingredient subItem = Optional.ofNullable(json.get("sub_item"))
                .map(Ingredient::deserialize)
                .orElse(Ingredient.EMPTY);
            if (subItem == Ingredient.EMPTY)
                LOGGER.warn("Empty ingredient was loaded for {}, json: {}", recipeId, json);
            LOGGER.debug("Serializer loaded {} from json for tier {}.", recipeId, tiers);
            return new TierRecipe(recipeId, tiers, subItem);
        }

        @Override
        public TierRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            Tiers tier = Tiers.fromNBT(buffer.readCompoundTag());
            Ingredient subItem = Ingredient.read(buffer);
            if (subItem == Ingredient.EMPTY)
                LOGGER.warn("Empty ingredient was loaded for {}", recipeId);
            LOGGER.debug("Serializer loaded {} from packet for tier {}.", recipeId, tier);
            return new TierRecipe(recipeId, tier, subItem);
        }

        @Override
        public void write(PacketBuffer buffer, TierRecipe recipe) {
            buffer.writeCompoundTag(recipe.tier.toNBTTag());
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
            json.addProperty("tier", tiers.toString().toLowerCase());
            if (tiers.hasTagRecipe()) {
                Ingredient ingredient = Ingredient.fromTag(ItemTags.getCollection().getOrCreate(new ResourceLocation(tiers.tagName())));
                json.add("sub_item", ingredient.serialize());
            }
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
