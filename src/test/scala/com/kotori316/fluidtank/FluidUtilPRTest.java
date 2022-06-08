package com.kotori316.fluidtank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FluidUtilPRTest {
    @Test
    @DisplayName("tryEmptyContainer should do simulated filling.")
    @Disabled
    void tryEmptyContainerSimulate() {
        var bucket = new ItemStack(Items.WATER_BUCKET);
        var tank = new FluidTank(1000);
        assertTrue(tank.isEmpty());

        FluidActionResult result = FluidUtil.tryEmptyContainer(bucket, tank, 1000, /*player*/ null, /*doDrain*/ false);
        assertTrue(result.isSuccess(), "Insertion must success, but it failed."); // Pass
        assertEquals(Items.BUCKET, result.getResult().getItem(), "Result item isn't empty bucket. " + result.getResult()); // Pass
        assertTrue(tank.isEmpty(), "Tank has %s@%dmb".formatted(tank.getFluid().getFluid(), tank.getFluid().getAmount())); // Fail
    }

    @Test
    void tryEmptyContainerExecute() {
        var bucket = new ItemStack(Items.WATER_BUCKET);
        var tank = new FluidTank(1000);
        assertTrue(tank.isEmpty());

        FluidActionResult result = FluidUtil.tryEmptyContainer(bucket, tank, 1000, /*player*/ null, /*doDrain*/ true);
        assertTrue(result.isSuccess(), "Insertion must success, but it failed.");
        assertEquals(Items.BUCKET, result.getResult().getItem(), "Result item isn't empty bucket. " + result.getResult());
        FluidStack content = tank.getFluid();
        assertTrue(content.isFluidStackIdentical(new FluidStack(Fluids.WATER, 1000)), "Tank has %s@%dmb".formatted(content.getFluid(), content.getAmount()));
    }

    @Test
    void improvedTryEmptyContainerSimulate() {
        var bucket = new ItemStack(Items.WATER_BUCKET);
        var tank = new FluidTank(1000);
        assertTrue(tank.isEmpty());

        FluidActionResult result = FluidUtilPRTest.tryEmptyContainer(bucket, tank, 1000, /*player*/ null, /*doDrain*/ false);
        assertTrue(result.isSuccess(), "Insertion must success, but it failed.");
        assertEquals(Items.BUCKET, result.getResult().getItem(), "Result item isn't empty bucket. " + result.getResult());
        assertTrue(tank.isEmpty(), "Tank has %s@%dmb".formatted(tank.getFluid().getFluid(), tank.getFluid().getAmount()));
        assertEquals(Items.WATER_BUCKET, bucket.getItem(), "Original stack must not be changed");
    }

    @Test
    void improvedTryEmptyContainerExecute() {
        var bucket = new ItemStack(Items.WATER_BUCKET);
        var tank = new FluidTank(1000);
        assertTrue(tank.isEmpty());

        FluidActionResult result = FluidUtilPRTest.tryEmptyContainer(bucket, tank, 1000, /*player*/ null, /*doDrain*/ true);
        assertTrue(result.isSuccess(), "Insertion must success, but it failed.");
        assertEquals(Items.BUCKET, result.getResult().getItem(), "Result item isn't empty bucket. " + result.getResult());
        FluidStack content = tank.getFluid();
        assertTrue(content.isFluidStackIdentical(new FluidStack(Fluids.WATER, 1000)), "Tank has %s@%dmb".formatted(content.getFluid(), content.getAmount()));
        assertEquals(Items.WATER_BUCKET, bucket.getItem(), "Original stack must not be changed");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10, 500, 999})
    void improvedFailsToFillingFullTank(int tankCapacity) {
        var bucket = new ItemStack(Items.WATER_BUCKET);
        var tank = new FluidTank(tankCapacity);
        assertTrue(tank.isEmpty());

        FluidActionResult simulationResult = FluidUtilPRTest.tryEmptyContainer(bucket, tank, 1000, /*player*/ null, /*doDrain*/ false);
        assertFalse(simulationResult.isSuccess());
        FluidActionResult executionResult = FluidUtilPRTest.tryEmptyContainer(bucket, tank, 1000, /*player*/ null, /*doDrain*/ true);
        assertFalse(executionResult.isSuccess());
    }

    @Test
    void improvedFailsToFillingLavaTank() {
        var bucket = new ItemStack(Items.WATER_BUCKET);
        var tank = new FluidTank(4000);
        tank.setFluid(new FluidStack(Fluids.LAVA, 1000));

        FluidActionResult simulationResult = FluidUtilPRTest.tryEmptyContainer(bucket, tank, 1000, /*player*/ null, /*doDrain*/ false);
        assertFalse(simulationResult.isSuccess());
        FluidActionResult executionResult = FluidUtilPRTest.tryEmptyContainer(bucket, tank, 1000, /*player*/ null, /*doDrain*/ true);
        assertFalse(executionResult.isSuccess());
    }

    @Nonnull
    public static FluidActionResult tryEmptyContainer(@Nonnull ItemStack container, IFluidHandler fluidDestination, int maxAmount, @Nullable Player player, boolean doDrain) {
        ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1); // do not modify the input
        return FluidUtil.getFluidHandler(containerCopy)
            .map(containerFluidHandler -> {

                FluidStack content = containerFluidHandler.drain(maxAmount, IFluidHandler.FluidAction.SIMULATE);
                if (content.isEmpty())
                    return FluidActionResult.FAILURE;
                int fillableAmount = fluidDestination.fill(content, IFluidHandler.FluidAction.SIMULATE);
                if (fillableAmount < 0)
                    return FluidActionResult.FAILURE;
                content.setAmount(fillableAmount);

                // We are acting on a COPY of the stack, so performing changes is acceptable even if we are simulating.
                FluidStack transfer = containerFluidHandler.drain(content, IFluidHandler.FluidAction.EXECUTE);
                if (transfer.isEmpty())
                    return FluidActionResult.FAILURE;
                transfer.setAmount(fluidDestination.fill(transfer, doDrain ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE));
                if (transfer.isEmpty())
                    return FluidActionResult.FAILURE;

                if (doDrain && player != null) {
                    SoundEvent soundevent = transfer.getFluid().getAttributes().getEmptySound(transfer);
                    player.level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                ItemStack resultContainer = containerFluidHandler.getContainer();
                return new FluidActionResult(resultContainer);
            })
            .orElse(FluidActionResult.FAILURE);
    }
}
