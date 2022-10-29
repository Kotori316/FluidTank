package com.kotori316.fluidtank.recipes;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import scala.jdk.javaapi.StreamConverters;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.FluidKey;
import com.kotori316.fluidtank.fluids.GenericAmount;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.items.TankItemFluidHandler;

public class CombineRecipe extends CustomRecipe {
    private static final Logger LOGGER = Utils.getLogger(CombineRecipe.class);
    public static final RecipeSerializer<CombineRecipe> SERIALIZER = new Serializer();
    public static final String LOCATION = "fluidtank:combine_tanks";
    private final Ingredient tanks;

    public CombineRecipe(ResourceLocation location, Ingredient tanks) {
        super(location);
        this.tanks = tanks;
        LOGGER.debug("{} instance({}) created", getClass().getSimpleName(), location);
    }

    @Override
    public boolean matches(CraftingContainer inv, @Nullable Level worldIn) {
        // Check all items are tanks.
        boolean allTanks = IntStream.range(0, inv.getContainerSize())
            .mapToObj(inv::getItem).filter(s -> !s.isEmpty()).allMatch(tanks);
        if (!allTanks) return false;
        long tankCount = IntStream.range(0, inv.getContainerSize())
            .mapToObj(inv::getItem)
            .filter(s -> !s.isEmpty()).filter(tanks).count();
        if (tankCount < 2) return false;
        // Check all tanks have the same fluid.
        List<GenericAmount<Fluid>> fluids = IntStream.range(0, inv.getContainerSize())
            .mapToObj(inv::getItem)
            .map(s -> getHandler(s)
                .map(TankItemFluidHandler::getFluid)
                .orElse(FluidAmount.EMPTY()))
            .toList();
        boolean allSameFluid = fluids.stream()
                                   .map(FluidKey::from)
                                   .filter(FluidKey::isDefined)
                                   .distinct().count() == 1;
        if (!allSameFluid) return false;
        long totalAmount = fluids.stream().mapToLong(GenericAmount::amount).sum();
        return getMaxCapacityTank(inv).map(p -> p.getRight() >= totalAmount).orElse(false);
    }

    private static Optional<Pair<ItemStack, Long>> getMaxCapacityTank(CraftingContainer inv) {
        return getMaxCapacityTank(IntStream.range(0, inv.getContainerSize())
            .mapToObj(inv::getItem));
    }

    static Optional<Pair<ItemStack, Long>> getMaxCapacityTank(Stream<ItemStack> stream) {
        return stream
            .filter(s -> !s.isEmpty())
            .flatMap(s -> getHandler(s).stream())
            .map(h -> Pair.of(h.getContainer(), h.getCapacity()))
            .max(Comparator.comparing(Pair::getRight))
            .map(p -> Pair.of(p.getLeft().copy(), p.getRight()));
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        Optional<GenericAmount<Fluid>> fluid = IntStream.range(0, inv.getContainerSize())
            .mapToObj(inv::getItem)
            .map(s -> getHandler(s)
                .map(TankItemFluidHandler::getFluid)
                .orElse(FluidAmount.EMPTY()))
            .filter(GenericAmount::nonEmpty)
            .reduce(FluidAmount.monoidFA()::combine);
        Optional<ItemStack> tank = getMaxCapacityTank(inv)
            .flatMap(p -> getHandler(ItemHandlerHelper.copyStackWithSize(p.getLeft(), 1))
                .flatMap(h ->
                    fluid.map(f -> {
                        h.drain(h.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE);
                        h.fill(FluidAmount.toStack(f), IFluidHandler.FluidAction.EXECUTE);
                        return h.getContainer();
                    })));
        return tank.orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        Optional<ItemStack> stack = getMaxCapacityTank(inv).map(Pair::getLeft);

        NonNullList<ItemStack> nn = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < nn.size(); i++) {
            ItemStack item = inv.getItem(i);
            if (stack.filter(s -> s.sameItem(item)).isPresent()) {
                stack = Optional.empty();
            } else {
                ItemStack leave = getHandler(ItemHandlerHelper.copyStackWithSize(item, 1))
                    .map(h -> {
                        h.drain(h.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE);
                        return h.getContainer();
                    }).orElse(item);
                nn.set(i, leave);
            }
        }
        return nn;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static Optional<TankItemFluidHandler> getHandler(ItemStack stack) {
        if (stack.getItem() instanceof ItemBlockTank tankItem) {
            return Optional.of(new TankItemFluidHandler(tankItem.blockTank().tier(), stack));
        } else {
            return Optional.empty();
        }
    }

    private static final class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<CombineRecipe> {
        @Override
        public CombineRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            LOGGER.warn("Trying to load recipe({}) without ICondition.IContext, which should not happen.", pRecipeId);
            return fromJson(pRecipeId, pSerializedRecipe, ICondition.IContext.EMPTY);
        }

        @Override
        public CombineRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            var tanks = Ingredient.fromNetwork(pBuffer);
            return new CombineRecipe(pRecipeId, tanks);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, CombineRecipe recipe) {
            recipe.tanks.toNetwork(pBuffer);
        }

        @Override
        public CombineRecipe fromJson(ResourceLocation recipeLoc, JsonObject recipeJson, ICondition.IContext context) {
            var tanks = tankList(context);
            return new CombineRecipe(recipeLoc, tanks);
        }

        private static Ingredient tankList(ICondition.IContext context) {
            Stream<BlockTank> tankStream = StreamConverters.asJavaSeqStream(ModObjects.blockTanks());
            Predicate<BlockTank> filter = t -> t.tier().isNormalTier();
            if (!Config.content().usableUnavailableTankInRecipe().get()) {
                filter = filter.and(t -> t.tier().hasWayToCreate(context));
            }
            return Ingredient.of(tankStream.filter(filter).map(ItemStack::new));
        }
    }

    public record CombineFinishedRecipe(ResourceLocation recipeId) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject pJson) {

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
