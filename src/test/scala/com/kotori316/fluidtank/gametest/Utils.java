package com.kotori316.fluidtank.gametest;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.junit.jupiter.api.Assertions;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.tiles.Connection;
import com.kotori316.fluidtank.tiles.TileTank;

final class Utils {
    public static final String EMPTY_STRUCTURE = "empty";
    public static final String TANK2_STRUCTURE = "tank2";

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

    static TestFunction create(String testName, String batchName, Consumer<GameTestHelper> test) {
        return new TestFunction(
            batchName, testName, FluidTank.modID + ":" + EMPTY_STRUCTURE, 100, 0L,
            true, test
        );
    }

    static BlockPos getBasePos(GameTestHelper helper) {
        try {
            var f = GameTestHelper.class.getDeclaredField("testInfo");
            f.setAccessible(true);
            var info = (GameTestInfo) f.get(helper);
            var pos = info.getStructureBlockPos();
            var structure = (StructureBlockEntity) helper.getLevel().getBlockEntity(pos);
            Assertions.assertNotNull(structure);
            return structure.getStructurePos();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
