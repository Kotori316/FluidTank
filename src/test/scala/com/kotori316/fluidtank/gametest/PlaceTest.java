package com.kotori316.fluidtank.gametest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.tank.Connection;
import com.kotori316.fluidtank.tank.Tiers;
import com.kotori316.fluidtank.tank.TileTank;

import static com.kotori316.fluidtank.gametest.Utils.getConnection;
import static com.kotori316.fluidtank.gametest.Utils.placeTank;

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
    public void placeTank3(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenExecute(() -> {
                helper.setBlock(pos, ModTank.Entries.WOOD_TANK);
                // Make unloaded flag on.
                Optional.ofNullable((TileTank) helper.getBlockEntity(pos))
                    .ifPresent(t -> t.load(t.saveWithoutMetadata()));
            })
            .thenIdle(2)
            .thenExecute(() -> {
                var c = getConnection(helper, pos);
                assert c.capacity() == 4000L : "Connection is not created.";
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void place2Tanks(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModTank.Entries.WOOD_TANK);
        var tank = (TileTank) helper.getBlockEntity(pos);
        assert tank != null : "Tank must not be null. %s at %s"
            .formatted(helper.getBlockState(pos), pos);
        placeTank(helper, pos.above(), ModTank.Entries.WOOD_TANK);

        assert tank.connection().capacity() == 8000 : "Wood + Wood, " + tank.connection();
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void place2Tanks2(GameTestHelper helper) {
        helper.startSequence()
            .thenWaitUntil(() -> placeTank(helper, BlockPos.ZERO, ModTank.Entries.WOOD_TANK))
            .thenIdle(2)
            .thenWaitUntil(() -> placeTank(helper, BlockPos.ZERO.above(), ModTank.Entries.TANK_BLOCKS.get(0)))
            .thenExecuteAfter(2, () -> helper.assertBlockPresent(ModTank.Entries.WOOD_TANK, BlockPos.ZERO))
            .thenWaitUntil(() -> helper.assertBlockPresent(ModTank.Entries.TANK_BLOCKS.get(0), BlockPos.ZERO.above()))
            .thenIdle(2)
            .thenWaitUntil(() -> {
                var tank1 = (TileTank) helper.getBlockEntity(BlockPos.ZERO);
                var tank2 = (TileTank) helper.getBlockEntity(BlockPos.ZERO.above());
                assert tank1 != null && tank2 != null : "Tanks must not be null";
                assert tank1.connection() == tank2.connection() :
                    "Connection of tanks must be same instance. %s, %s".formatted(tank1.connection(), tank2.connection());
                assert tank1.connection().capacity() == 20000 :
                    "Tank capacity must be Wood + Stone. %s, %s".formatted(tank1.tank(), tank2.tank());
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void place3Tanks1(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var block = ModTank.Entries.TANK_BLOCKS.get(1);
        List.of(pos, pos.above(1), pos.above(2)).forEach(p -> placeTank(helper, p, block));

        var tile = (TileTank) Objects.requireNonNull(helper.getBlockEntity(pos));
        var connection = tile.connection();
        assert connection.capacity() == tile.tank().capacity() * 3L :
            "Tank capacity must be 3 times of each tank. Tank=%d, Connection=%d".formatted(tile.tank().capacity(), connection.capacity());
        assert 3 == connection.seq().length() : "Connection must contain 3 tanks. seq=" + connection.seq();

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void interactWithBucket(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModTank.Entries.WOOD_TANK);
        placeTank(helper, pos.above(), ModTank.Entries.WOOD_TANK);
        var tank = (TileTank) helper.getBlockEntity(pos);
        assert tank != null;

        var player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        var result = helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.NORTH, helper.absolutePos(pos), true));
        assert result.consumesAction() : "Interact must success.";
        assert tank.connection().getFluidStack().filter(f -> FluidAmount.BUCKET_WATER().equals(f)).isDefined() : "Connection must have 1 bucket of Water";

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void dummy(GameTestHelper helper) {
        // assert false : "Fail Test";
        helper.succeed();
    }
}
