package com.kotori316.fluidtank.integration;

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.tank.Connection;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface FluidInteraction {
    boolean isFluidContainer(ItemStack stack);

    FluidAmount getFluidInContainer(ItemStack stack);

    FluidVolumeUtil.FluidTankInteraction interact(Connection connection, Player player, InteractionHand hand, ItemStack stack);
}
