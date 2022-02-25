package com.kotori316.fluidtank.gametest;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.tiles.Connection;
import com.kotori316.fluidtank.tiles.TileTank;

final class Utils {
    public static final String EMPTY_STRUCTURE = "empty";

    static void placeTank(GameTestHelper helper, BlockPos pos, BlockTank block) {
        helper.setBlock(pos, block);
        Optional.ofNullable(helper.getBlockEntity(pos))
            .map(TileTank.class::cast)
            .ifPresent(TileTank::onBlockPlacedBy);
    }

    static Connection getConnection(GameTestHelper helper, BlockPos pos) {
        return Optional.ofNullable(helper.getBlockEntity(pos))
            .map(TileTank.class::cast)
            .map(TileTank::connection)
            .orElseThrow(() -> new IllegalArgumentException("No tank at " + pos));
    }

    static TestFunction create(String name, Consumer<GameTestHelper> test) {
        return new TestFunction(
            "defaultBatch", name, FluidTank.modID + ":" + EMPTY_STRUCTURE, 100, 0L,
            true, test
        );
    }

    static void assertTrue(boolean condition, String message) throws GameTestAssertException {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }
}
