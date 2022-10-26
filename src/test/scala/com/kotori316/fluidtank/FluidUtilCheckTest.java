package com.kotori316.fluidtank;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.TankHandler;
import com.kotori316.fluidtank.recipes.RecipeInventoryUtil;
import com.kotori316.fluidtank.tiles.Tier;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FluidUtilCheckTest extends BeforeAllTest {
    private static final BlockTank BLOCK_TANK = ModObjects.tierToBlock().apply(Tier.WOOD);

    record ItemTransferLog(int slot, ItemStack stack, boolean add) {
        ItemTransferLog append(ItemTransferLog that) {
            if (this.stack().isEmpty() || that.stack().isEmpty() || ItemStack.isSameItemSameTags(this.stack(), that.stack())) {
                var count = this.stack().getCount() + (add ? 1 : -1) * that.stack().getCount();
                var copy = ItemHandlerHelper.copyStackWithSize(this.stack().isEmpty() ? that.stack() : this.stack(), count);
                return new ItemTransferLog(slot, copy, true);
            } else {
                return new ItemTransferLog(slot, ItemStack.EMPTY, true);
            }
        }
    }

    private static class ItemHandler implements IItemHandlerModifiable {
        List<ItemTransferLog> logs = new ArrayList<>();

        ItemStack getSum(int slot) {
            return logs.stream()
                .filter(l -> l.slot() == slot)
                .reduce(ItemTransferLog::append)
                .map(ItemTransferLog::stack)
                .orElse(ItemStack.EMPTY);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            logs.add(new ItemTransferLog(slot, stack, true));
        }

        @Override
        public int getSlots() {
            return 5;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return getSum(slot);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            var inv = getSum(slot);
            if (inv.isEmpty() || ItemStack.isSameItemSameTags(inv, stack)) {
                if (!simulate) {
                    logs.add(new ItemTransferLog(slot, stack.copy(), true));
                }
                return ItemStack.EMPTY;
            } else {
                return stack;
            }
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            var stack = getSum(slot);
            var move = Math.min(amount, stack.getCount());
            var moved = ItemHandlerHelper.copyStackWithSize(stack, move);
            if (!simulate) {
                logs.add(new ItemTransferLog(slot, moved, false));
            }
            return moved;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }
    }

    @Test
    void tryTransfer() {
        var tankStack = new ItemStack(BLOCK_TANK);
        var tank = TankHandler.apply(5000L);
        RecipeInventoryUtil.getFluidHandler(tankStack).fill(FluidAmount.toStack(FluidAmount.BUCKET_WATER()), IFluidHandler.FluidAction.EXECUTE);

        var container = ItemHandlerHelper.copyStackWithSize(tankStack, 1);
        FluidStack transfer = FluidUtil.tryFluidTransfer(tank, RecipeInventoryUtil.getFluidHandler(container), 1000, false);
        assertEquals(FluidAmount.BUCKET_WATER(), RecipeInventoryUtil.getFluidHandler(tankStack).getFluid());
        assertEquals(Fluids.EMPTY, tank.getTank().content());
        assertEquals(0L, tank.getTank().amount());
        assertEquals(FluidAmount.BUCKET_WATER(), FluidAmount.fromStack(transfer));
    }

    @Test
    void insert1StackSimulate() {
        var tankStack = new ItemStack(BLOCK_TANK);
        var tank = TankHandler.apply(5000L);
        var inv = new ItemHandler();
        RecipeInventoryUtil.getFluidHandler(tankStack).fill(FluidAmount.toStack(FluidAmount.BUCKET_WATER()), IFluidHandler.FluidAction.EXECUTE);

        assertTrue(tank.getTank().isEmpty(), "Content: %s".formatted(tank.getTank())); // Make sure the tank is empty before filling.
        var result = FluidUtil.tryEmptyContainerAndStow(tankStack, tank, inv, 1000, null, false);
        assertEquals(FluidAmount.BUCKET_WATER(), RecipeInventoryUtil.getFluidHandler(tankStack).getFluid());
        assertTrue(RecipeInventoryUtil.getFluidHandler(result.getResult()).getFluid().nonEmpty());
        assertTrue(result.isSuccess());
        assertTrue(tank.getTank().isEmpty(), "Content: %s".formatted(tank.getTank())); // The tank must be empty after simulating, but not.
    }

    @Test
    void insert1StackExecute() {
        var tankStack = new ItemStack(BLOCK_TANK);
        var tank = TankHandler.apply(5000L);
        var inv = new ItemHandler();
        RecipeInventoryUtil.getFluidHandler(tankStack).fill(FluidAmount.toStack(FluidAmount.BUCKET_WATER()), IFluidHandler.FluidAction.EXECUTE);

        var result = FluidUtil.tryEmptyContainerAndStow(tankStack, tank, inv, 1000, null, true);
        assertAll(
            () -> assertEquals(FluidAmount.BUCKET_WATER(), RecipeInventoryUtil.getFluidHandler(tankStack).getFluid()), // Input will not be changed.
            () -> assertTrue(RecipeInventoryUtil.getFluidHandler(result.getResult()).getFluid().isEmpty()),
            () -> assertTrue(result.isSuccess()),
            () -> assertEquals(FluidAmount.BUCKET_WATER(), tank.getTank().fluidAmount())
        );
    }
}
