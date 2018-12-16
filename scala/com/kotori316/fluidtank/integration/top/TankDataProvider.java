package com.kotori316.fluidtank.integration.top;

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
import com.kotori316.fluidtank.integration.Localize;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

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
            String tier = I18n.translateToLocalFormatted(Localize.WAILA_TIER, tank.tier().toString());
            String fluid = I18n.translateToLocalFormatted(Localize.WAILA_CONTENT,
                Utils.toJava(tank.connection().getFluidStack()).map(FluidStack::getLocalizedName).orElse("Null"));
            if (tank.connection().hasCreative()) {
                probeInfo.text(tier).text(fluid);
            } else {
                String amount = I18n.translateToLocalFormatted(Localize.WAILA_AMOUNT, tank.connection().amount());
                String capacity = I18n.translateToLocalFormatted(Localize.WAILA_CAPACITY, tank.connection().capacity());
                String comparator = I18n.translateToLocalFormatted(Localize.WAILA_COMPARATOR, tank.getComparatorLevel());
                probeInfo.text(tier).text(fluid).text(amount).text(capacity).text(comparator);
            }
        }
    }
}
