package com.kotori316.fluidtank.fluids;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import scala.Option;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.blocks.BucketEventHandler;

@SuppressWarnings("UnstableApiUsage")
public final class VariantUtil {
    static long convertFabricAmountToForge(long fabricAmount) {
        return fabricAmount / FluidConstants.BUCKET * 1000L;
    }

    static long convertForgeAmountToFabric(long forgeAmount) {
        return forgeAmount * FluidConstants.BUCKET / 1000L;
    }

    public static FluidVariant convert(FluidAmount amount) {
        return FluidVariant.of(amount.fluid(), OptionConverters.toJava(amount.nbt()).orElse(null));
    }

    public static FluidVariant convert(FluidKey fluidKey) {
        return FluidVariant.of(fluidKey.fluid(), OptionConverters.toJava(fluidKey.tag()).orElse(null));
    }

    static Component getName(FluidAmount amount) {
        return FluidVariantAttributes.getName(convert(amount));
    }

    static boolean isGaseous(FluidAmount amount) {
        return FluidVariantAttributes.isLighterThanAir(convert(amount));
    }

    public static boolean isFluidContainer(ItemStack stack) {
        var storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack));
        return storage != null;
    }

    static FluidAmount getFluidInItem(ItemStack stack) {
        var storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack));
        if (storage != null) {
            for (StorageView<FluidVariant> view : storage) {
                var variant = view.getResource();
                var amount = convertFabricAmountToForge(view.getAmount());
                return FluidAmount.apply(variant.getFluid(), amount, Option.apply(variant.copyNbt()));
            }
        }
        return FluidAmount.EMPTY();
    }

    /**
     * @return the fluid which was inserted.
     */
    public static FluidAmount fillAtPos(FluidAmount source, Level level, BlockPos pos, Direction direction) {
        var storage = FluidStorage.SIDED.find(level, pos, direction);
        if (storage != null && storage.supportsInsertion()) {
            var variant = convert(source);
            var simulate = storage.simulateInsert(variant, convertForgeAmountToFabric(source.amount()), null);
            if (simulate > 0) {
                try (Transaction transaction = Transaction.openOuter()) {
                    var inserted = storage.insert(variant, simulate, transaction);
                    transaction.commit();
                    return source.setAmount(convertFabricAmountToForge(inserted));
                }
            } else {
                // Fill failed.
                return FluidAmount.EMPTY();
            }
        } else {
            // No fluid storage at the pos.
            return FluidAmount.EMPTY();
        }
    }

    public static Option<BucketEventHandler.TransferResult> fillFluidContainer(FluidContainer container, ItemStack itemSource) {
        var itemStorage = ContainerItemContext.withInitial(itemSource);
        var fluidStorage = FluidStorage.ITEM.find(itemSource, itemStorage);
        // Not a fluid storage item.
        if (fluidStorage == null || !fluidStorage.supportsExtraction()) return Option.empty();
        var source = getFluidInItem(itemSource);
        // Nothing to fill.
        if (source.isEmpty()) return Option.empty();

        var fillSimulation = container.fill(source, FluidAction.SIMULATE);
        // Nothing is filled.
        if (fillSimulation.isEmpty()) return Option.empty();

        final FluidAmount drained;
        try (var transaction = Transaction.openOuter()) {
            var result = fluidStorage.extract(convert(fillSimulation), convertForgeAmountToFabric(fillSimulation.amount()), transaction);
            drained = fillSimulation.setAmount(convertFabricAmountToForge(result));
            transaction.abort();
        }
        // Nothing is drained from item.
        if (drained.isEmpty()) return Option.empty();

        var fillSimulation2 = container.fill(drained, FluidAction.SIMULATE);
        // Nothing is filled.
        if (fillSimulation2.isEmpty()) return Option.empty();

        // DO ACTUAL PROCESS
        final FluidAmount drainExecution;
        try (var transaction = Transaction.openOuter()) {
            var result = fluidStorage.extract(convert(fillSimulation), convertForgeAmountToFabric(fillSimulation.amount()), transaction);
            drainExecution = fillSimulation.setAmount(convertFabricAmountToForge(result));
            transaction.commit();
        }
        container.fill(drainExecution, FluidAction.EXECUTE);
        var drainedItem = itemStorage.getItemVariant().toStack(Utils.toInt(itemStorage.getAmount()));
        var emptySound = FluidVariantAttributes.getEmptySound(convert(drainExecution));
        return Option.apply(new BucketEventHandler.TransferResult(new BucketEventHandler.FluidActionResult(drainedItem), emptySound));
    }

    public static Option<BucketEventHandler.TransferResult> fillItemContainer(FluidContainer tank, ItemStack stack, FluidAmount tankContent) {
        var itemStorage = ContainerItemContext.withInitial(stack);
        var fluidStorage = FluidStorage.ITEM.find(stack, itemStorage);
        // Not a fluid storage item.
        if (fluidStorage == null || !fluidStorage.supportsInsertion()) return Option.empty();

        var filled = fluidStorage.simulateInsert(convert(tankContent), convertForgeAmountToFabric(tankContent.amount()), null);
        var toFill = tankContent.setAmount(convertFabricAmountToForge(filled));
        // Nothing is filled into the item.
        if (toFill.isEmpty()) return Option.empty();
        // The fluid can't be drained from the tank.
        var drained = tank.drain(toFill, FluidAction.SIMULATE);
        if (drained.isEmpty()) return Option.empty();

        var drainExecution = tank.drain(drained, FluidAction.EXECUTE);
        try (var transaction = Transaction.openOuter()) {
            fluidStorage.insert(convert(drainExecution), convertForgeAmountToFabric(drainExecution.amount()), transaction);
            transaction.commit();
        }
        var filledItem = itemStorage.getItemVariant().toStack(Utils.toInt(itemStorage.getAmount()));
        var fillSound = FluidVariantAttributes.getFillSound(convert(drainExecution));
        return Option.apply(new BucketEventHandler.TransferResult(new BucketEventHandler.FluidActionResult(filledItem), fillSound));
    }

    public static void addItemToPlayer(Player player, BucketEventHandler.TransferResult result) {
        var storage = PlayerInventoryStorage.of(player);
        var stack = result.result().getResult();
        long inserted;
        try (Transaction transaction = Transaction.openOuter()) {
            inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            transaction.commit();
        }
        if (inserted < stack.getCount()) {
            var toDrop = stack.copy();
            toDrop.setCount(stack.getCount() - (int) inserted);
            player.drop(toDrop, false);
        }
    }
}
