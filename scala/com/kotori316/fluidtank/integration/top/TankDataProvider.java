package com.kotori316.fluidtank.integration.top;

import java.util.Arrays;
import java.util.List;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

import static com.kotori316.fluidtank.integration.Localize.*;

public class TankDataProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return FluidTank.modID + ":toptank";
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player,
                             World world, IBlockState blockState, IProbeHitData data) {
        TileEntity entity = world.getTileEntity(data.getPos());
        if (entity instanceof TileTankNoDisplay && Config.content().showTOP()) {
            TileTankNoDisplay tank = (TileTankNoDisplay) entity;
            List<String> list;
            String tier = I18n.translateToLocalFormatted(WAILA_TIER, tank.tier().toString());
            String fluid = I18n.translateToLocalFormatted(WAILA_CONTENT,
                    Utils.toJava(tank.connection().getFluidStack()).map(FluidStack::getLocalizedName).orElse(FLUID_NULL));
            if (tank.connection().hasCreative()) {
                list = Arrays.asList(tier, fluid);
            } else {
                String amount = I18n.translateToLocalFormatted(WAILA_AMOUNT, tank.connection().amount());
                String capacity = I18n.translateToLocalFormatted(WAILA_CAPACITY, tank.connection().capacity());
                String comparator = I18n.translateToLocalFormatted(WAILA_COMPARATOR, tank.getComparatorLevel());
                list = Arrays.asList(tier, fluid, amount, capacity, comparator);
            }
            list.forEach(probeInfo::text);
        }
    }
}
