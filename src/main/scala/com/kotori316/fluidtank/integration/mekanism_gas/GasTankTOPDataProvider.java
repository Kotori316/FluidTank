package com.kotori316.fluidtank.integration.mekanism_gas;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.ModList;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.integration.top.FluidTankTOPPlugin;

public final class GasTankTOPDataProvider implements IProbeInfoProvider {
    private final Lazy<Boolean> showTOP = Lazy.of(Config.content().showTOP());
    private final Lazy<Boolean> useShort = Lazy.of(Config.content().topShort());
    private final Lazy<Boolean> compact = Lazy.of(Config.content().topCompact());

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(FluidTank.modID, "gas_tank_top");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
        if (!showTOP.get() && !ModList.get().isLoaded("mekanism")) return;
        var entity = level.getBlockEntity(data.getPos());
        if (entity instanceof TileGasTank gasTank) {
            var connection = ((TileInfo.Holder) gasTank.tileInfo().getHolder()).gasConnection;
            var texts = FluidTankTOPPlugin.toInfo(
                compact.get(), useShort.get(), connection.contentType().getLocalizedName(), connection.amount(), connection.capacity(),
                gasTank.tier(), connection.getComparatorLevel(), connection.hasCreative());
            texts.foreach(iProbeInfo::text);
        }
    }
}
