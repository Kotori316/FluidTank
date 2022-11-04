package com.kotori316.fluidtank.integration.mekanism_gas;

import mekanism.api.chemical.gas.IGasHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class Constant {
    static final String MEKANISM_ID = "mekanism";
    static final Logger LOGGER = LogManager.getLogger(Constant.class);

    static boolean isMekanismLoaded() {
        return ModList.get().isLoaded(MEKANISM_ID);
    }

    static final Capability<IGasHandler> GAS_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
}
