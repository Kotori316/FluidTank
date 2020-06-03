package com.kotori316.fluidtank.network;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public abstract class SideProxy {

    public abstract scala.Option<World> getWorld(NetworkEvent.Context context);

    public static boolean isServer(TileEntity entity) {
        if (entity == null) return false;
        World world = entity.getWorld();
        return world != null && !world.isRemote();
    }

    public abstract Item.Properties getTankProperties();

}
