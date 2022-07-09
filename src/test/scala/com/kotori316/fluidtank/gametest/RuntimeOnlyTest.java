package com.kotori316.fluidtank.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.items.TankItemFluidHandler;
import com.kotori316.fluidtank.tiles.Tier;

public final class RuntimeOnlyTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void createTankItemFluidHandlerInstance(GameTestHelper helper) {
        var tank = new TankItemFluidHandler(Tier.WOOD, new ItemStack(ModObjects.tierToBlock().apply(Tier.WOOD)));
        assert FluidAmount.EMPTY().equals(tank.getFluid());
        assert 4000L == tank.getCapacity();

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void bucketFluid(GameTestHelper helper) {
        assert FluidAmount.fromItem(new ItemStack(Items.BUCKET)).isEmpty();
        helper.succeed();
    }
}
