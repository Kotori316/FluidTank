package com.kotori316.fluidtank.gametest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.blocks.TankPos;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.FluidConnection;
import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.fluidtank.tiles.TileTank;

import static com.kotori316.testutil.GameTestUtil.EMPTY_STRUCTURE;
import static com.kotori316.testutil.GameTestUtil.NO_PLACE_STRUCTURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
public final class PlaceTest {
    static final String BATCH = "placeTestBatch";

    public static void placeTank(GameTestHelper helper, BlockPos pos, BlockTank block) {
        helper.setBlock(pos, block);
        Optional.ofNullable(helper.getBlockEntity(pos))
            .map(TileTank.class::cast)
            .ifPresent(t -> {
                t.skipLoadingLog_$eq(true);
                t.onBlockPlacedBy();
            });
    }

    public static FluidConnection getConnection(GameTestHelper helper, BlockPos pos) {
        return Optional.ofNullable(helper.getBlockEntity(pos))
            .map(TileTank.class::cast)
            .map(TileTank::connection)
            .orElseThrow(() -> new IllegalArgumentException("No tank at " + pos));
    }


    @BeforeBatch(batch = BATCH)
    public void beforeTest(ServerLevel level) {
        com.kotori316.fluidtank.Utils.setInDev(false);
    }

