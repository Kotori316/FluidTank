package com.kotori316.fluidtank.recipe;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.tank.TankBlock;
import com.kotori316.fluidtank.tank.Tiers;

public class TierRecipe extends ShapedRecipe {
    public static final com.kotori316.fluidtank.recipe.TierRecipe.Serializer SERIALIZER = new com.kotori316.fluidtank.recipe.TierRecipe.Serializer();
    public static final String GROUP = ModTank.modID + ":tank_recipes";
    private static final int[] TANK_SLOTS = {0, 2, 6, 8};
    private static final int[] SUB_SLOTS = {1, 3, 5, 7};
    private final ResourceLocation id;
    private final Tiers tier;
    private final Ingredient tankItems;
    private final Ingredient subItems;
    private final ItemStack result;
    private final boolean isEmptyRecipe;
    private final Logic logic;

    public TierRecipe(ResourceLocation idIn, Tiers tier) {
        super(idIn, GROUP, 3, 3, NonNullList.create(), ItemStack.EMPTY);
        id = idIn;
        this.tier = tier;

        result = ModTank.Entries.ALL_TANK_BLOCKS.stream().filter(b -> b.tiers == tier).findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
        tankItems = Logic.getTankItemIngredient(tier);
        subItems = Logic.getSubItems(tier);
        isEmptyRecipe = subItems.isEmpty();
        logic = new Logic(tier, tankItems, subItems, result);
//        if (isEmptyRecipe)
//            throw new IllegalArgumentException(String.format("Mod 'FluidTank' Recipe for %s is disabled.", tier));
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        if (isEmptyRecipe) return false;
        return logic.matches(inv);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        return logic.craft(inv);
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

    public Ingredient getTankItems() {
        return tankItems;
    }

    public Ingredient getSubItems() {
        return subItems;
    }

    @Override
    public boolean isSpecial() {
        return isEmptyRecipe;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return allSlot().sorted(Comparator.comparing(Pair::getLeft)).map(Pair::getRight).collect(Collectors.toCollection(NonNullList::create));
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
        public static final ResourceLocation LOCATION = new ResourceLocation(ModTank.modID, "crafting_grade_up");

        @Override
        public TierRecipe fromJson(ResourceLocation id, JsonObject json) {
            String t = GsonHelper.getAsString(json, "tier");
            Tiers tiers = Tiers.stream()
                .filter(tier -> tier.toString().equalsIgnoreCase(t))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid tier: %s", t)));
            return new TierRecipe(id, tiers);
        }

        @Override
        public TierRecipe fromNetwork(ResourceLocation _id, FriendlyByteBuf buffer) {
            ResourceLocation id = buffer.readResourceLocation();
            Tiers tier = Tiers.fromNBT(buffer.readNbt());
            return new TierRecipe(id, tier);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TierRecipe recipe) {
            buffer.writeResourceLocation(recipe.getId());
            buffer.writeNbt(recipe.tier.toNBTTag());
        }
    }

    static class Logic {
        private final Tiers tier;
        private final Ingredient tankItems;
        private final Ingredient subItems;
        private final ItemStack result;

        Logic(Tiers tier, Ingredient tankItems, Ingredient subItems, ItemStack result) {
            this.tier = tier;
            this.tankItems = tankItems;
            this.subItems = subItems;
            this.result = result;
        }

        public Logic(Tiers tier) {
            this(
                tier,
                getTankItemIngredient(tier),
                getSubItems(tier),
                ModTank.Entries.ALL_TANK_BLOCKS.stream().filter(b -> b.tiers == tier).findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY)
            );
        }

        boolean matches(CraftingContainer inv) {
            if (!inv.getItem(4).isEmpty()) return false;
            if (!IntStream.of(SUB_SLOTS).mapToObj(inv::getItem).allMatch(subItems)) return false;
            if (!IntStream.of(TANK_SLOTS).mapToObj(inv::getItem).allMatch(tankItems)) return false;
            return IntStream.of(TANK_SLOTS).mapToObj(inv::getItem)
                .map(stack -> stack.getTagElement(TankBlock.NBT_BlockTag))
                .filter(Objects::nonNull)
                .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TankBlock.NBT_Tank)))
                .filter(FluidAmount::nonEmpty)
                .map(f -> f.fluidVolume().getFluidKey())
                .distinct()
                .count() <= 1;
        }

        ItemStack craft(CraftingContainer inv) {
            ItemStack result = this.result.copy();
            FluidAmount fluidAmount = IntStream.of(TANK_SLOTS).mapToObj(inv::getItem)
                .map(stack -> stack.getTagElement(TankBlock.NBT_BlockTag))
                .filter(Objects::nonNull)
                .map(nbt -> FluidAmount.fromNBT(nbt.getCompound(TankBlock.NBT_Tank)))
                .filter(FluidAmount::nonEmpty)
                .reduce(FluidAmount::$plus).orElse(FluidAmount.EMPTY());

            if (fluidAmount.nonEmpty()) {
                CompoundTag compound = new CompoundTag();

                CompoundTag tankTag = new CompoundTag();
                tankTag.putInt(TankBlock.NBT_Capacity, com.kotori316.fluidtank.Utils.toInt(tier.amount()));
                fluidAmount.write(tankTag);

                compound.put(TankBlock.NBT_Tank, tankTag);
                compound.put(TankBlock.NBT_Tier, tier.toNBTTag());

                result.addTagElement(TankBlock.NBT_BlockTag, compound);
            }

            return result;
        }

        static Ingredient getTankItemIngredient(Tiers resultTier) {
            Set<Tiers> tiersSet = Tiers.stream().filter(t -> t.rank == resultTier.rank - 1).collect(Collectors.toSet());
            Set<TankBlock> tanks = ModTank.Entries.ALL_TANK_BLOCKS.stream().filter(b -> tiersSet.contains(b.tiers)).collect(Collectors.toSet());
            return Ingredient.of(tanks.stream().map(ItemStack::new)); // OfStacks
        }

        private static Ingredient getSubItems(Tiers tier) {
            return Optional.ofNullable(SerializationTags.getInstance().getOrEmpty(Registry.ITEM_REGISTRY).getTag(new ResourceLocation(tier.tagName)))
                .map(Ingredient::of)
                .orElse(tier.getAlternative());
        }

    }
}
