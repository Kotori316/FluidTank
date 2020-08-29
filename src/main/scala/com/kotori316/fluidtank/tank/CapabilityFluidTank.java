package com.kotori316.fluidtank.tank;

import alexiil.mc.lib.attributes.ListenerRemovalToken;
import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidInvTankChangeListener;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.impl.EmptyFixedFluidInv;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import com.kotori316.fluidtank.FluidAmount;

public class CapabilityFluidTank {

    private static class Tank implements FluidAmount.Tank {
        FluidAmount content = FluidAmount.EMPTY();
        int capacity = 4000;

        @Override
        public FluidAmount fill(FluidAmount fluidAmount, boolean doFill, long min) {
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
        public FluidAmount drain(FluidAmount fluidAmount, boolean doDrain, long min) {
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

        @Override
        public alexiil.mc.lib.attributes.fluid.amount.FluidAmount getMaxAmount_F(int tank) {
            return alexiil.mc.lib.attributes.fluid.amount.FluidAmount.ofWhole(4);
        }
    }

    public static class EmptyTank implements FluidAmount.Tank {
        public static final EmptyTank INSTANCE = new EmptyTank();

        @Override
        public FluidAmount fill(FluidAmount fluidAmount, boolean doFill, long min) {
            return FluidAmount.EMPTY();
        }

        @Override
        public FluidAmount drain(FluidAmount fluidAmount, boolean doDrain, long min) {
            return FluidAmount.EMPTY();
        }

        @Override
        public boolean isFluidValidForTank(int tank, FluidKey fluid) {
            return false;
        }

        @Override
        public boolean setInvFluid(int tank, FluidVolume to, Simulation simulation) {
            return false;
        }

        @Override
        public FluidVolume getInvFluid(int tank) {
            return FluidVolumeUtil.EMPTY;
        }

        @Override
        public ListenerToken addListener(FluidInvTankChangeListener listener, ListenerRemovalToken removalToken) {
            return EmptyFixedFluidInv.INSTANCE.addListener(listener, removalToken);
        }

        @Override
        public alexiil.mc.lib.attributes.fluid.amount.FluidAmount getMaxAmount_F(int tank) {
            return alexiil.mc.lib.attributes.fluid.amount.FluidAmount.ZERO;
        }
    }
}
