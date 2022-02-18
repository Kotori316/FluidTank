package com.kotori316.fluidtank.gametest;

import java.util.Optional;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;

import com.kotori316.fluidtank.tank.Connection;
import com.kotori316.fluidtank.tank.TankBlock;
import com.kotori316.fluidtank.tank.TileTank;

final class Utils {
    static void placeTank(GameTestHelper helper, BlockPos pos, TankBlock block) {
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
            "defaultBatch", name, FabricGameTest.EMPTY_STRUCTURE, 100, 0L,
            true, test
        );
    }

}
