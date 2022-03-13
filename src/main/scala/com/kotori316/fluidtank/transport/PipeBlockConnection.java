package com.kotori316.fluidtank.transport;

import java.util.Arrays;

import net.minecraft.util.StringRepresentable;

public enum PipeBlockConnection implements StringRepresentable {
    NO_CONNECTION,
    CONNECTED,
    INPUT,
    OUTPUT;

    public String getName() {
        return name().toLowerCase();
    }

    public boolean is(PipeBlockConnection c1, PipeBlockConnection... cs) {
        return this == c1 || Arrays.asList(cs).contains(this);
    }

    public boolean hasConnection() {
        return is(CONNECTED, INPUT, OUTPUT);
    }

    public boolean isOutput() {
        return is(OUTPUT);
    }

    public boolean isInput() {
        return is(INPUT);
    }

    public static PipeBlockConnection onOffConnection(PipeBlockConnection now) {
        if (now == NO_CONNECTION)
            return CONNECTED;
        else
            return NO_CONNECTION;
    }

    @Override
    public String getSerializedName() {
        return getName();
    }
}
