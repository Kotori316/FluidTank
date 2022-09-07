package com.kotori316.fluidtank.integration.jade;

import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.resources.ResourceLocation;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.integration.Localize;
import com.kotori316.fluidtank.tiles.TileTank;

/*
    Use java to avoid strange java.lang.VerifyError.
 */
@SuppressWarnings("UnstableApiUsage")
@WailaPlugin
public class JadeTankWailaPlugin implements IWailaPlugin {

    static final ResourceLocation KEY_TANK_INFO = new ResourceLocation(FluidTank.modID, Localize.WAILA_TANK_INFO);
    static final ResourceLocation KEY_SHORT_INFO = new ResourceLocation(FluidTank.modID, Localize.WAILA_SHORT_INFO);
    static final ResourceLocation KEY_COMPACT_NUMBER = new ResourceLocation(FluidTank.modID, Localize.WAILA_COMPACT_NUMBER);

    @Override
    public void register(IWailaCommonRegistration registration) {
        if (Config.content().enableWailaAndTOP().get()) {
            JadeTankDataProvider tankDataProvider = new JadeTankDataProvider();
            registration.registerBlockDataProvider(tankDataProvider, TileTank.class);
            registration.addConfig(KEY_TANK_INFO, true);
            registration.addConfig(KEY_SHORT_INFO, true);
            registration.addConfig(KEY_COMPACT_NUMBER, false);
        }
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        if (Config.content().enableWailaAndTOP().get()) {
            JadeTankDataProvider tankDataProvider = new JadeTankDataProvider();
            registration.registerComponentProvider(tankDataProvider, TooltipPosition.BODY, BlockTank.class);
        }
    }
}
