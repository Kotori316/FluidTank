package com.kotori316.fluidtank.fluids;

public enum FluidAction {
    SIMULATE, EXECUTE;

    public boolean execute() {
        return this == EXECUTE;
    }
}
