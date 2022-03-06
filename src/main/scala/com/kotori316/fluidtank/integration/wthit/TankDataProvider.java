package com.kotori316.fluidtank.integration.wthit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.tank.Tiers;
import com.kotori316.fluidtank.tank.TileTank;
import com.kotori316.fluidtank.tank.TileTankVoid;

import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.AMOUNT;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.CAPACITY;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.COMPARATOR;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.CONTENT;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.FLUID_NULL;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.KEY_SHORT_INFO;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.KEY_TANK_INFO;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.NBT_ConnectionAmount;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.NBT_ConnectionCapacity;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.NBT_ConnectionComparator;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.NBT_ConnectionFluidName;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.NBT_Creative;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.NBT_Tier;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.TIER;
import static com.kotori316.fluidtank.integration.wthit.TankWailaPlugin.WAILA_SHORT;

public class TankDataProvider implements IServerDataProvider<TileTank>, IBlockComponentProvider {
    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        TileTank tank = accessor.getBlockEntity();
        if (tank != null && config.getBoolean(KEY_TANK_INFO)) {
            CompoundTag nbtData = accessor.getServerData();
            List<? extends Component> list;
            if (config.getBoolean(KEY_SHORT_INFO)) {
                if (tank instanceof TileTankVoid) {
                    list = Collections.emptyList();
                } else {
                    if (!nbtData.contains(NBT_Creative)) {
                        list = Collections.singletonList(
                            new TranslatableComponent(WAILA_SHORT,
                                tank.tank().fluid().getLocalizedName(),
                                tank.tank().getFluidAmount().asLong(FluidAmount.AMOUNT_BUCKET()),
                                tank.tank().capacity())
                        );
                    } else if (!nbtData.getBoolean(NBT_Creative)) {
                        list = Collections.singletonList(
                            new TranslatableComponent(WAILA_SHORT,
                                nbtData.getString(NBT_ConnectionFluidName),
                                nbtData.getLong(NBT_ConnectionAmount),
                                nbtData.getLong(NBT_ConnectionCapacity))
                        );
                    } else {
                        String fluidName = getCreativeFluidName(tank);
                        list = java.util.Optional.of(fluidName)
                            .filter(s -> !FLUID_NULL.equals(s))
                            .map(TextComponent::new)
                            .map(Collections::singletonList)
                            .orElse(Collections.emptyList());
                    }
                }
            } else {
                Tiers tier = tank.tier();
                if (tank instanceof TileTankVoid) {
                    list = Collections.singletonList(new TranslatableComponent(TIER, tier.toString()));
                } else {
                    if (!nbtData.getBoolean(NBT_Creative)) {
                        list = Arrays.asList(
                            new TranslatableComponent(TIER, tier.toString()),
                            new TranslatableComponent(CONTENT, nbtData.getString(NBT_ConnectionFluidName)),
                            new TranslatableComponent(AMOUNT, nbtData.getLong(NBT_ConnectionAmount)),
                            new TranslatableComponent(CAPACITY, nbtData.getLong(NBT_ConnectionCapacity)),
                            new TranslatableComponent(COMPARATOR, nbtData.getInt(NBT_ConnectionComparator))
                        );
                    } else {
                        String fluidName = getCreativeFluidName(tank);
                        list = Arrays.asList(
                            new TranslatableComponent(TIER, tier.toString()),
                            new TranslatableComponent(CONTENT, fluidName)
                        );
                    }
                }
            }

            list.forEach(tooltip::addLine);
        }
    }

    @Nonnull
    private static String getCreativeFluidName(TileTank tank) {
        return java.util.Optional.ofNullable(tank.tank().fluid()).filter(FluidAmount::nonEmpty).map(FluidAmount::getLocalizedName).orElse(FLUID_NULL);
    }

    @Override
    public void appendServerData(CompoundTag tag, IServerAccessor<TileTank> accessor, IPluginConfig config) {
        var tank = accessor.getTarget();
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
