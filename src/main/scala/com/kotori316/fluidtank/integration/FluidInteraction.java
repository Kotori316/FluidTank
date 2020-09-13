package com.kotori316.fluidtank.integration;

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.tank.Connection;

public interface FluidInteraction {
    boolean isFluidContainer(ItemStack stack);

    FluidAmount getFluidInContainer(ItemStack stack);

    FluidVolumeUtil.FluidTankInteraction interact(Connection connection, PlayerEntity player, Hand hand, ItemStack stack);
}
