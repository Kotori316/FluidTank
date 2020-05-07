package com.kotori316.fluidtank.tiles;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.FluidStack;

import com.kotori316.fluidtank.FluidAmount;

public class CapabilityFluidTank implements Capability.IStorage<FluidAmount.Tank>, Callable<FluidAmount.Tank> {

    @CapabilityInject(FluidAmount.Tank.class)
    public static Capability<FluidAmount.Tank> cap = null;

    @Override
    public FluidAmount.Tank call() {
        return new Tank();
    }

    @Nullable
    @Override
    public INBT writeNBT(Capability<FluidAmount.Tank> capability, FluidAmount.Tank instance, Direction side) {
        CompoundNBT tag = new CompoundNBT();
        FluidAmount drained = instance.drain(FluidAmount.EMPTY().setAmount(Long.MAX_VALUE), false, 0);
        drained.write(tag);
        return tag;
    }

    @Override
    public void readNBT(Capability<FluidAmount.Tank> capability, FluidAmount.Tank instance, Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            CompoundNBT tag = (CompoundNBT) nbt;
            FluidAmount toFill = FluidAmount.fromNBT(tag);
            instance.fill(toFill, true, 0);
        }
    }

    public static void register() {
        CapabilityFluidTank x = new CapabilityFluidTank();
        CapabilityManager.INSTANCE.register(FluidAmount.Tank.class, x, x);
    }

    private static class Tank implements FluidAmount.Tank {
        FluidAmount content = FluidAmount.EMPTY();
        final int capacity = 4000;

        @Override
        public FluidAmount fill(FluidAmount fluidAmount, boolean doFill, int min) {
            if (fluidAmount.amount() < min) return FluidAmount.EMPTY();
            if (content.isEmpty()) {
                if (doFill) {
                    content = fluidAmount;
                }
                return fluidAmount;
            } else {
                if (content.fluidEqual(fluidAmount)) {
                    long newAmount = content.amount() + fluidAmount.amount();
                    if (capacity < newAmount) {
                        long inserted = fluidAmount.amount() - (capacity - content.amount());
                        if (doFill) content.setAmount(capacity);
                        return fluidAmount.setAmount(inserted);
                    } else {
                        if (doFill) content = content.$plus(fluidAmount);
                        return fluidAmount;
                    }
                } else {
                    return FluidAmount.EMPTY();
                }
            }
        }

        @Override
        public FluidAmount drain(FluidAmount fluidAmount, boolean doDrain, int min) {
            if (fluidAmount.amount() < min) return FluidAmount.EMPTY();
            if (fluidAmount.fluidEqual(FluidAmount.EMPTY())) {
                FluidAmount drained = content.setAmount(Math.min(content.amount(), fluidAmount.amount()));
                if (doDrain) content = content.$minus(drained);
                return drained;
            } else {
                if (content.fluidEqual(fluidAmount)) {
                    FluidAmount drained = content.setAmount(Math.min(content.amount(), fluidAmount.amount()));
                    if (doDrain) content = content.$minus(drained);
                    return drained;
                } else {
                    return FluidAmount.EMPTY();
                }
            }
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return content.toStack();
        }

        @Override
        public int getTankCapacity(int tank) {
            return capacity;
        }
    }

    public static class EmptyTank implements FluidAmount.Tank {
        public static final EmptyTank INSTANCE = new EmptyTank();

        @Override
        public FluidAmount fill(FluidAmount fluidAmount, boolean doFill, int min) {
            return FluidAmount.EMPTY();
        }

        @Override
        public FluidAmount drain(FluidAmount fluidAmount, boolean doDrain, int min) {
            return FluidAmount.EMPTY();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