    @AfterBatch(batch = BATCH)
    public void afterTest(ServerLevel level) {
        com.kotori316.fluidtank.Utils.setInDev(true);
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void placeStone(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), Blocks.STONE);
        helper.assertBlock(new BlockPos(0, 1, 0), Predicate.isEqual(Blocks.STONE), "Stone");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void placeTank1(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, ModObjects.tierToBlock().apply(Tier.WOOD));
        var entity = helper.getBlockEntity(pos);
        if (entity instanceof TileTank tank) {
            tank.skipLoadingLog_$eq(true);
            var connection = tank.connection();
            assertTrue(connection.isDummy(), "Connection before initialization must be invalid. " + connection);
        } else {
            throw new GameTestAssertException("Expected TileTank but " + entity);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void placeTank2(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, ModObjects.tierToBlock().apply(Tier.WOOD));
        var entity = helper.getBlockEntity(pos);
        if (entity instanceof TileTank tank) {
            tank.skipLoadingLog_$eq(true);
            tank.onBlockPlacedBy();
            var connection = tank.connection();
            assertEquals(connection.capacity(), Tier.WOOD.amount(), "Capacity of Wood Tank is 4000. " + connection);
            assertEquals(0, connection.amount(), "Amount must be 0.");
            assertTrue(connection.getFluidStack().isEmpty(), "Fluid must be empty. " + connection.getFluidStack());
        } else {
            throw new GameTestAssertException("Expected TileTank but " + entity);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void placeTank3(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenExecute(() -> placeTank(helper, pos.above(), ModObjects.tierToBlock().apply(Tier.WOOD)))
            .thenIdle(2)
            .thenExecute(() -> {
                var c = getConnection(helper, pos.above());
                assertEquals(4000L, c.capacity(), "Connection is not created.");
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void place2Tanks(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.tierToBlock().apply(Tier.WOOD));
        var tank = (TileTank) helper.getBlockEntity(pos);
        assertNotNull(tank, "Tank must not be null. %s at %s".formatted(helper.getBlockState(pos), pos));
        placeTank(helper, pos.above(), ModObjects.tierToBlock().apply(Tier.WOOD));

        assertEquals(8000, tank.connection().capacity(), "Wood + Wood, " + tank.connection());
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void place2Tanks2(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenWaitUntil(() -> placeTank(helper, pos, ModObjects.tierToBlock().apply(Tier.WOOD)))
            .thenIdle(2)
            .thenWaitUntil(() -> placeTank(helper, pos.above(), ModObjects.tierToBlock().apply(Tier.STONE)))
            .thenExecuteAfter(2, () -> helper.assertBlockPresent(ModObjects.tierToBlock().apply(Tier.WOOD), pos))
            .thenWaitUntil(() -> helper.assertBlockPresent(ModObjects.tierToBlock().apply(Tier.STONE), pos.above()))
            .thenIdle(2)
            .thenWaitUntil(() -> {
                var tank1 = (TileTank) helper.getBlockEntity(pos);
                var tank2 = (TileTank) helper.getBlockEntity(pos.above());
                assertNotNull(tank1, "Tank1 must not be null");
                assertNotNull(tank2, "Tank2 must not be null");
                assertSame(tank1.connection(), tank2.connection(), "Connection of tanks must be same instance. %s, %s".formatted(tank1.connection(), tank2.connection()));
                assertEquals(20000, tank1.connection().capacity(),
                    "Tank capacity must be Wood + Stone. %s, %s".formatted(tank1.internalTank().getTank(), tank2.internalTank().getTank()));
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void place3Tanks1(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var block = ModObjects.tierToBlock().apply(Tier.IRON);
        List.of(pos, pos.above(1), pos.above(2)).forEach(p -> placeTank(helper, p, block));

        var tile = (TileTank) Objects.requireNonNull(helper.getBlockEntity(pos));
        var connection = tile.connection();
        assertEquals(tile.internalTank().getTank().capacity() * 3L, connection.capacity(),
            "Tank capacity must be 3 times of each tank. Tank=%d, Connection=%d".formatted(tile.internalTank().getTank().capacity(), connection.capacity()));
        assertEquals(3, connection.sortedTanks().length(), "Connection must contain 3 tanks. seq=" + connection.sortedTanks());

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void placeWaterOnEmpty(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.tierToBlock().apply(Tier.WOOD));

        helper.setBlock(pos.above(), ModObjects.tierToBlock().apply(Tier.STONE));
        var stoneTile = (TileTank) Objects.requireNonNull(helper.getBlockEntity(pos.above()));
        var preConnection = stoneTile.connection();
        stoneTile.internalTank().fill(FluidAmount.BUCKET_WATER().setAmount(16000), IFluidHandler.FluidAction.EXECUTE);

        stoneTile.onBlockPlacedBy();
        var afterConnection = stoneTile.connection();
        assertNotEquals(preConnection, afterConnection);
        assertEquals(2, afterConnection.sortedTanks().size());
        assertEquals(scala.Option.apply(FluidAmount.BUCKET_WATER().setAmount(16000)), afterConnection.getFluidStack());
        var woodTank = (TileTank) Objects.requireNonNull(helper.getBlockEntity(pos));
        assertEquals(Tier.WOOD, woodTank.tier());
        assertEquals(woodTank.connection(), afterConnection);

        helper.succeed();
    }

    @GameTest(template = NO_PLACE_STRUCTURE)
    public void dummy(GameTestHelper helper) {
        // fail("Fail Test");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void removeMiddleTank(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenExecute(() -> {
                for (int i = 0; i < 3; i++) {
                    helper.setBlock(pos.above(i), ModObjects.tierToBlock().apply(Tier.WOOD));
                    Objects.requireNonNull((TileTank) helper.getBlockEntity(pos.above(i))).skipLoadingLog_$eq(true);
                }
            })
            .thenExecuteAfter(1, () -> {
                helper.assertBlockProperty(pos, TankPos.TANK_POS_PROPERTY, TankPos.BOTTOM);
                helper.assertBlockProperty(pos.above(), TankPos.TANK_POS_PROPERTY, TankPos.MIDDLE);
                helper.assertBlockProperty(pos.above(2), TankPos.TANK_POS_PROPERTY, TankPos.TOP);
            })
            .thenExecuteAfter(1, () -> helper.setBlock(pos.above(), Blocks.AIR))
            .thenExecuteAfter(1, () -> {
                helper.assertBlockProperty(pos, TankPos.TANK_POS_PROPERTY, TankPos.SINGLE);
                helper.assertBlockProperty(pos.above(2), TankPos.TANK_POS_PROPERTY, TankPos.SINGLE);
            })
            .thenSucceed();
    }
}
