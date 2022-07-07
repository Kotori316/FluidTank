package com.kotori316.fluidtank.integration.wthit;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import net.minecraft.resources.ResourceLocation;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.integration.Localize;
import com.kotori316.fluidtank.tiles.TileTank;

/*
    Use java to avoid strange java.lang.VerifyError.
 */
public class WthitTankWailaPlugin implements IWailaPlugin {
    static final ResourceLocation KEY_TANK_INFO = new ResourceLocation(FluidTank.modID, Localize.WAILA_TANK_INFO);
    static final ResourceLocation KEY_SHORT_INFO = new ResourceLocation(FluidTank.modID, Localize.WAILA_SHORT_INFO);

    @Override
    public void register(IRegistrar registrar) {
        WthitTankDataProvider tankDataProvider = new WthitTankDataProvider();
        registrar.addBlockData(tankDataProvider, TileTank.class);
        registrar.addComponent(tankDataProvider, TooltipPosition.BODY, BlockTank.class);
        registrar.addConfig(KEY_TANK_INFO, true);
        registrar.addConfig(KEY_SHORT_INFO, true);
    }
}
