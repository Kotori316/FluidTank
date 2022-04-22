package com.kotori316.fluidtank.recipe;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.tank.TankBlock;
import com.kotori316.fluidtank.tank.Tiers;

public class TierRecipe extends ShapedRecipe {
    public static final RecipeSerializer<?> SERIALIZER = new TierRecipe.Serializer();
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
        super(idIn, "", 3, 3, NonNullList.create(), ItemStack.EMPTY);
        id = idIn;
        this.tier = tier;

        if (RecipeConfigCondition.isUpdateRecipeEnabled()) {
            result = ModTank.Entries.ALL_TANK_BLOCKS.stream().filter(b -> b.tiers == tier).findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
            tankItems = Logic.getTankItemIngredient(tier);
            subItems = Logic.getSubItems(tier);
            isEmptyRecipe = subItems.isEmpty();
            logic = new Logic(tier, tankItems, subItems, result);
            if (isEmptyRecipe) {
                ModTank.LOGGER.warn("Sub item in recipe({}) is empty.", tier);
            }
        } else {
            result = ItemStack.EMPTY;
            tankItems = subItems = Ingredient.EMPTY;
            isEmptyRecipe = true;
            logic = EmptyLogic.INSTANCE;
        }
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

    public FinishedRecipe getFinishedRecipe() {
        return new TierFinishedRecipe();
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
                .map(BlockItem::getBlockEntityData)
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
                .map(BlockItem::getBlockEntityData)
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

                result.addTagElement("BlockEntityTag", compound);
            }

            return result;
        }

        static Ingredient getTankItemIngredient(Tiers resultTier) {
            Set<Tiers> tiersSet = Tiers.stream().filter(t -> t.rank == resultTier.rank - 1).collect(Collectors.toSet());
            Set<TankBlock> tanks = ModTank.Entries.ALL_TANK_BLOCKS.stream().filter(b -> tiersSet.contains(b.tiers)).collect(Collectors.toSet());
            return Ingredient.of(tanks.stream().map(ItemStack::new)); // OfStacks
        }

        private static Ingredient getSubItems(Tiers tier) {
            var tag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(tier.tagName));
            return Ingredient.of(tag);
        }

    }

    static class EmptyLogic extends Logic {
        static final EmptyLogic INSTANCE = new EmptyLogic();

        private EmptyLogic() {
            super(Tiers.Invalid, Ingredient.EMPTY, Ingredient.EMPTY, ItemStack.EMPTY);
        }

        @Override
        boolean matches(CraftingContainer inv) {
            return false;
        }

        @Override
        ItemStack craft(CraftingContainer inv) {
            return ItemStack.EMPTY;
        }
    }

    private class TierFinishedRecipe implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject jsonObject) {
            jsonObject.addProperty("tier", tier.toString());
        }

        @Override
        public ResourceLocation getId() {
            return TierRecipe.this.getId();
        }

        @Override
        public RecipeSerializer<?> getType() {
            return getSerializer();
        }

        @Override
        public JsonObject serializeAdvancement() {
            var advancement = Advancement.Builder.advancement();
            var inventoryCriterion = tier.getAlternative().isEmpty()
                ? RecipeProvider.has(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(tier.tagName)))
                : RecipeProvider.has(tier.getAlternative().getItems()[0].getItem());
            advancement.parent(new ResourceLocation("recipes/root"))
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(getId()))
                .addCriterion("has_" + tier.toString().toLowerCase(Locale.ROOT), inventoryCriterion)
                .rewards(AdvancementRewards.Builder.recipe(getId()))
                .requirements(RequirementsStrategy.OR);
            return advancement.serializeToJson();
        }

        @Override
        public ResourceLocation getAdvancementId() {
            String recipeFolderName = Optional.ofNullable(result.getItem().getItemCategory()).map(CreativeModeTab::getRecipeFolderName).orElseThrow(
                () -> new IllegalStateException("Item doesn't have Creative Tab. %s (Class: %s)".formatted(result.getItem(), result.getItem().getClass()))
            );
            return new ResourceLocation(getId().getNamespace(), "recipes/" + recipeFolderName + "/" + getId().getPath());
        }
    }
}
