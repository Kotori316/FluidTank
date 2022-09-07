package com.kotori316.fluidtank.integration.wthit;

import java.util.List;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.integration.Localize;
import com.kotori316.fluidtank.tiles.TileTank;
import com.kotori316.fluidtank.tiles.TileTankVoid;

import static com.kotori316.fluidtank.integration.Localize.FLUID_NULL;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionAmount;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionCapacity;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionComparator;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionFluidName;
import static com.kotori316.fluidtank.integration.Localize.NBT_Creative;
import static com.kotori316.fluidtank.integration.Localize.NBT_Tier;

/**
 * Add details of tank with data from server.
 * This class must not contain any fields because the instance is not same in client side and server side.
 * The data should be transferred via packets with NBT, just added to given tag.
 */
final class WthitTankDataProvider implements IServerDataProvider<BlockEntity>, IBlockComponentProvider {
    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof TileTank tank && config.getBoolean(WthitTankWailaPlugin.KEY_TANK_INFO)) {
            CompoundTag nbtData = accessor.getServerData();
            List<? extends Component> list = Localize.getTooltipText(nbtData, tank, config.getBoolean(WthitTankWailaPlugin.KEY_SHORT_INFO), config.getBoolean(WthitTankWailaPlugin.KEY_COMPACT_NUMBER));

            list.forEach(tooltip::addLine);
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, IServerAccessor<BlockEntity> accessor, IPluginConfig config) {
        if (!(accessor.getTarget() instanceof TileTank tank)) return;
        tag.putString(NBT_Tier, tank.tier().toString());
        if (tank instanceof TileTankVoid) return;
        tag.putString(NBT_ConnectionFluidName,
            OptionConverters.toJava(tank.connection().getFluidStack()).filter(FluidAmount::nonEmpty).map(FluidAmount::getLocalizedName).orElse(FLUID_NULL));
        if (!tank.connection().hasCreative()) {
            tag.putBoolean(NBT_Creative, false);
            tag.putLong(NBT_ConnectionAmount, tank.connection().amount());
            tag.putLong(NBT_ConnectionCapacity, tank.connection().capacity());
            tag.putInt(NBT_ConnectionComparator, tank.getComparatorLevel());
        } else {
            tag.putBoolean(NBT_Creative, true);
        }
    }

}
