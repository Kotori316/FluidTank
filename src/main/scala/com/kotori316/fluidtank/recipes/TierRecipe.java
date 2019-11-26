package com.kotori316.fluidtank.recipes;

import java.util.Comparator;
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
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.tuple.Pair;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.tiles.Tiers;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

public class TierRecipe implements ICraftingRecipe {
    public static final Serializer SERIALIZER = new Serializer();
    private static final int[] TANK_SLOTS = {0, 2, 6, 8};
    private static final int[] SUB_SLOTS = {1, 3, 5, 7};
    private final ResourceLocation id;
    private final Tiers tier;
    private final Ingredient tankItems;
    private final Ingredient subItems;
    private final ItemStack result;

    public TierRecipe(ResourceLocation idIn, Tiers tier) {
        id = idIn;
        this.tier = tier;

        result = CollectionConverters.asJava(ModObjects.blockTanks()).stream().filter(b -> b.tier() == tier).findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
        Set<Tiers> tiersSet = Tiers.jList().stream().filter(t -> t.rank() == tier.rank() - 1).collect(Collectors.toSet());
        Set<BlockTank> tanks = CollectionConverters.asJava(ModObjects.blockTanks()).stream().filter(b -> tiersSet.contains(b.tier())).collect(Collectors.toSet());
        Set<BlockTank> invTanks = CollectionConverters.asJava(ModObjects.blockTanksInvisible()).stream().filter(b -> tiersSet.contains(b.tier())).collect(Collectors.toSet());
        tankItems = Ingredient.fromStacks(Stream.concat(tanks.stream(), invTanks.stream()).map(ItemStack::new).toArray(ItemStack[]::new));
        subItems = Optional.ofNullable(ItemTags.getCollection().get(new ResourceLocation(Config.content().tagMap().apply(tier))))
            .map(Ingredient::fromTag)
            .orElse(Ingredient.EMPTY);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        if (!IntStream.of(SUB_SLOTS).mapToObj(inv::getStackInSlot).allMatch(subItems)) return false;
        if (!IntStream.of(TANK_SLOTS).mapToObj(inv::getStackInSlot).allMatch(tankItems)) return false;
        return IntStream.of(TANK_SLOTS).mapToObj(inv::getStackInSlot)
            .map(stack -> stack.getChildTag(TileTankNoDisplay.NBT_BlockTag()))
            .filter(Objects::nonNull)
            .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TileTankNoDisplay.NBT_Tank())))
            .filter(FluidAmount::nonEmpty)
            .map(FluidAmount::fluid)
            .distinct()
            .count() <= 1;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack result = getRecipeOutput();
        FluidAmount fluidAmount = IntStream.of(TANK_SLOTS).mapToObj(inv::getStackInSlot)
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
            return new TierRecipe(recipeId, tiers);
        }

        @Override
        public TierRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            ResourceLocation id = buffer.readResourceLocation();
            Tiers tier = Tiers.fromNBT(buffer.readCompoundTag());
            return new TierRecipe(id, tier);
        }

        @Override
        public void write(PacketBuffer buffer, TierRecipe recipe) {
            buffer.writeResourceLocation(recipe.getId());
            buffer.writeCompoundTag(recipe.tier.toNBTTag());
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
