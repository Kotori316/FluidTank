package com.kotori316.fluidtank.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.junit.jupiter.api.Assertions;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.Tier;

import static com.kotori316.testutil.GameTestUtil.EMPTY_STRUCTURE;

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
public final class TankFuelTest {
    static final String BATCH = "tankFuelTestBatch";

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    void checkFuelValueOfWater() {
        var stack = new ItemStack(ModObjects.tierToBlock().apply(Tier.BRONZE));
        FluidUtil.getFluidHandler(stack).ifPresent(h -> h.fill(FluidAmount.BUCKET_WATER().toStack(), IFluidHandler.FluidAction.EXECUTE));

        Assertions.assertEquals(-1, stack.getBurnTime(RecipeType.SMELTING));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    void checkFuelValueOfEmpty() {
        var stack = new ItemStack(ModObjects.tierToBlock().apply(Tier.BRONZE));

        Assertions.assertEquals(-1, stack.getBurnTime(RecipeType.SMELTING));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    void checkFuelValueOfLavaStacked() {
        var stack = new ItemStack(ModObjects.tierToBlock().apply(Tier.BRONZE), 2);
        FluidUtil.getFluidHandler(stack).ifPresent(h -> h.fill(FluidAmount.BUCKET_LAVA().toStack(), IFluidHandler.FluidAction.EXECUTE));

        Assertions.assertEquals(-1, stack.getBurnTime(RecipeType.SMELTING));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    void checkFuelValueOfLava0() {
        var stack = new ItemStack(ModObjects.tierToBlock().apply(Tier.BRONZE));
        FluidUtil.getFluidHandler(stack).ifPresent(h -> h.fill(FluidAmount.BUCKET_LAVA().setAmount(0).toStack(), IFluidHandler.FluidAction.EXECUTE));

        Assertions.assertEquals(-1, stack.getBurnTime(RecipeType.SMELTING));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    void checkFuelValueOfLava() {
        var stack = new ItemStack(ModObjects.tierToBlock().apply(Tier.BRONZE));
        FluidUtil.getFluidHandler(stack).ifPresent(h -> h.fill(FluidAmount.BUCKET_LAVA().toStack(), IFluidHandler.FluidAction.EXECUTE));

        Assertions.assertEquals(100 * 200, stack.getBurnTime(RecipeType.SMELTING));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    void checkFuelValueOfLava2() {
        var stack = new ItemStack(ModObjects.tierToBlock().apply(Tier.BRONZE));
        FluidUtil.getFluidHandler(stack).ifPresent(h -> h.fill(FluidAmount.BUCKET_LAVA().setAmount(500).toStack(), IFluidHandler.FluidAction.EXECUTE));

        Assertions.assertEquals(50 * 200, stack.getBurnTime(RecipeType.SMELTING));
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    void checkFuelValueOfLava3() {
        var stack = new ItemStack(ModObjects.tierToBlock().apply(Tier.BRONZE));
        FluidUtil.getFluidHandler(stack).ifPresent(h -> h.fill(FluidAmount.BUCKET_LAVA().setAmount(100).toStack(), IFluidHandler.FluidAction.EXECUTE));

        Assertions.assertEquals(10 * 200, stack.getBurnTime(RecipeType.SMELTING));
    }
}
