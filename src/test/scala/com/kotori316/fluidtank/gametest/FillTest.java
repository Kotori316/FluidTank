package com.kotori316.fluidtank.gametest;

import java.util.List;
import java.util.stream.IntStream;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.fluids.FluidAction;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.items.ReservoirItem;
import com.kotori316.fluidtank.items.TankItemFluidHandler;
import com.kotori316.fluidtank.tiles.TileTank;

public final class FillTest implements FabricGameTest {

    private static final ReservoirItem RESERVOIR_ITEM = ModObjects.itemReservoirs().apply(0);

    @GameTest(template = EMPTY_STRUCTURE)
    public void fillToEmpty1Simulate(GameTestHelper helper) {
        var stack = new ItemStack(RESERVOIR_ITEM);
        var handler = new TankItemFluidHandler(RESERVOIR_ITEM.tier(), stack);

        assert handler.getFluid().isEmpty() : "Tank shouldn't contain any fluid. " + handler.getFluid();
        var result = handler.fill(FluidAmount.BUCKET_WATER(), FluidAction.SIMULATE);
        assert result.equals(FluidAmount.BUCKET_WATER()) : "Fill should succeed. " + result;
        assert handler.getFluid().isEmpty() : "Tank shouldn't contain any fluid after simulation. " + handler.getFluid();
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void fillToEmpty1Execute(GameTestHelper helper) {
        fillTest(helper, FluidAmount.EMPTY(), FluidAmount.BUCKET_WATER(), FluidAmount.BUCKET_WATER(), FluidAction.EXECUTE,
            FluidAmount.BUCKET_WATER());
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void fillToWater1Simulate(GameTestHelper helper) {
        fillTest(helper, FluidAmount.BUCKET_WATER(), FluidAmount.BUCKET_WATER(), FluidAmount.BUCKET_WATER(), FluidAction.SIMULATE,
            FluidAmount.BUCKET_WATER());
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void fillToWater1Execute(GameTestHelper helper) {
        fillTest(helper, FluidAmount.BUCKET_WATER(), FluidAmount.BUCKET_WATER(), FluidAmount.BUCKET_WATER(), FluidAction.EXECUTE,
            FluidAmount.BUCKET_WATER().setAmount(2000L));
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void fillToLava1(GameTestHelper helper) {
        var stack = new ItemStack(RESERVOIR_ITEM);
        Utils.setTileTag(stack, getTankTag(FluidAmount.BUCKET_LAVA(), 4000));
        var handler = new TankItemFluidHandler(RESERVOIR_ITEM.tier(), stack);
        var result = handler.fill(FluidAmount.BUCKET_WATER(), FluidAction.SIMULATE);
        assert result.isEmpty();
        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> fillLava() {
        var sources = List.of(
            FluidAmount.EMPTY(),
            FluidAmount.EMPTY().setAmount(2000L),
            FluidAmount.BUCKET_LAVA(),
            FluidAmount.BUCKET_LAVA().setAmount(1500)
        );
        var results = List.of(
            FluidAmount.BUCKET_LAVA(),
            FluidAmount.BUCKET_LAVA(),
            FluidAmount.BUCKET_LAVA().setAmount(2000),
            FluidAmount.BUCKET_LAVA().setAmount(2500)
        );
        return IntStream.range(0, sources.size())
            .mapToObj(i -> {
                var s = sources.get(i);
                var r = results.get(i);
                return com.kotori316.fluidtank.gametest.Utils.create("Fill %s to %s".formatted(FluidAmount.BUCKET_LAVA(), s),
                    g -> fillTest(g, s, FluidAmount.BUCKET_LAVA(), FluidAmount.BUCKET_LAVA(), FluidAction.EXECUTE, r));
            }).toList();
    }

    @GameTestGenerator
    public List<TestFunction> drainFromEmpty() {
        var toDrain = List.of(FluidAmount.EMPTY(), FluidAmount.EMPTY().setAmount(2000),
            FluidAmount.BUCKET_LAVA(), FluidAmount.BUCKET_WATER());
        return toDrain.stream().map(s ->
                com.kotori316.fluidtank.gametest.Utils.create("Drain from empty: " + s, g ->
                    drainTest(g, FluidAmount.EMPTY(), s, FluidAmount.EMPTY(), FluidAction.SIMULATE, FluidAmount.EMPTY())))
            .toList();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void drainLava1(GameTestHelper helper) {
        drainTest(helper, FluidAmount.BUCKET_LAVA(), FluidAmount.BUCKET_LAVA(), FluidAmount.BUCKET_LAVA(), FluidAction.EXECUTE, FluidAmount.BUCKET_LAVA().setAmount(0));
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void drainLava2(GameTestHelper helper) {
        drainTest(helper, FluidAmount.BUCKET_LAVA().setAmount(2000), FluidAmount.BUCKET_LAVA(), FluidAmount.BUCKET_LAVA(), FluidAction.EXECUTE, FluidAmount.BUCKET_LAVA());
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void drainLava3(GameTestHelper helper) {
        drainTest(helper, FluidAmount.BUCKET_LAVA().setAmount(2000), FluidAmount.EMPTY().setAmount(1000), FluidAmount.BUCKET_LAVA(), FluidAction.EXECUTE, FluidAmount.BUCKET_LAVA());
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void drainLava4(GameTestHelper helper) {
        var stack = new ItemStack(RESERVOIR_ITEM);
        Utils.setTileTag(stack, getTankTag(FluidAmount.BUCKET_LAVA(), 10000));
        var handler = new TankItemFluidHandler(RESERVOIR_ITEM.tier(), stack);
        var result = handler.drain(1000, FluidAction.SIMULATE);
        assert result.equals(FluidAmount.BUCKET_LAVA());
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void drainWaterFromLava(GameTestHelper helper) {
        drainTest(helper, FluidAmount.BUCKET_WATER(), FluidAmount.BUCKET_LAVA(), FluidAmount.EMPTY(), FluidAction.SIMULATE, FluidAmount.BUCKET_WATER());
    }

    private static CompoundTag getTankTag(FluidAmount amount, long capacity) {
        var tankTag = new CompoundTag();
        amount.write(tankTag);
        tankTag.putLong(TileTank.NBT_Capacity(), capacity);
        var tag = new CompoundTag();
        tag.put(TileTank.NBT_Tank(), tankTag);
        return tag;
    }

    private static void fillTest(GameTestHelper helper, FluidAmount source, FluidAmount toFill, FluidAmount fillResult, FluidAction mode, FluidAmount filled) {
        var stack = new ItemStack(RESERVOIR_ITEM);
        Utils.setTileTag(stack, getTankTag(source, 8000));
        var handler = new TankItemFluidHandler(RESERVOIR_ITEM.tier(), stack);
        assert source.equals(handler.getFluid()) : "Initialization should be completed. " + handler.getFluid();
        var result = handler.fill(toFill, mode);
        assert result.equals(fillResult) : "Filling should succeed. " + result;

        assert filled.equals(handler.getFluid()) : "%s Expect %s, Actual %s".formatted(mode, filled, handler.getFluid());

        helper.succeed();
    }

    private static void drainTest(GameTestHelper helper, FluidAmount source, FluidAmount toDrain, FluidAmount drainResult, FluidAction mode, FluidAmount lastStatus) {
        var stack = new ItemStack(RESERVOIR_ITEM);
        Utils.setTileTag(stack, getTankTag(source, 10000));
        var handler = new TankItemFluidHandler(RESERVOIR_ITEM.tier(), stack);
        assert source.equals(handler.getFluid()) : "Initialization should be completed. " + handler.getFluid();
        var result = handler.drain(toDrain, mode);
        assert result.equals(drainResult) : "Draining should succeed. " + result;

        assert lastStatus.equals(handler.getFluid()) : "%s Expect %s, Actual %s".formatted(mode, lastStatus, handler.getFluid());

        helper.succeed();
    }
}
