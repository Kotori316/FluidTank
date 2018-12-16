package com.kotori316.fluidtank.integration.hwyla;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

/*
Use java to avoid strange java.lang.VerifyError.
 */
@WailaPlugin
public class TankWailaPlugin implements IWailaPlugin {

    public static final String Waila_ModId = "waila";
    public static final String NBT_Tier = "tier";
    public static final String NBT_ConnectionAmount = "ConnectionAmount";
    public static final String NBT_ConnectionCapacity = "ConnectionCapacity";
    public static final String NBT_ConnectionComparator = "Comparator";
    public static final String NBT_ConnectionFluidName = "FluidName";
    public static final String NBT_NonCreative = "Normal";

    public static final String FLUIDTANK_WAILA_TIER = "fluidtank.waila.tier";
    public static final String WAILA_CONTENT = "fluidtank.waila.content";
    public static final String WAILA_AMOUNT = "fluidtank.waila.amount";
    public static final String WAILA_CAPACITY = "fluidtank.waila.capacity";
    public static final String WAILA_COMPARATOR = "fluidtank.waila.comparator";
    public static final String WAILA_TANKINFO = "fluidtank.waila.tankinfo";

    @Override
    public void register(IWailaRegistrar registrar) {
        TankDataProvider provider = new TankDataProvider();
        registrar.registerBodyProvider(provider, TileTankNoDisplay.class);
        registrar.registerNBTProvider(provider, TileTankNoDisplay.class);

        registrar.addConfig(FluidTank.MOD_NAME, WAILA_TANKINFO, true);
    }
}
