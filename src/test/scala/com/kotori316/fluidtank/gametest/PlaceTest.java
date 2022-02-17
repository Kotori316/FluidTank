package com.kotori316.fluidtank.gametest;

import java.util.Objects;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.tank.Connection;
import com.kotori316.fluidtank.tank.Tiers;
import com.kotori316.fluidtank.tank.TileTank;

public final class PlaceTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void placeStone(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), Blocks.STONE);
        helper.assertBlock(new BlockPos(0, 1, 0), Predicate.isEqual(Blocks.STONE), "Stone");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void placeTank1(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, ModTank.Entries.WOOD_TANK);
        var entity = helper.getBlockEntity(BlockPos.ZERO);
        if (entity instanceof TileTank tank) {
            var connection = tank.connection();
            assert Objects.equals(connection, Connection.invalid()) : "Connection before initialization must be invalid. " + connection;
        } else {
            throw new GameTestAssertException("Expected TileTank but " + entity);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void placeTank2(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, ModTank.Entries.WOOD_TANK);
        var entity = helper.getBlockEntity(BlockPos.ZERO);
        if (entity instanceof TileTank tank) {
            tank.onBlockPlacedBy();
            var connection = tank.connection();
            assert connection.capacity() == Tiers.WOOD.amount() : "Capacity of Wood Tank is 4000. " + connection;
            assert connection.amount() == 0 : "Amount must be 0.";
            assert connection.getFluidStack().isEmpty() : "Fluid must be empty. " + connection.getFluidStack();
        } else {
            throw new GameTestAssertException("Expected TileTank but " + entity);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void dummy(GameTestHelper helper) {
        // assert false : "Fail Test";
        helper.succeed();
    }
}
