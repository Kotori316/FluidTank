package com.kotori316.fluidtank.integration.wthit;

import java.util.Arrays;
import java.util.Collections;
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
import org.jetbrains.annotations.NotNull;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.fluidtank.tiles.TileTank;
import com.kotori316.fluidtank.tiles.TileTankVoid;

import static com.kotori316.fluidtank.integration.Localize.AMOUNT;
import static com.kotori316.fluidtank.integration.Localize.CAPACITY;
import static com.kotori316.fluidtank.integration.Localize.COMPARATOR;
import static com.kotori316.fluidtank.integration.Localize.CONTENT;
import static com.kotori316.fluidtank.integration.Localize.FLUID_NULL;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionAmount;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionCapacity;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionComparator;
import static com.kotori316.fluidtank.integration.Localize.NBT_ConnectionFluidName;
import static com.kotori316.fluidtank.integration.Localize.NBT_Creative;
import static com.kotori316.fluidtank.integration.Localize.NBT_Tier;
import static com.kotori316.fluidtank.integration.Localize.TIER;
import static com.kotori316.fluidtank.integration.Localize.WAILA_SHORT;

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
            List<? extends Component> list;
            if (config.getBoolean(WthitTankWailaPlugin.KEY_SHORT_INFO)) {
                if (tank instanceof TileTankVoid) {
                    list = Collections.emptyList();
                } else {
                    if (!nbtData.contains(NBT_Creative)) {
                        list = Collections.singletonList(
                            Component.translatable(WAILA_SHORT,
                                tank.internalTank().getTank().fluidAmount().getLocalizedName(),
                                tank.internalTank().getTank().amountInForge(),
                                tank.internalTank().getTank().capacityInForge())
                        );
                    } else if (!nbtData.getBoolean(NBT_Creative)) {
                        list = Collections.singletonList(
                            Component.translatable(WAILA_SHORT,
                                nbtData.getString(NBT_ConnectionFluidName),
                                nbtData.getLong(NBT_ConnectionAmount),
                                nbtData.getLong(NBT_ConnectionCapacity))
                        );
                    } else {
                        String fluidName = getCreativeFluidName(tank);
                        list = java.util.Optional.of(fluidName)
                            .filter(s -> !FLUID_NULL.equals(s))
                            .map(Component::literal)
                            .map(Collections::singletonList)
                            .orElse(Collections.emptyList());
                    }
                }
            } else {
                Tier tier = tank.tier();
                if (tank instanceof TileTankVoid) {
                    list = Collections.singletonList(Component.translatable(TIER, tier.toString()));
                } else {
                    if (!nbtData.getBoolean(NBT_Creative)) {
                        list = Arrays.asList(
                            Component.translatable(TIER, tier.toString()),
                            Component.translatable(CONTENT, nbtData.getString(NBT_ConnectionFluidName)),
                            Component.translatable(AMOUNT, nbtData.getLong(NBT_ConnectionAmount)),
                            Component.translatable(CAPACITY, nbtData.getLong(NBT_ConnectionCapacity)),
                            Component.translatable(COMPARATOR, nbtData.getInt(NBT_ConnectionComparator))
                        );
                    } else {
                        String fluidName = getCreativeFluidName(tank);
                        list = Arrays.asList(
                            Component.translatable(TIER, tier.toString()),
                            Component.translatable(CONTENT, fluidName)
                        );
                    }
                }
            }

            list.forEach(tooltip::addLine);
        }
    }

    @NotNull
    private static String getCreativeFluidName(TileTank tank) {
        return java.util.Optional.ofNullable(tank.internalTank().getTank().fluidAmount()).filter(FluidAmount::nonEmpty).map(FluidAmount::getLocalizedName).orElse(FLUID_NULL);
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
