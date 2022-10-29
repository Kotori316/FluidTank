package com.kotori316.fluidtank.integration.mekanism_gas;

import net.minecraftforge.fml.ModList;

final class Constant {
    static final String MEKANISM_ID = "mekanism";

    static boolean isMekanismLoaded() {
        return ModList.get().isLoaded(MEKANISM_ID);
    }
}
