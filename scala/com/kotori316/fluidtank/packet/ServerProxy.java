package com.kotori316.fluidtank.packet;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ServerProxy extends SideProxy {

    @Override
    public World getWorld(INetHandler handler) {
        if (handler instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) handler).player.getEntityWorld();
        return null;
    }

    @Override
    public void registerTESR() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
