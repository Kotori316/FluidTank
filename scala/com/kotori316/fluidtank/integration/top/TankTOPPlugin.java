package com.kotori316.fluidtank.integration.top;

import java.util.function.Function;

import mcjty.theoneprobe.api.ITheOneProbe;

public class TankTOPPlugin implements Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe impl) {
        impl.registerProvider(new TankDataProvider());
        return null;
    }
}
