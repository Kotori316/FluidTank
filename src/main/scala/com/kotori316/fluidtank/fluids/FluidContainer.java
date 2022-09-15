package com.kotori316.fluidtank.fluids;

public interface FluidContainer {
    FluidAmount fill(FluidAmount resource, FluidAction action);

    FluidAmount drain(FluidAmount toDrain, FluidAction action);

    default FluidAmount getFluid() {
        return drain(FluidAmount.EMPTY().setAmount(Long.MAX_VALUE), FluidAction.SIMULATE);
    }
}
