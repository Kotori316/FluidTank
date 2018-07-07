package com.kotori316.fluidtank.packet;

import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class SideProxy {

    public abstract scala.Option<World> getWorld(INetHandler handler);

    public abstract void registerTESR();

    public static boolean isServer(TileEntity entity) {
        return entity.hasWorld() && !entity.getWorld().isRemote;
    }
}
