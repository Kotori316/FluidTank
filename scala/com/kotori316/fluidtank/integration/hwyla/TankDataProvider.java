package com.kotori316.fluidtank.integration.hwyla;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import mcp.mobius.waila.api.ITaggedList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

import static com.kotori316.fluidtank.integration.Localize.*;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.*;

@Optional.Interface(iface = "mcp.mobius.waila.api.IWailaDataProvider", modid = Waila_ModId)
public class TankDataProvider implements IWailaDataProvider {

    private static final Predicate<Object> NOT_EMPTY = Predicate.isEqual(FLUID_NULL).negate();

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = Waila_ModId)
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity t = accessor.getTileEntity();
        if (t instanceof TileTankNoDisplay && config.getConfig(WAILA_TANK_INFO)) {
            removeTag(tooltip, "IFluidHandler");
            NBTTagCompound nbtData = accessor.getNBTData();
            List<String> list;
            if (!config.getConfig(WAILA_SHORT_INFO)) {
                if (nbtData.getBoolean(NBT_NonCreative)) {
                    list = Arrays.asList(
                        I18n.format(WAILA_TIER, nbtData.getString(NBT_Tier)),
                        I18n.format(WAILA_CONTENT, nbtData.getString(NBT_ConnectionFluidName)),
                        I18n.format(WAILA_AMOUNT, nbtData.getLong(NBT_ConnectionAmount)),
                        I18n.format(WAILA_CAPACITY, nbtData.getLong(NBT_ConnectionCapacity)),
                        I18n.format(WAILA_COMPARATOR, nbtData.getInteger(NBT_ConnectionComparator))
                    );
                } else {
                    list = Arrays.asList(
                        new TextComponentTranslation(WAILA_TIER, nbtData.getString(NBT_Tier)).getFormattedText(),
                        new TextComponentTranslation(WAILA_CONTENT, nbtData.getString(NBT_ConnectionFluidName)).getFormattedText()
                    );
                }
            } else {
                if (nbtData.getBoolean(NBT_NonCreative)) {
                    list = Collections.singletonList(
                        new TextComponentTranslation(WAILA_SHORT,
                            nbtData.getString(NBT_ConnectionFluidName),
                            nbtData.getLong(NBT_ConnectionAmount),
                            nbtData.getLong(NBT_ConnectionCapacity)).getFormattedText()
                    );
                } else {
                    list = java.util.Optional.of(nbtData.getString(NBT_ConnectionFluidName))
                        .filter(NOT_EMPTY)
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
                }
            }

            tooltip.addAll(list);
        }
        return tooltip;
    }

    @Nonnull
    @Override
    @Optional.Method(modid = Waila_ModId)
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (te instanceof TileTankNoDisplay) {
            TileTankNoDisplay tank = (TileTankNoDisplay) te;

            tag.setString(NBT_Tier, tank.tier().toString());
            tag.setString(NBT_ConnectionFluidName,
                Utils.toJava(tank.connection().getFluidStack()).map(FluidStack::getLocalizedName).orElse(FLUID_NULL));
            if (!tank.connection().hasCreative()) {
                tag.setBoolean(NBT_NonCreative, true);
                tag.setLong(NBT_ConnectionAmount, tank.connection().amount());
                tag.setLong(NBT_ConnectionCapacity, tank.connection().capacity());
                tag.setInteger(NBT_ConnectionComparator, tank.getComparatorLevel());
            }
        }
        return tag;
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    private static void removeTag(List<?> list, String tag) {
        if (list instanceof ITaggedList) {
            ITaggedList taggedList = (ITaggedList) list;
            taggedList.removeEntries(tag);
        }
    }
}
