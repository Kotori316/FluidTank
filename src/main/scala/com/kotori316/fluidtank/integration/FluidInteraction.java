package com.kotori316.fluidtank.integration;

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.tank.Connection;

public interface FluidInteraction {
    boolean isFluidContainer(ItemStack stack);

    FluidAmount getFluidInContainer(ItemStack stack);

    FluidVolumeUtil.FluidTankInteraction interact(Connection connection, Player player, InteractionHand hand, ItemStack stack);

    @SuppressWarnings("UnstableApiUsage")
    static SoundEvent getSoundEvent(FluidAmount toFill, boolean fill) {
        if (fill)
            return FluidVariantAttributes.getFillSound(FluidVariant.of(toFill.fluid()));
        else
            return FluidVariantAttributes.getEmptySound(FluidVariant.of(toFill.fluid()));
    }
}
