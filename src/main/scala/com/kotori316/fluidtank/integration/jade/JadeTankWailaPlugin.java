package com.kotori316.fluidtank.integration.jade;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.integration.Localize;
import com.kotori316.fluidtank.tiles.TileTank;

/*
    Use java to avoid strange java.lang.VerifyError.
 */
@WailaPlugin
public class JadeTankWailaPlugin implements IWailaPlugin {

    static final ResourceLocation KEY_TANK_INFO = new ResourceLocation(FluidTank.modID, Localize.WAILA_TANK_INFO);
    static final ResourceLocation KEY_SHORT_INFO = new ResourceLocation(FluidTank.modID, Localize.WAILA_SHORT_INFO);

    @Override
    public void register(IWailaCommonRegistration registration) {
        JadeTankDataProvider tankDataProvider = new JadeTankDataProvider();
        registration.registerBlockDataProvider(tankDataProvider, TileTank.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        JadeTankDataProvider tankDataProvider = new JadeTankDataProvider();
        registration.registerBlockComponent(tankDataProvider, BlockTank.class);
        registration.addConfig(KEY_TANK_INFO, true);
        registration.addConfig(KEY_SHORT_INFO, true);
    }
}
