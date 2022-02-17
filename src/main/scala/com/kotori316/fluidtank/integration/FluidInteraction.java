package com.kotori316.fluidtank.integration;

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.tank.Connection;

public interface FluidInteraction {
    boolean isFluidContainer(ItemStack stack);

    FluidAmount getFluidInContainer(ItemStack stack);

    FluidVolumeUtil.FluidTankInteraction interact(Connection connection, Player player, InteractionHand hand, ItemStack stack);

    static SoundEvent getSoundEvent(FluidAmount toFill, boolean fill) {
        final SoundEvent soundEvent;
        if (toFill.fluidEqual(FluidAmount.BUCKET_LAVA()))
            soundEvent = fill ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_EMPTY_LAVA;
        else
            soundEvent = fill ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY;
        return soundEvent;
    }
}
