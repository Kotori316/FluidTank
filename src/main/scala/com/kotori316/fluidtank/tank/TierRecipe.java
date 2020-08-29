package com.kotori316.fluidtank.tank;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.Utils;

public class TierRecipe implements CraftingRecipe {
    public static final Serializer SERIALIZER = new Serializer();
    private static final int[] TANK_SLOTS = {0, 2, 6, 8};
    private static final int[] SUB_SLOTS = {1, 3, 5, 7};
    private final Identifier id;
    private final Tiers tier;
    private final Ingredient tankItems;
    private final Ingredient subItems;
    private final ItemStack result;
    private final boolean isEmptyRecipe;

    public TierRecipe(Identifier idIn, Tiers tier) {
        id = idIn;
        this.tier = tier;

        result = ModTank.Entries.ALL_TANK_BLOCKS.stream().filter(b -> b.tiers == tier).findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
        Set<Tiers> tiersSet = Tiers.stream().filter(t -> t.rank == tier.rank - 1).collect(Collectors.toSet());
        Set<TankBlock> tanks = ModTank.Entries.ALL_TANK_BLOCKS.stream().filter(b -> tiersSet.contains(b.tiers)).collect(Collectors.toSet());
        Set<TankBlock> invTanks = ModTank.Entries.ALL_TANK_BLOCKS.stream().filter(b -> tiersSet.contains(b.tiers)).collect(Collectors.toSet());
        tankItems = Ingredient.ofStacks(Stream.concat(tanks.stream(), invTanks.stream()).map(ItemStack::new).toArray(ItemStack[]::new));
        subItems = Optional.ofNullable(ItemTags.getTagGroup().getTag(new Identifier(tier.tagName)))
            .map(Ingredient::fromTag)
            .orElse(tier.getAlternative());
        isEmptyRecipe = subItems.isEmpty();
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        if (isEmptyRecipe) return false;
        if (!IntStream.of(SUB_SLOTS).mapToObj(inv::getStack).allMatch(subItems)) return false;
        if (!IntStream.of(TANK_SLOTS).mapToObj(inv::getStack).allMatch(tankItems)) return false;
        return IntStream.of(TANK_SLOTS).mapToObj(inv::getStack)
            .map(stack -> stack.getSubTag(TankBlock.NBT_BlockTag))
            .filter(Objects::nonNull)
            .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TankBlock.NBT_Tank)))
            .filter(FluidAmount::nonEmpty)
            .map(FluidAmount::fluid)
            .distinct()
            .count() <= 1;
    }

    @Override
    public ItemStack craft(CraftingInventory inv) {
        ItemStack result = getOutput();
        FluidAmount fluidAmount = IntStream.of(TANK_SLOTS).mapToObj(inv::getStack)
            .map(stack -> stack.getSubTag(TankBlock.NBT_BlockTag))
            .filter(Objects::nonNull)
            .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TankBlock.NBT_Tank)))
            .filter(FluidAmount::nonEmpty)
            .reduce(FluidAmount::$plus).orElse(FluidAmount.EMPTY());

        if (fluidAmount.nonEmpty()) {
            CompoundTag compound = new CompoundTag();

            CompoundTag tankTag = new CompoundTag();
            tankTag.putInt(TankBlock.NBT_Capacity, Utils.toInt(tier.amount()));
            fluidAmount.write(tankTag);

            compound.put(TankBlock.NBT_Tank, tankTag);
            compound.put(TankBlock.NBT_Tier, tier.toNBTTag());

            result.putSubTag(TankBlock.NBT_BlockTag, compound);
        }

        return result;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public ItemStack getOutput() {
        return result.copy();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    public Ingredient getTankItems() {
        return tankItems;
    }

    public Ingredient getSubItems() {
        return subItems;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return isEmptyRecipe;
    }

    @Override
    public DefaultedList<Ingredient> getPreviewInputs() {
        return allSlot().sorted(Comparator.comparing(Pair::getLeft)).map(Pair::getRight).collect(Collectors.toCollection(DefaultedList::of));
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

    public static class Serializer implements RecipeSerializer<TierRecipe> {
        public static final Identifier LOCATION = new Identifier(ModTank.modID, "crafting_grade_up");

        @Override
        public TierRecipe read(Identifier id, JsonObject json) {
            String t = JsonHelper.getString(json, "tier");
            Tiers tiers = Tiers.stream()
                .filter(tier -> tier.toString().equalsIgnoreCase(t))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid tier: %s", t)));
            return new TierRecipe(id, tiers);
        }

        @Override
        public TierRecipe read(Identifier _id, PacketByteBuf buffer) {
            Identifier id = buffer.readIdentifier();
            Tiers tier = Tiers.fromNBT(buffer.readCompoundTag());
            return new TierRecipe(id, tier);
        }

        @Override
        public void write(PacketByteBuf buffer, TierRecipe recipe) {
            buffer.writeIdentifier(recipe.getId());
            buffer.writeCompoundTag(recipe.tier.toNBTTag());
        }
    }

}
