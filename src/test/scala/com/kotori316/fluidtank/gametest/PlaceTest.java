package com.kotori316.fluidtank.gametest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

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
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.Connection;
import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.fluidtank.tiles.TileTank;

import static com.kotori316.fluidtank.gametest.Utils.EMPTY_STRUCTURE;
import static com.kotori316.fluidtank.gametest.Utils.assertTrue;
import static com.kotori316.fluidtank.gametest.Utils.getConnection;
import static com.kotori316.fluidtank.gametest.Utils.placeTank;

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
public final class PlaceTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void placeStone(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), Blocks.STONE);
        helper.assertBlock(new BlockPos(0, 1, 0), Predicate.isEqual(Blocks.STONE), "Stone");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void placeTank1(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, ModObjects.blockTanks().head());
        var entity = helper.getBlockEntity(BlockPos.ZERO);
        if (entity instanceof TileTank tank) {
            var connection = tank.connection();
            assertTrue(connection.isDummy(), "Connection before initialization must be invalid. " + connection);
        } else {
            throw new GameTestAssertException("Expected TileTank but " + entity);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void placeTank2(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, ModObjects.blockTanks().head());
        var entity = helper.getBlockEntity(BlockPos.ZERO);
        if (entity instanceof TileTank tank) {
            tank.onBlockPlacedBy();
            var connection = tank.connection();
            assertTrue(connection.capacity() == Tier.WOOD.amount(), "Capacity of Wood Tank is 4000. " + connection);
            assertTrue(connection.amount() == 0, "Amount must be 0.");
            assertTrue(connection.getFluidStack().isEmpty(), "Fluid must be empty. " + connection.getFluidStack());
        } else {
            throw new GameTestAssertException("Expected TileTank but " + entity);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void placeTank3(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenExecute(() -> placeTank(helper, pos.above(), ModObjects.blockTanks().head()))
            .thenIdle(2)
            .thenExecute(() -> {
                var c = getConnection(helper, pos.above());
                assertTrue(c.capacity() == 4000L, "Connection is not created.");
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void place2Tanks(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.blockTanks().head());
        var tank = (TileTank) helper.getBlockEntity(pos);
        assertTrue(tank != null, "Tank must not be null. %s at %s".formatted(helper.getBlockState(pos), pos));
        placeTank(helper, pos.above(), ModObjects.blockTanks().head());

        assertTrue(tank.connection().capacity() == 8000, "Wood + Wood, " + tank.connection());
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void place2Tanks2(GameTestHelper helper) {
        helper.startSequence()
            .thenWaitUntil(() -> placeTank(helper, BlockPos.ZERO, ModObjects.blockTanks().head()))
            .thenIdle(2)
            .thenWaitUntil(() -> placeTank(helper, BlockPos.ZERO.above(), ModObjects.blockTanks().apply(1)))
            .thenExecuteAfter(2, () -> helper.assertBlockPresent(ModObjects.blockTanks().head(), BlockPos.ZERO))
            .thenWaitUntil(() -> helper.assertBlockPresent(ModObjects.blockTanks().apply(1), BlockPos.ZERO.above()))
            .thenIdle(2)
            .thenWaitUntil(() -> {
                var tank1 = (TileTank) helper.getBlockEntity(BlockPos.ZERO);
                var tank2 = (TileTank) helper.getBlockEntity(BlockPos.ZERO.above());
                assertTrue(tank1 != null && tank2 != null, "Tanks must not be null");
                assertTrue(tank1.connection() == tank2.connection(),
                    "Connection of tanks must be same instance. %s, %s".formatted(tank1.connection(), tank2.connection()));
                assertTrue(tank1.connection().capacity() == 20000,
                    "Tank capacity must be Wood + Stone. %s, %s".formatted(tank1.internalTank().getTank(), tank2.internalTank().getTank()));
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void place3Tanks1(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var block = ModObjects.blockTanks().apply(2);
        List.of(pos, pos.above(1), pos.above(2)).forEach(p -> placeTank(helper, p, block));

        var tile = (TileTank) Objects.requireNonNull(helper.getBlockEntity(pos));
        var connection = tile.connection();
        assertTrue(connection.capacity() == tile.internalTank().getTank().capacity() * 3L,
            "Tank capacity must be 3 times of each tank. Tank=%d, Connection=%d".formatted(tile.internalTank().getTank().capacity(), connection.capacity()));
        assertTrue(3 == connection.seq().length(), "Connection must contain 3 tanks. seq=" + connection.seq());

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void interactWithBucket(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.blockTanks().head());
        placeTank(helper, pos.above(), ModObjects.blockTanks().head());
        var tank = (TileTank) helper.getBlockEntity(pos);
        assertTrue(tank != null, "Tank should not be null");

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
        // assert false : "Fail Test";
        helper.succeed();
    }
}