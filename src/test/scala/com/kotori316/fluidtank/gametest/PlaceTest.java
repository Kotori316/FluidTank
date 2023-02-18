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

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.blocks.TankPos;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.fluidtank.tiles.TileTank;

import static com.kotori316.fluidtank.gametest.Utils.getConnection;
import static com.kotori316.fluidtank.gametest.Utils.placeTank;

public final class PlaceTest implements FabricGameTest {

    BlockTank woodTank = ModObjects.tierToBlock().apply(Tier.WOOD);
    BlockTank stoneTank = ModObjects.tierToBlock().apply(Tier.STONE);

    @GameTest(template = EMPTY_STRUCTURE)
    public void placeStone(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), Blocks.STONE);
        helper.assertBlock(new BlockPos(0, 1, 0), Predicate.isEqual(Blocks.STONE), "Stone");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void placeTank1(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, woodTank);
        var entity = helper.getBlockEntity(BlockPos.ZERO);
        if (entity instanceof TileTank tank) {
            var connection = tank.connection();
            assert connection.isDummy() : "Connection before initialization must be invalid. " + connection;
        } else {
            throw new GameTestAssertException("Expected TileTank but " + entity);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void placeTank2(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, woodTank);
        var entity = helper.getBlockEntity(BlockPos.ZERO);
        if (entity instanceof TileTank tank) {
            tank.onBlockPlacedBy();
            var connection = tank.connection();
            assert connection.capacity() == Tier.WOOD.amount() : "Capacity of Wood Tank is 4000. " + connection;
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
                helper.setBlock(pos, woodTank);
                // Make unloaded flag on.
                Optional.ofNullable((TileTank) helper.getBlockEntity(pos))
                    .ifPresent(t -> t.load(t.saveWithoutMetadata()));
            })
            .thenIdle(2)
            .thenExecute(() -> {
                var c = getConnection(helper, pos);
                assert c.capacity() == 4000L : "Connection is not created. " + c;
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void place2Tanks(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, woodTank);
        var tank = (TileTank) helper.getBlockEntity(pos);
        assert tank != null : "Tank must not be null. %s at %s"
            .formatted(helper.getBlockState(pos), pos);
        placeTank(helper, pos.above(), woodTank);

        assert tank.connection().capacity() == 8000 : "Wood + Wood, " + tank.connection();
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void place2Tanks2(GameTestHelper helper) {
        helper.startSequence()
            .thenWaitUntil(() -> placeTank(helper, BlockPos.ZERO, woodTank))
            .thenIdle(2)
            .thenWaitUntil(() -> placeTank(helper, BlockPos.ZERO.above(), stoneTank))
            .thenExecuteAfter(2, () -> helper.assertBlockPresent(woodTank, BlockPos.ZERO))
            .thenWaitUntil(() -> helper.assertBlockPresent(stoneTank, BlockPos.ZERO.above()))
            .thenIdle(2)
            .thenWaitUntil(() -> {
                var tank1 = (TileTank) helper.getBlockEntity(BlockPos.ZERO);
                var tank2 = (TileTank) helper.getBlockEntity(BlockPos.ZERO.above());
                assert tank1 != null && tank2 != null : "Tanks must not be null";
                assert tank1.connection() == tank2.connection() :
                    "Connection of tanks must be same instance. %s, %s".formatted(tank1.connection(), tank2.connection());
                assert tank1.connection().capacity() == 20000 :
                    "Tank capacity must be Wood + Stone. %s, %s".formatted(tank1.internalTank(), tank2.internalTank());
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void place3Tanks1(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var block = stoneTank;
        List.of(pos, pos.above(1), pos.above(2)).forEach(p -> placeTank(helper, p, block));

        var tile = (TileTank) Objects.requireNonNull(helper.getBlockEntity(pos));
        var connection = tile.connection();
        assert connection.capacity() == tile.internalTank().getTank().capacityInForge() * 3L :
            "Tank capacity must be 3 times of each tank. Tank=%d, Connection=%d".formatted(tile.internalTank().getTank().capacityInForge(), connection.capacity());
        assert 3 == connection.seq().length() : "Connection must contain 3 tanks. seq=" + connection.seq();

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void interactWithBucket(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, woodTank);
        placeTank(helper, pos.above(), woodTank);
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

    @GameTest(template = EMPTY_STRUCTURE)
    public void removeMiddleTank(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenExecute(() -> {
                for (int i = 0; i < 3; i++) {
                    helper.setBlock(pos.above(i), ModObjects.tierToBlock().apply(Tier.WOOD));
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
