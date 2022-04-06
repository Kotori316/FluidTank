package com.kotori316.fluidtank.gametest;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.fluidtank.tiles.TileTank;

import static com.kotori316.fluidtank.gametest.Utils.EMPTY_STRUCTURE;
import static com.kotori316.fluidtank.gametest.Utils.getConnection;
import static com.kotori316.fluidtank.gametest.Utils.placeTank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
public final class PlaceTest {
    static final String BATCH = "placeTestBatch";

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
        helper.setBlock(BlockPos.ZERO, ModObjects.tierToBlock().apply(Tier.WOOD));
        var entity = helper.getBlockEntity(BlockPos.ZERO);
        if (entity instanceof TileTank tank) {
            var connection = tank.connection();
            assertTrue(connection.isDummy(), "Connection before initialization must be invalid. " + connection);
        } else {
            throw new GameTestAssertException("Expected TileTank but " + entity);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void placeTank2(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, ModObjects.tierToBlock().apply(Tier.WOOD));
        var entity = helper.getBlockEntity(BlockPos.ZERO);
        if (entity instanceof TileTank tank) {
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
        helper.startSequence()
            .thenWaitUntil(() -> placeTank(helper, BlockPos.ZERO, ModObjects.tierToBlock().apply(Tier.WOOD)))
            .thenIdle(2)
            .thenWaitUntil(() -> placeTank(helper, BlockPos.ZERO.above(), ModObjects.blockTanks().apply(1)))
            .thenExecuteAfter(2, () -> helper.assertBlockPresent(ModObjects.tierToBlock().apply(Tier.WOOD), BlockPos.ZERO))
            .thenWaitUntil(() -> helper.assertBlockPresent(ModObjects.blockTanks().apply(1), BlockPos.ZERO.above()))
            .thenIdle(2)
            .thenWaitUntil(() -> {
                var tank1 = (TileTank) helper.getBlockEntity(BlockPos.ZERO);
                var tank2 = (TileTank) helper.getBlockEntity(BlockPos.ZERO.above());
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
        var block = ModObjects.blockTanks().apply(2);
        List.of(pos, pos.above(1), pos.above(2)).forEach(p -> placeTank(helper, p, block));

        var tile = (TileTank) Objects.requireNonNull(helper.getBlockEntity(pos));
        var connection = tile.connection();
        assertEquals(tile.internalTank().getTank().capacity() * 3L, connection.capacity(),
            "Tank capacity must be 3 times of each tank. Tank=%d, Connection=%d".formatted(tile.internalTank().getTank().capacity(), connection.capacity()));
        assertEquals(3, connection.seq().length(), "Connection must contain 3 tanks. seq=" + connection.seq());

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void interactWithBucket(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.tierToBlock().apply(Tier.WOOD));
        placeTank(helper, pos.above(), ModObjects.tierToBlock().apply(Tier.WOOD));
        var tank = (TileTank) helper.getBlockEntity(pos);
        assertNotNull(tank, "Tank should not be null");

        var player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        var result = helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.NORTH, helper.absolutePos(pos), true));
        assertTrue(result.consumesAction(), "Interact must success.");
        assertTrue(tank.connection().getFluidStack().filter(f -> FluidAmount.BUCKET_WATER().equals(f)).isDefined(), "Connection must have 1 bucket of Water");

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void dummy(GameTestHelper helper) {
        // fail("Fail Test");
        helper.succeed();
    }
}
