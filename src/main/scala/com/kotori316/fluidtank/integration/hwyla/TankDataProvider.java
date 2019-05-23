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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.tiles.Tiers;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

import static com.kotori316.fluidtank.integration.Localize.*;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.*;

//@Optional.Interface(iface = "mcp.mobius.waila.api.IWailaDataProvider", modid = Waila_ModId)
public class TankDataProvider implements IServerDataProvider<TileEntity>, IComponentProvider {

    private static final Predicate<Object> NOT_EMPTY = Predicate.isEqual(FLUID_NULL).negate();

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        TileEntity t = accessor.getTileEntity();
        if (t instanceof TileTankNoDisplay && config.get(KEY_TANK_INFO)) {
            TileTankNoDisplay tank = ((TileTankNoDisplay) t);
            NBTTagCompound nbtData = accessor.getServerData();
            List<? extends ITextComponent> list;
            if (!config.get(KEY_SHORT_INFO)) {
                Tiers tier = tank.tier();
                if (nbtData.getBoolean(NBT_NonCreative)) {
                    list = Arrays.asList(
                        new TextComponentTranslation(WAILA_TIER, tier.toString()),
                        new TextComponentTranslation(WAILA_CONTENT, nbtData.getString(NBT_ConnectionFluidName)),
                        new TextComponentTranslation(WAILA_AMOUNT, nbtData.getLong(NBT_ConnectionAmount)),
                        new TextComponentTranslation(WAILA_CAPACITY, nbtData.getLong(NBT_ConnectionCapacity)),
                        new TextComponentTranslation(WAILA_COMPARATOR, nbtData.getInt(NBT_ConnectionComparator))
                    );
                } else {
                    String fluidName = getCreativeFluidName(tank);
                    list = Arrays.asList(
                        new TextComponentTranslation(WAILA_TIER, tier.toString()),
                        new TextComponentTranslation(WAILA_CONTENT, fluidName)
                    );
                }
            } else {
                if (nbtData.getBoolean(NBT_NonCreative)) {
                    list = Collections.singletonList(
                        new TextComponentTranslation(WAILA_SHORT,
                            nbtData.getString(NBT_ConnectionFluidName),
                            nbtData.getLong(NBT_ConnectionAmount),
                            nbtData.getLong(NBT_ConnectionCapacity))
                    );
                } else {
                    String fluidName = getCreativeFluidName(tank);
                    list = java.util.Optional.of(fluidName)
                        .filter(NOT_EMPTY)
                        .map(TextComponentString::new)
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
    public void appendServerData(NBTTagCompound tag, EntityPlayerMP player, World world, TileEntity te) {
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
