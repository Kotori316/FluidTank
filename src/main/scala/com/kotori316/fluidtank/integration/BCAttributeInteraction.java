package com.kotori316.fluidtank.integration;

import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidInvUtil;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.impl.EmptyFluidExtractable;
import alexiil.mc.lib.attributes.fluid.impl.RejectingFluidInsertable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.tank.Connection;

class BCAttributeInteraction implements FluidInteraction {
    @Override
    public boolean isFluidContainer(ItemStack stack) {
        return FluidAttributes.INSERTABLE.get(stack) != RejectingFluidInsertable.NULL ||
            FluidAttributes.EXTRACTABLE.get(stack) != EmptyFluidExtractable.NULL;
    }

    @Override
    public FluidAmount getFluidInContainer(ItemStack stack) {
        return FluidAmount.fromItem(stack);
    }

    @Override
    public FluidVolumeUtil.FluidTankInteraction interact(Connection connection, PlayerEntity player, Hand hand, ItemStack stack) {
        return FluidInvUtil.interactHandWithTank(connection.handler(), player, hand);
    }
}
