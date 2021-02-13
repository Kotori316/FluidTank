package com.kotori316.fluidtank.recipes;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.jdk.javaapi.StreamConverters;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.FluidKey;
import com.kotori316.fluidtank.items.TankItemFluidHandler;

public class CombineRecipe extends SpecialRecipe {
    private static final Logger LOGGER = LogManager.getLogger(CombineRecipe.class);
    public static final SpecialRecipeSerializer<CombineRecipe> SERIALIZER = new SpecialRecipeSerializer<>(CombineRecipe::new);
    public static final String LOCATION = "fluidtank:combine_tanks";

    public CombineRecipe(ResourceLocation location) {
        super(location);
        LOGGER.debug("Recipe instance of ConvertInvisibleRecipe({}) was created.", location);
    }

    @Override
    public boolean matches(CraftingInventory inv, @Nullable World worldIn) {
        // Check all items are tanks.
        Ingredient tanks = tankList();
        boolean allTanks = IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot).filter(s -> !s.isEmpty()).allMatch(tanks);
        if (!allTanks) return false;
        long tankCount = IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot)
            .filter(s -> !s.isEmpty()).filter(tanks).count();
        if (tankCount < 2) return false;
        // Check all tanks have the same fluid.
        List<FluidAmount> fluids = IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot)
            .map(s -> s.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .filter(h -> h instanceof TankItemFluidHandler)
                .map(h -> ((TankItemFluidHandler) h).getFluid()).orElse(FluidAmount.EMPTY()))
            .collect(Collectors.toList());
        boolean allSameFluid = fluids.stream()
            .map(FluidKey::from)
            .filter(FluidKey::isDefined)
            .distinct().count() == 1;
        if (!allSameFluid) return false;
        long totalAmount = fluids.stream().mapToLong(FluidAmount::amount).sum();
        return getMaxCapacityTank(inv).map(p -> p.getRight() >= totalAmount).orElse(false);
    }

    private static Ingredient tankList() {
        Stream<BlockTank> tankStream;
        if (Config.content() != null && !Config.content().usableInvisibleInRecipe().get()) {
            // Normal tanks only
            tankStream = StreamConverters.asJavaSeqStream(ModObjects.blockTanks());
        } else {
            tankStream = Stream.of(ModObjects.blockTanks(), ModObjects.blockTanksInvisible())
                .flatMap(StreamConverters::asJavaSeqStream);
        }
        Predicate<BlockTank> filter = t -> t.tier().isNormalTier();
        if (Config.content() != null && !Config.content().usableUnavailableTankInRecipe().get()) {
            filter = filter.and(t -> t.tier().hasWayToCreate());
        }
        return Ingredient.fromStacks(tankStream.filter(filter).map(ItemStack::new));
    }

    private static Optional<Pair<ItemStack, Long>> getMaxCapacityTank(CraftingInventory inv) {
        return getMaxCapacityTank(IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot));
    }

    static Optional<Pair<ItemStack, Long>> getMaxCapacityTank(Stream<ItemStack> stream) {
        return stream
            .filter(s -> !s.isEmpty())
            .map(s -> s.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY))
            .flatMap(l -> l.filter(h -> h instanceof TankItemFluidHandler).map(Stream::of).orElse(Stream.empty()))
            .map(h -> Pair.of(h.getContainer(), ((TankItemFluidHandler) h).getCapacity()))
            .max(Comparator.comparing(Pair::getRight))
            .map(p -> Pair.of(p.getLeft().copy(), p.getRight()));
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        Optional<FluidAmount> fluid = IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot)
            .map(s -> s.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .filter(h -> h instanceof TankItemFluidHandler)
                .map(h -> ((TankItemFluidHandler) h).getFluid()).orElse(FluidAmount.EMPTY()))
            .filter(FluidAmount::nonEmpty)
            .reduce(FluidAmount::$plus);
        Optional<ItemStack> tank = getMaxCapacityTank(inv)
            .flatMap(p -> ItemHandlerHelper.copyStackWithSize(p.getLeft(), 1).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).resolve()
                .flatMap(h ->
                    fluid.map(f -> {
                        h.drain(h.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE);
                        h.fill(f.toStack(), IFluidHandler.FluidAction.EXECUTE);
                        return h.getContainer();
                    })));
        return tank.orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        Optional<ItemStack> stack = getMaxCapacityTank(inv).map(Pair::getLeft);

        NonNullList<ItemStack> nn = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < nn.size(); i++) {
            ItemStack item = inv.getStackInSlot(i);
            if (stack.filter(s -> s.isItemEqual(item)).isPresent()) {
                stack = Optional.empty();
            } else {
                ItemStack leave = ItemHandlerHelper.copyStackWithSize(item, 1).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
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
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

}
