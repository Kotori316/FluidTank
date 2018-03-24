package com.kotori316.fluidtank.packet;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;

public class ServerProxy implements SideProxy {

    @Override
    public World getWorld(INetHandler handler) {
        if (handler instanceof NetHandlerPlayServer)
            return ((NetHandlerPlayServer) handler).player.getEntityWorld();
        return null;
    }

    @Override
    public void registerTESR() {
        //NO OP
    }
}
