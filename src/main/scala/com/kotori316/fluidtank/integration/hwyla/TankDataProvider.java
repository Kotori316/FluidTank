package com.kotori316.fluidtank.integration.hwyla;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.tiles.Tiers;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

import static com.kotori316.fluidtank.integration.Localize.FLUID_NULL;
import static com.kotori316.fluidtank.integration.Localize.WAILA_AMOUNT;
import static com.kotori316.fluidtank.integration.Localize.WAILA_CAPACITY;
import static com.kotori316.fluidtank.integration.Localize.WAILA_COMPARATOR;
import static com.kotori316.fluidtank.integration.Localize.WAILA_CONTENT;
import static com.kotori316.fluidtank.integration.Localize.WAILA_SHORT;
import static com.kotori316.fluidtank.integration.Localize.WAILA_TIER;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.KEY_SHORT_INFO;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.KEY_TANK_INFO;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_ConnectionAmount;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_ConnectionCapacity;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_ConnectionComparator;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_ConnectionFluidName;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_NonCreative;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_Tier;

public class TankDataProvider implements IServerDataProvider<TileEntity>, IComponentProvider {

    private static final Predicate<Object> NOT_EMPTY = Predicate.isEqual(FLUID_NULL).negate();

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        TileEntity t = accessor.getTileEntity();
        if (t instanceof TileTankNoDisplay && config.get(KEY_TANK_INFO)) {
            TileTankNoDisplay tank = ((TileTankNoDisplay) t);
            CompoundNBT nbtData = accessor.getServerData();
            List<? extends ITextComponent> list;
            if (!config.get(KEY_SHORT_INFO)) {
                Tiers tier = tank.tier();
                if (nbtData.getBoolean(NBT_NonCreative)) {
                    list = Arrays.asList(
                        new TranslationTextComponent(WAILA_TIER, tier.toString()),
                        new TranslationTextComponent(WAILA_CONTENT, nbtData.getString(NBT_ConnectionFluidName)),
                        new TranslationTextComponent(WAILA_AMOUNT, nbtData.getLong(NBT_ConnectionAmount)),
                        new TranslationTextComponent(WAILA_CAPACITY, nbtData.getLong(NBT_ConnectionCapacity)),
                        new TranslationTextComponent(WAILA_COMPARATOR, nbtData.getInt(NBT_ConnectionComparator))
                    );
                } else {
                    String fluidName = getCreativeFluidName(tank);
                    list = Arrays.asList(
                        new TranslationTextComponent(WAILA_TIER, tier.toString()),
                        new TranslationTextComponent(WAILA_CONTENT, fluidName)
                    );
                }
            } else {
                if (nbtData.getBoolean(NBT_NonCreative)) {
                    list = Collections.singletonList(
                        new TranslationTextComponent(WAILA_SHORT,
                            nbtData.getString(NBT_ConnectionFluidName),
                            nbtData.getLong(NBT_ConnectionAmount),
                            nbtData.getLong(NBT_ConnectionCapacity))
                    );
                } else {
                    String fluidName = getCreativeFluidName(tank);
                    list = java.util.Optional.of(fluidName)
                        .filter(NOT_EMPTY)
                        .map(StringTextComponent::new)
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
                }
            }

            tooltip.addAll(list);
        }
    }

    @Nonnull
    private static String getCreativeFluidName(TileTankNoDisplay tank) {
        return java.util.Optional.ofNullable(tank.tank().getFluid()).filter(FluidAmount::nonEmpty).map(FluidAmount::getLocalizedName).orElse(FLUID_NULL);
    }

    @Override
    public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te) {
        if (te instanceof TileTankNoDisplay) {
            TileTankNoDisplay tank = (TileTankNoDisplay) te;

            tag.putString(NBT_Tier, tank.tier().toString());
            tag.putString(NBT_ConnectionFluidName,
                Utils.toJava(tank.connection().getFluidStack()).filter(FluidAmount::nonEmpty).map(FluidAmount::getLocalizedName).orElse(FLUID_NULL));
            if (!tank.connection().hasCreative()) {
                tag.putBoolean(NBT_NonCreative, true);
                tag.putLong(NBT_ConnectionAmount, tank.connection().amount());
                tag.putLong(NBT_ConnectionCapacity, tank.connection().capacity());
                tag.putInt(NBT_ConnectionComparator, tank.getComparatorLevel());
            }
        }
    }
}
