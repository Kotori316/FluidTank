package com.kotori316.fluidtank.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.fluids.FluidAction;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.fluidtank.tiles.TileTank;

import static com.kotori316.fluidtank.gametest.Utils.placeTank;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class InteractionTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void interactWithBucketToFillCreative(GameTestHelper helper) {
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
    public void interactWithBucketToFillSurvival(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.tierToBlock().apply(Tier.WOOD));
        placeTank(helper, pos.above(), ModObjects.tierToBlock().apply(Tier.WOOD));
        var tank = (TileTank) helper.getBlockEntity(pos);
        assertNotNull(tank, "Tank should not be null");

        var player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        var result = helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.NORTH, helper.absolutePos(pos), true));
        assertTrue(result.consumesAction(), "Interact must success.");
        assertTrue(tank.connection().getFluidStack().filter(f -> FluidAmount.BUCKET_WATER().equals(f)).isDefined(), "Connection must have 1 bucket of Water");
        assertTrue(player.getInventory().contains(new ItemStack(Items.BUCKET)));

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void interactWithStackedBucketCreative(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.tierToBlock().apply(Tier.WOOD));
        var tank = (TileTank) helper.getBlockEntity(pos);
        assertNotNull(tank, "Tank should not be null");

        var player = helper.makeMockPlayer();
        player.getAbilities().instabuild = true; // Required to treat this as creative player in some methods.
        assertTrue(player.isCreative(), "Assumption failed.");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET, 3));
        var hitResult = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.NORTH, helper.absolutePos(pos), true);
        for (int i = 0; i < 3; i++) {
            var result = helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hitResult);
            assertTrue(result.consumesAction(), "Interact must success. Trial %d".formatted(i));
            assertEquals(FluidAmount.BUCKET_WATER().setAmount((long) FluidAmount.AMOUNT_BUCKET() * (i + 1)),
                tank.connection().getFluidStack().get(), "Tank must contain %d Water".formatted(i + 1));
            assertEquals(0, player.getInventory().countItem(Items.BUCKET), "In creative, the stack should not change.");
            assertEquals(3, player.getInventory().countItem(Items.WATER_BUCKET), "In creative, the stack should not change.");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void interactWithStackedBucketSurvival(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.tierToBlock().apply(Tier.WOOD));
        var tank = (TileTank) helper.getBlockEntity(pos);
        assertNotNull(tank, "Tank should not be null");

        var player = helper.makeMockSurvivalPlayer();
        assertFalse(player.isCreative(), "Assumption failed.");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET, 3));
        var hitResult = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.NORTH, helper.absolutePos(pos), true);
        for (int i = 0; i < 3; i++) {
            var result = helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hitResult);
            assertTrue(result.consumesAction(), "Interact must success. Trial %d".formatted(i));
            assertEquals(FluidAmount.BUCKET_WATER().setAmount((long) FluidAmount.AMOUNT_BUCKET() * (i + 1)),
                tank.connection().getFluidStack().get(), "Tank must contain %d Water".formatted(i + 1));
            assertEquals(i + 1, player.getInventory().countItem(Items.BUCKET));
            assertEquals(2 - i, player.getInventory().countItem(Items.WATER_BUCKET));
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void interactWithBucketToDrain1(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.tierToBlock().apply(Tier.WOOD));
        placeTank(helper, pos.above(), ModObjects.tierToBlock().apply(Tier.WOOD));
        var tank = (TileTank) helper.getBlockEntity(pos);
        assertNotNull(tank, "Tank should not be null");
        tank.connection().handler().fill(FluidAmount.BUCKET_WATER().setAmount(2000), FluidAction.EXECUTE);

        var player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        var result = helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.NORTH, helper.absolutePos(pos), true));
        assertEquals(InteractionResult.SUCCESS, result, "Interact must success.");

        assertTrue(tank.connection().getFluidStack().filter(f -> FluidAmount.BUCKET_WATER().equals(f)).isDefined(), "Connection must have 1 bucket of Water");
        assertEquals(1, player.getInventory().countItem(Items.WATER_BUCKET), "Player must have filled bucket.");
        assertEquals(0, player.getInventory().countItem(Items.BUCKET), "Player must have no empty buckets.");

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void interactWithBucketToDrain2(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.tierToBlock().apply(Tier.WOOD));
        placeTank(helper, pos.above(), ModObjects.tierToBlock().apply(Tier.WOOD));
        var tank = (TileTank) helper.getBlockEntity(pos);
        assertNotNull(tank, "Tank should not be null");
        tank.connection().handler().fill(FluidAmount.BUCKET_WATER().setAmount(2000), FluidAction.EXECUTE);

        var player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET, 2));
        var result = helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.NORTH, helper.absolutePos(pos), true));
        assertEquals(InteractionResult.SUCCESS, result, "Interact must success.");

        assertTrue(tank.connection().getFluidStack().filter(f -> FluidAmount.BUCKET_WATER().equals(f)).isDefined(), "Connection must have 1 bucket of Water");
        assertEquals(1000, tank.connection().amount());
        assertEquals(1, player.getInventory().countItem(Items.WATER_BUCKET), "Player must have filled bucket.");
        assertEquals(1, player.getInventory().countItem(Items.BUCKET), "Player must have 1 empty bucket.");

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void interactWithBucketToDrain2Creative(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.tierToBlock().apply(Tier.WOOD));
        placeTank(helper, pos.above(), ModObjects.tierToBlock().apply(Tier.WOOD));
        var tank = (TileTank) helper.getBlockEntity(pos);
        assertNotNull(tank, "Tank should not be null");
        tank.connection().handler().fill(FluidAmount.BUCKET_WATER().setAmount(2000), FluidAction.EXECUTE);

        var player = helper.makeMockPlayer();
        player.getAbilities().instabuild = true; // Required to treat this as creative player in some methods.
        assertTrue(player.isCreative(), "Assumption failed.");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET, 2));
        var result = helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.NORTH, helper.absolutePos(pos), true));
        assertEquals(InteractionResult.SUCCESS, result, "Interact must success.");

        assertTrue(tank.connection().getFluidStack().filter(f -> FluidAmount.BUCKET_WATER().equals(f)).isDefined(), "Connection must have 1 bucket of Water");
        assertEquals(1000, tank.connection().amount());
        assertEquals(0, player.getInventory().countItem(Items.WATER_BUCKET), "Creative: no change allowed.");
        assertEquals(2, player.getInventory().countItem(Items.BUCKET), "In creative, stack should not change.");

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void interactWithBucketToDrain3(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        placeTank(helper, pos, ModObjects.tierToBlock().apply(Tier.WOOD));
        placeTank(helper, pos.above(), ModObjects.tierToBlock().apply(Tier.WOOD));
        var tank = (TileTank) helper.getBlockEntity(pos);
        assertNotNull(tank, "Tank should not be null");
        tank.connection().handler().fill(FluidAmount.BUCKET_WATER().setAmount(2000), FluidAction.EXECUTE);

        var player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET, 2));
        var result1 = helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.NORTH, helper.absolutePos(pos), true));
        assertEquals(InteractionResult.SUCCESS, result1, "Interact must success. 1");
        var result2 = helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.NORTH, helper.absolutePos(pos), true));
        assertEquals(InteractionResult.SUCCESS, result2, "Interact must success. 2");

        assertTrue(tank.connection().getFluidStack().isEmpty(), "Connection must have no fluid.");
        assertEquals(2, player.getInventory().countItem(Items.WATER_BUCKET), "Player must have filled bucket.");
        assertEquals(0, player.getInventory().countItem(Items.BUCKET), "Player must have no empty buckets.");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET, 1));
        assertDoesNotThrow(() -> helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.NORTH, helper.absolutePos(pos), true)));

        helper.succeed();
    }

}
