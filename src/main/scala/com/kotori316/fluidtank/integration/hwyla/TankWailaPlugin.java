package com.kotori316.fluidtank.integration.hwyla;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.util.ResourceLocation;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.integration.Localize;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

/*
    Use java to avoid strange java.lang.VerifyError.
 */
@WailaPlugin
public class TankWailaPlugin implements IWailaPlugin {

    static final String Waila_ModId = "waila";
    static final String NBT_Tier = TileTankNoDisplay.NBT_Tier();
    static final String NBT_ConnectionAmount = "ConnectionAmount";
    static final String NBT_ConnectionCapacity = "ConnectionCapacity";
    static final String NBT_ConnectionComparator = "Comparator";
    static final String NBT_ConnectionFluidName = "FluidName";
    static final String NBT_NonCreative = "Normal";
    public static final ResourceLocation KEY_TANK_INFO = new ResourceLocation(FluidTank.modID, Localize.WAILA_TANK_INFO);
    public static final ResourceLocation KEY_SHORT_INFO = new ResourceLocation(FluidTank.modID, Localize.WAILA_SHORT_INFO);

    @Override
    public void register(IRegistrar registrar) {
        if (Config.content().enableWailaAndTOP().get()) {
            TankDataProvider provider = new TankDataProvider();
            registrar.registerBlockDataProvider(provider, TileTankNoDisplay.class);
            registrar.registerComponentProvider(provider, TooltipPosition.BODY, TileTankNoDisplay.class);

            registrar.addConfig(KEY_TANK_INFO, true);
            registrar.addConfig(KEY_SHORT_INFO, true);
        }
    }
}
