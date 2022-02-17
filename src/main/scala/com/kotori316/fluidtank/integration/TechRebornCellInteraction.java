package com.kotori316.fluidtank.integration;

import java.math.RoundingMode;

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
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
    public FluidVolumeUtil.FluidTankInteraction interact(Connection connection, Player player, InteractionHand hand, ItemStack stack) {
        FluidAmount fluidAmount = getFluidInContainer(stack);
        if (fluidAmount.isEmpty()) {
            // Fill the cell and drain from tanks.
            var a = BUCKET.mul(stack.getCount()).min(connection.amountInBCAmount());
            FluidAmount requires = connection.getFluidStack().map(f -> f.setAmount(a)).getOrElse(FluidAmount::EMPTY);
            FluidAmount toFill = connection.handler().drain(requires, false, a.asLong(FluidAmount.AMOUNT_BUCKET()));
            if (toFill.nonEmpty() && toFill.fluid() != null) { // Null means not registered fluid such as potion.
                int stackSize = toFill.fluidVolume().amount().asInt(1);
                ItemStack full = ((ItemFluidInfo) stack.getItem()).getFull(toFill.fluid());
                stack.shrink(stackSize);
                full.setCount(stackSize);
                if (!player.getInventory().add(full)) {
                    player.spawnAtLocation(full);
                }
                connection.handler().drain(toFill, true, 0);
                player.playNotifySound(FluidInteraction.getSoundEvent(toFill, true), SoundSource.BLOCKS, 1.0f, 1.0f);
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
                stack.shrink(stackSize);
                empty.setCount(stackSize);
                if (!player.getInventory().add(empty)) {
                    player.spawnAtLocation(empty);
                }
                connection.handler().fill(fluidAmount.setAmount(simulation.fluidVolume().amount()), true, 0);
                player.playNotifySound(FluidInteraction.getSoundEvent(fluidAmount, false), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else {
                return FluidVolumeUtil.FluidTankInteraction.none(FluidVolumeUtil.ItemContainerStatus.VALID, FluidVolumeUtil.ItemContainerStatus.NOT_CHECKED);
            }
        }
        return FluidVolumeUtil.FluidTankInteraction.NONE;
    }
}
