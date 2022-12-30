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
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import scala.jdk.javaapi.StreamConverters;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FluidAction;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.FluidKey;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.items.TankItemFluidHandler;

public class CombineRecipe extends CustomRecipe {
    private static final Logger LOGGER = Utils.getLogger(CombineRecipe.class);
    public static final RecipeSerializer<CombineRecipe> SERIALIZER = new Serializer();
    public static final String LOCATION = "fluidtank:combine_tanks";
    private final Ingredient tanks;

    public CombineRecipe(ResourceLocation location, Ingredient tanks) {
        super(location, CraftingBookCategory.MISC);
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
        List<FluidAmount> fluids = IntStream.range(0, inv.getContainerSize())
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
        long totalAmount = fluids.stream().mapToLong(FluidAmount::amount).sum();
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
        Optional<FluidAmount> fluid = IntStream.range(0, inv.getContainerSize())
            .mapToObj(inv::getItem)
            .map(s -> getHandler(s)
                .map(TankItemFluidHandler::getFluid)
                .orElse(FluidAmount.EMPTY()))
            .filter(FluidAmount::nonEmpty)
            .reduce(FluidAmount::$plus);
        Optional<ItemStack> tank = getMaxCapacityTank(inv)
            .flatMap(p -> getHandler(copyStackWithSize(p.getLeft(), 1))
                .flatMap(h ->
                    fluid.map(f -> {
                        h.drain(h.getFluidInTank(0), FluidAction.EXECUTE);
                        h.fill(f, FluidAction.EXECUTE);
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
                ItemStack leave = getHandler(copyStackWithSize(item, 1))
                    .map(h -> {
                        h.drain(h.getFluidInTank(0), FluidAction.EXECUTE);
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

    private static ItemStack copyStackWithSize(ItemStack stack, int size) {
        var copied = stack.copy();
        copied.setCount(size);
        return copied;
    }

    private static final class Serializer implements RecipeSerializer<CombineRecipe> {
        @Override
        public CombineRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            var tanks = tankList();
            return new CombineRecipe(pRecipeId, tanks);
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

        private static Ingredient tankList() {
            Stream<BlockTank> tankStream = StreamConverters.asJavaSeqStream(ModObjects.blockTanks());
            Predicate<BlockTank> filter = t -> t.tier().isNormalTier();
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
