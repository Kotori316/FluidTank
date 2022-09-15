package com.kotori316.fluidtank.gametest;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FluidAction;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.items.TankItemFluidHandler;
import com.kotori316.fluidtank.tiles.Tier;

import static com.kotori316.fluidtank.gametest.Utils.getConnection;
import static com.kotori316.fluidtank.gametest.Utils.placeTank;

public final class DropTest implements FabricGameTest {

    BlockTank wood_tank = ModObjects.tierToBlock().apply(Tier.WOOD);

    @GameTest(template = EMPTY_STRUCTURE)
    public void dropOfEmptyTank(GameTestHelper helper) {
        var pos = BlockPos.ZERO.east();
        placeTank(helper, pos, wood_tank);

        var drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
            helper.getBlockEntity(pos), helper.makeMockPlayer(), ItemStack.EMPTY);
        assert drops.size() == 1 : "Drop was " + drops;
        var stack = drops.get(0);
        assert stack.getItem() == wood_tank.itemBlock() : "Dropped item was " + stack;
        assert !stack.hasTag() : "Stack must not have tag if dropped from empty tank. " + stack;

        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> dropOfFilledTank() {
        return Stream.of(
                FluidAmount.BUCKET_WATER(),
                FluidAmount.BUCKET_LAVA()
            ).flatMap(f -> IntStream.of(500, 1000, 1500, 2000, 3000, 4000).mapToObj(f::setAmount))
            .map(f -> Utils.create("dropOfFilledTank" + f, g -> dropOfWaterTank1(g, f)))
            .toList();
    }

    void dropOfWaterTank1(GameTestHelper helper, FluidAmount amount) {
        var pos = BlockPos.ZERO.east();
        placeTank(helper, pos, wood_tank);
        var connection = getConnection(helper, pos);
        connection.handler().fill(amount, FluidAction.EXECUTE);

        var drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
            helper.getBlockEntity(pos), helper.makeMockPlayer(), ItemStack.EMPTY);
        assert drops.size() == 1 : "Drop was " + drops;
        var stack = drops.get(0);
        assert stack.getItem() == wood_tank.itemBlock() : "Dropped item was " + stack;
        var itemTank = new TankItemFluidHandler(Tier.WOOD, stack);
        assert itemTank.getFluid().equals(amount) : "Fluid must be given fluid. " + itemTank.getFluid();

        helper.succeed();
    }
}
