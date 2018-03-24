package com.kotori316.fluidtank.packet;

import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface SideProxy {

    public World getWorld(INetHandler handler);

    public void registerTESR();

    public static boolean isServer(TileEntity entity) {
        return entity.hasWorld() && !entity.getWorld().isRemote;
    }
}
