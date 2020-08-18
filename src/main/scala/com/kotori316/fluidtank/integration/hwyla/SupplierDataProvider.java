/*package com.kotori316.fluidtank.integration.hwyla;

import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.integration.Localize;
import com.kotori316.fluidtank.tiles.FluidSourceTile;

public class SupplierDataProvider implements IServerDataProvider<TileEntity>, IComponentProvider {

    private static final String KEY_FLUID = "fluid";

    @Override
    public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity tileEntity) {
        // Called in server world.
        if (tileEntity instanceof FluidSourceTile) {
            FluidSourceTile sourceTile = (FluidSourceTile) tileEntity;
            compoundNBT.put(KEY_FLUID, sourceTile.fluid().write(new CompoundNBT()));
        }
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        TileEntity t = accessor.getTileEntity();
        if (t instanceof FluidSourceTile) {
            CompoundNBT data = accessor.getServerData();
            FluidAmount fluid = FluidAmount.fromNBT(data.getCompound(KEY_FLUID));
            tooltip.add(new TranslationTextComponent(Localize.WAILA_SUPPLIER, fluid));
        }
    }
}
*/