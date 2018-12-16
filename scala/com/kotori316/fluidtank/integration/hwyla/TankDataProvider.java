package com.kotori316.fluidtank.integration.hwyla;

import java.util.List;

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
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.*;

@Optional.Interface(iface = "mcp.mobius.waila.api.IWailaDataProvider", modid = Waila_ModId)
public class TankDataProvider implements IWailaDataProvider {

    @Nonnull
    @Override
    @Optional.Method(modid = Waila_ModId)
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity t = accessor.getTileEntity();
        if (t instanceof TileTankNoDisplay && config.getConfig(WAILA_TANKINFO)) {
            ITaggedList<String, String> taggedList = (ITaggedList<String, String>) tooltip;
            taggedList.removeEntries("IFluidHandler");
            NBTTagCompound nbtData = accessor.getNBTData();
            if (nbtData.getBoolean(NBT_NonCreative)) {
                taggedList.add(I18n.format(FLUIDTANK_WAILA_TIER, nbtData.getString(NBT_Tier)), FluidTank.MOD_NAME);
                taggedList.add(I18n.format(WAILA_CONTENT, nbtData.getString(NBT_ConnectionFluidName)), FluidTank.MOD_NAME);
                taggedList.add(I18n.format(WAILA_AMOUNT, nbtData.getLong(NBT_ConnectionAmount)), FluidTank.MOD_NAME);
                taggedList.add(I18n.format(WAILA_CAPACITY, nbtData.getLong(NBT_ConnectionCapacity)), FluidTank.MOD_NAME);
                taggedList.add(I18n.format(WAILA_COMPARATOR, nbtData.getInteger(NBT_ConnectionComparator)), FluidTank.MOD_NAME);
            } else {
                taggedList.add(I18n.format(FLUIDTANK_WAILA_TIER, nbtData.getString(NBT_Tier)), FluidTank.MOD_NAME);
                taggedList.add(I18n.format(WAILA_CONTENT, nbtData.getString(NBT_ConnectionFluidName)), FluidTank.MOD_NAME);
            }
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
                Utils.toJava(tank.connection().getFluidStack()).map(FluidStack::getLocalizedName).orElse("Null"));
            if (!tank.connection().hasCreative()) {
                tag.setBoolean(NBT_NonCreative, true);
                tag.setLong(NBT_ConnectionAmount, tank.connection().amount());
                tag.setLong(NBT_ConnectionCapacity, tank.connection().capacity());
                tag.setInteger(NBT_ConnectionComparator, tank.getComparatorLevel());
            }
        }
        return tag;
    }

}
