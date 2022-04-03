package com.kotori316.fluidtank.integration.jade;

import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.tiles.TileTank;

/*
    Use java to avoid strange java.lang.VerifyError.
 */
@WailaPlugin
public class TankWailaPlugin implements IWailaPlugin {

    static final String TIER = "fluidtank.waila.tier";
    static final String CONTENT = "fluidtank.waila.content";
    static final String AMOUNT = "fluidtank.waila.amount";
    static final String CAPACITY = "fluidtank.waila.capacity";
    static final String COMPARATOR = "fluidtank.waila.comparator";
    static final String WAILA_TANK_INFO = "fluidtank.waila.tank_info";
    static final String WAILA_SHORT_INFO = "fluidtank.waila.short_info";
    static final String WAILA_SHORT = "fluidtank.waila.short";
    static final String FLUID_NULL = "None";
    static final String NBT_Tier = TileTank.NBT_Tier();
    static final String NBT_ConnectionAmount = "ConnectionAmount";
    static final String NBT_ConnectionCapacity = "ConnectionCapacity";
    static final String NBT_ConnectionComparator = "Comparator";
    static final String NBT_ConnectionFluidName = "FluidName";
    static final String NBT_Creative = "Creative";
    static final ResourceLocation KEY_TANK_INFO = new ResourceLocation(FluidTank.modID, WAILA_TANK_INFO);
    static final ResourceLocation KEY_SHORT_INFO = new ResourceLocation(FluidTank.modID, WAILA_SHORT_INFO);

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void register(IWailaCommonRegistration registrar) {
        TankDataProvider tankDataProvider = new TankDataProvider();
        registrar.registerBlockDataProvider(tankDataProvider, BlockEntity.class);
        registrar.addConfig(KEY_TANK_INFO, true);
        registrar.addConfig(KEY_SHORT_INFO, true);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void registerClient(IWailaClientRegistration registrar) {
        TankDataProvider tankDataProvider = new TankDataProvider();
        registrar.registerComponentProvider(tankDataProvider, TooltipPosition.BODY, BlockTank.class);
    }
}