package com.kotori316.fluidtank.integration;

import java.math.RoundingMode;

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import reborncore.common.fluid.container.ItemFluidInfo;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.tank.Connection;

import static alexiil.mc.lib.attributes.fluid.amount.FluidAmount.BUCKET;

class TechRebornCellInteraction implements FluidInteraction {
    @Override
    public boolean isFluidContainer(ItemStack stack) {
        return stack.getItem() instanceof ItemFluidInfo;
    }

    @Override
    public FluidAmount getFluidInContainer(ItemStack stack) {
        Fluid fluid = ((ItemFluidInfo) stack.getItem()).getFluid(stack);
        FluidKey fluidKey = FluidKeys.get(fluid);
        if (!fluidKey.isEmpty())
            return FluidAmount.apply(fluidKey.withAmount(BUCKET).multiplyAmount(stack.getCount()));
        else
            return FluidAmount.EMPTY();
    }

    @Override
    public FluidVolumeUtil.FluidTankInteraction interact(Connection connection, PlayerEntity player, Hand hand, ItemStack stack) {
        FluidAmount fluidAmount = getFluidInContainer(stack);
        if (fluidAmount.isEmpty()) {
            // Fill the cell and drain from tanks.
            var a = BUCKET.mul(stack.getCount()).min(
                alexiil.mc.lib.attributes.fluid.amount.FluidAmount.of(connection.amount(), FluidAmount.AMOUNT_BUCKET()));
            FluidAmount requires = connection.getFluidStack().map(f -> f.setAmount(a)).getOrElse(FluidAmount::EMPTY);
            FluidAmount toFill = connection.handler().drain(requires, false, a.asLong(FluidAmount.AMOUNT_BUCKET()));
            if (toFill.nonEmpty() && toFill.fluid() != null) { // Null means not registered fluid such as potion.
                int stackSize = toFill.fluidVolume().amount().asInt(1);
                ItemStack full = ((ItemFluidInfo) stack.getItem()).getFull(toFill.fluid());
                stack.decrement(stackSize);
                full.setCount(stackSize);
                if (!player.getInventory().insertStack(full)) {
                    player.dropStack(full);
                }
                connection.handler().drain(toFill, true, 0);
                player.playSound(getSoundEvent(toFill, true), SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return new FluidVolumeUtil.FluidTankInteraction(toFill.fluidVolume(), false,
                FluidVolumeUtil.ItemContainerStatus.NOT_CHECKED, FluidVolumeUtil.ItemContainerStatus.VALID);
        } else {
            // Drain from cell and fill tank.
            if (connection.getFluidStack().forall(f -> f.fluidEqual(fluidAmount))) {
                var simulation = connection.handler().fill(fluidAmount.setAmount(BUCKET.mul(stack.getCount())), false, 0);
                // Draining from cell always successes.
                int stackSize = simulation.fluidVolume().amount().asInt(1, RoundingMode.DOWN);
                ItemStack empty = ((ItemFluidInfo) stack.getItem()).getEmpty();
                stack.decrement(stackSize);
                empty.setCount(stackSize);
                if (!player.getInventory().insertStack(empty)) {
                    player.dropStack(empty);
                }
                connection.handler().fill(fluidAmount.setAmount(simulation.fluidVolume().amount()), true, 0);
                player.playSound(getSoundEvent(fluidAmount, false), SoundCategory.BLOCKS, 1.0f, 1.0f);
            } else {
                return FluidVolumeUtil.FluidTankInteraction.none(FluidVolumeUtil.ItemContainerStatus.VALID, FluidVolumeUtil.ItemContainerStatus.NOT_CHECKED);
            }
        }
        return FluidVolumeUtil.FluidTankInteraction.NONE;
    }

    private static SoundEvent getSoundEvent(FluidAmount toFill, boolean fill) {
        final SoundEvent soundEvent;
        if (toFill.fluidEqual(FluidAmount.BUCKET_LAVA()))
            soundEvent = fill ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_EMPTY_LAVA;
        else
            soundEvent = fill ? SoundEvents.ITEM_BUCKET_FILL : SoundEvents.ITEM_BUCKET_EMPTY;
        return soundEvent;
    }
}
