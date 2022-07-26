package com.kotori316.fluidtank.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import com.kotori316.fluidtank.ModTank;

public final class FluidTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void milkIsSource(GameTestHelper helper) {
        assert ModTank.Entries.MILK_FLUID.isSource(ModTank.Entries.MILK_FLUID.defaultFluidState());
        helper.succeed();
    }
}
