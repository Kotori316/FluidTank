package com.kotori316.fluidtank.recipes;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeHidden;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import scala.collection.JavaConverters;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.tiles.Tiers;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

public class TierRecipe extends IRecipeHidden {
    public static final Serializer SERIALIZER = new Serializer();
    private final Tiers tier;
    private final Ingredient tankItems;
    private final Ingredient subItems;
    private final ItemStack result;

    public TierRecipe(ResourceLocation idIn, Tiers tier) {
        super(idIn);
        this.tier = tier;

        result = JavaConverters.seqAsJavaList(ModObjects.blockTanks()).stream().filter(b -> b.tier() == tier).findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
        Set<Tiers> tiersSet = Tiers.jList().stream().filter(t -> t.rank() == tier.rank() - 1).collect(Collectors.toSet());
        Set<BlockTank> tanks = JavaConverters.seqAsJavaList(ModObjects.blockTanks()).stream().filter(b -> tiersSet.contains(b.tier())).collect(Collectors.toSet());
        Set<BlockTank> invTanks = JavaConverters.seqAsJavaList(ModObjects.blockTanksInvisible()).stream().filter(b -> tiersSet.contains(b.tier())).collect(Collectors.toSet());
        tankItems = Ingredient.merge(Stream.concat(tanks.stream(), invTanks.stream()).map(ItemStack::new).map(Ingredient::fromStacks).collect(Collectors.toList()));
        subItems = Optional.ofNullable(ItemTags.getCollection().get(new ResourceLocation(Config.content().oreNameMap().apply(tier))))
            .map(Ingredient::fromTag)
            .orElse(Ingredient.EMPTY);
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        if (!IntStream.of(1, 3, 5, 7).mapToObj(inv::getStackInSlot).allMatch(subItems)) return false;
        if (!IntStream.of(0, 2, 6, 8).mapToObj(inv::getStackInSlot).allMatch(tankItems))
            return false;
        return IntStream.of(0, 2, 6, 8).mapToObj(inv::getStackInSlot)
            .map(stack -> stack.getChildTag(TileTankNoDisplay.NBT_BlockTag()))
            .filter(Objects::nonNull)
            .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TileTankNoDisplay.NBT_Tank())))
            .filter(FluidAmount::nonEmpty)
            .map(FluidAmount::fluid)
            .distinct()
            .count() <= 1;
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        ItemStack result = getRecipeOutput();
        FluidAmount fluidAmount = IntStream.of(0, 2, 6, 8).mapToObj(inv::getStackInSlot)
            .map(stack -> stack.getChildTag(TileTankNoDisplay.NBT_BlockTag()))
            .filter(Objects::nonNull)
            .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TileTankNoDisplay.NBT_Tank())))
            .filter(FluidAmount::nonEmpty)
            .reduce(FluidAmount::$plus).orElse(FluidAmount.EMPTY());

        if (fluidAmount.nonEmpty()) {
            NBTTagCompound compound = new NBTTagCompound();

            NBTTagCompound tankTag = new NBTTagCompound();
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

    public Ingredient getTankItems() {
        return tankItems;
    }

    public Ingredient getSubItems() {
        return subItems;
    }

    public static class Serializer implements IRecipeSerializer<TierRecipe> {
        public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "crafting_grade_up");

        @Override
        public TierRecipe read(ResourceLocation recipeId, JsonObject json) {
            String t = JsonUtils.getString(json, "tier");
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

        @Override
        public ResourceLocation getName() {
            return LOCATION;
        }
    }
}
