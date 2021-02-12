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
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;
import scala.jdk.javaapi.StreamConverters;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.FluidKey;
import com.kotori316.fluidtank.items.TankItemFluidHandler;

public class CombineRecipe implements ICraftingRecipe {
    public static final SpecialRecipeSerializer<CombineRecipe> SERIALIZER = new SpecialRecipeSerializer<>(CombineRecipe::new);
    private final ResourceLocation location;

    public CombineRecipe(ResourceLocation location) {
        this.location = location;
    }

    @Override
    public boolean matches(CraftingInventory inv, @Nullable World worldIn) {
        // Check all items are tanks.
        Ingredient tanks = tankList();
        boolean allTanks = IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot)
            .filter(s -> !s.isEmpty())
            .allMatch(tanks.and(ItemStack::hasTag));
        if (!allTanks) return false;
        // Check all tanks have the same fluid.
        List<FluidAmount> fluids = IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot)
            .map(s -> s.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .map(f -> f.getFluidInTank(0)).orElse(FluidStack.EMPTY))
            .map(FluidAmount::fromStack).collect(Collectors.toList());
        boolean allSameFluid = fluids.stream()
            .map(FluidKey::from)
            .filter(FluidKey::isDefined)
            .distinct().count() == 1;
        if (!allSameFluid) return false;
        long totalAmount = fluids.stream().mapToLong(FluidAmount::amount).sum();
        return getMaxCapacityTank(inv).map(p -> p.getRight() >= totalAmount).orElse(false);
    }

    private Ingredient tankList() {
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
        return IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot)
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
                .map(f -> f.getFluidInTank(0)).orElse(FluidStack.EMPTY))
            .map(FluidAmount::fromStack)
            .filter(FluidAmount::nonEmpty)
            .reduce(FluidAmount::$plus);
        Optional<ItemStack> tank = getMaxCapacityTank(inv)
            .flatMap(p -> p.getLeft().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).resolve()
                .flatMap(h ->
                    fluid.map(f -> {
                        h.drain(h.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE);
                        h.fill(f.toStack(), IFluidHandler.FluidAction.EXECUTE);
                        return h.getContainer();
                    })));
        return tank.orElse(null);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        return null;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return this.location;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
