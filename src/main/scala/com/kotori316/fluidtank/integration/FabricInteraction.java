package com.kotori316.fluidtank.integration;

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.tank.Connection;

@SuppressWarnings("UnstableApiUsage")
final class FabricInteraction implements FluidInteraction {
    @Override
    public boolean isFluidContainer(ItemStack stack) {
        var context = ContainerItemContext.withInitial(stack);
        return context.find(FluidStorage.ITEM) != null;
    }

    @Override
    public FluidAmount getFluidInContainer(ItemStack stack) {
        var context = ContainerItemContext.withInitial(stack);
        var storage = context.find(FluidStorage.ITEM);
        if (storage == null) {
            return FluidAmount.EMPTY();
        } else {
            return getFluidInStorage(storage);
        }
    }

    private FluidAmount getFluidInStorage(Storage<FluidVariant> storage) {
        try (var transaction = Transaction.openOuter()) {
            var itr = storage.iterator(transaction);
            if (itr.hasNext()) {
                var s = itr.next();
                return FabricFluidTankStorage.getFluidAmount(s.getResource(), s.getAmount());
            } else {
                return FluidAmount.EMPTY();
            }
        }
    }

    @Override
    public FluidVolumeUtil.FluidTankInteraction interact(Connection connection, Player player, InteractionHand hand, ItemStack stack) {
        var context = ContainerItemContext.ofPlayerHand(player, hand);
        var storage = context.find(FluidStorage.ITEM);
        if (storage == null) return FluidVolumeUtil.FluidTankInteraction.NONE;
        var fluid = getFluidInStorage(storage);
        if (fluid.isEmpty()) {
            // Fill item with tank fluid.
            var tankContent = connection.getFluidStack().getOrElse(FluidAmount::EMPTY);
            if (tankContent.isEmpty()) // Tank is empty.
                return FluidVolumeUtil.FluidTankInteraction.NONE;
            long filledSimulate;
            try (Transaction transaction = Transaction.openOuter()) {
                filledSimulate = storage.insert(FluidVariant.of(tankContent.fluid()), FabricFluidTankStorage.asFabricAmount(tankContent.fluidVolume().amount()), transaction);
                transaction.abort();
            }
            if (filledSimulate <= 0) // Fill into item failed.
                return FluidVolumeUtil.FluidTankInteraction.NONE;
            var toDrain = connection.handler().drain(tankContent.setAmount(FabricFluidTankStorage.asBCAmount(filledSimulate)), false, 0);
            if (toDrain.isEmpty()) // Drain from tank failed.
                return FluidVolumeUtil.FluidTankInteraction.NONE;
            long toFill;
            try (Transaction transaction = Transaction.openOuter()) {
                toFill = storage.insert(FluidVariant.of(toDrain.fluid()), FabricFluidTankStorage.asFabricAmount(toDrain.fluidVolume().amount()), transaction);
                transaction.abort();
            }

            // Execution
            var drainedExecute = connection.handler().drain(toDrain.setAmount(FabricFluidTankStorage.asBCAmount(toFill)), true, 0);
            long filled;
            try (Transaction transaction = Transaction.openOuter()) {
                filled = storage.insert(FluidVariant.of(drainedExecute.fluid()), FabricFluidTankStorage.asFabricAmount(drainedExecute.fluidVolume().amount()), transaction);
                transaction.commit();
            }
            player.playNotifySound(FluidInteraction.getSoundEvent(drainedExecute, true), SoundSource.BLOCKS, 1.0f, 1.0f);
            return new FluidVolumeUtil.FluidTankInteraction(drainedExecute.fluidVolume().withAmount(FabricFluidTankStorage.asBCAmount(filled)),
                false, FluidVolumeUtil.ItemContainerStatus.NOT_CHECKED, FluidVolumeUtil.ItemContainerStatus.VALID);
        } else {
            // Fill tank with the fluid in item.
            var filledSimulate = connection.handler().fill(fluid, false, 0);
            if (filledSimulate.isEmpty()) // Fill into tank failed.
                return FluidVolumeUtil.FluidTankInteraction.NONE;
            long drained;
            try (Transaction transaction = Transaction.openOuter()) {
                drained = storage.extract(FluidVariant.of(filledSimulate.fluid()), FabricFluidTankStorage.asFabricAmount(filledSimulate.fluidVolume().amount()), transaction);
                transaction.abort();
            }
            if (drained <= 0) // Drain from item failed.
                return FluidVolumeUtil.FluidTankInteraction.NONE;
            var toFill = connection.handler().fill(fluid.setAmount(FabricFluidTankStorage.asBCAmount(drained)), false, 0);
            if (toFill.isEmpty()) // Fill into tank failed.
                return FluidVolumeUtil.FluidTankInteraction.NONE;

            // Execution
            long drainedExecute;
            try (Transaction transaction = Transaction.openOuter()) {
                drainedExecute = storage.extract(FluidVariant.of(toFill.fluid()), FabricFluidTankStorage.asFabricAmount(toFill.fluidVolume().amount()), transaction);
                transaction.commit();
            }
            var filled = connection.handler().fill(toFill.setAmount(FabricFluidTankStorage.asBCAmount(drainedExecute)), true, 0);
            player.playNotifySound(FluidInteraction.getSoundEvent(filled, false), SoundSource.BLOCKS, 1.0f, 1.0f);
            return new FluidVolumeUtil.FluidTankInteraction(filled.fluidVolume(), true, FluidVolumeUtil.ItemContainerStatus.VALID, FluidVolumeUtil.ItemContainerStatus.NOT_CHECKED);
        }
    }
}
