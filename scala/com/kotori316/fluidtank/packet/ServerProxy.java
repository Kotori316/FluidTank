package com.kotori316.fluidtank.packet;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import scala.Option;

public class ServerProxy extends SideProxy {

    @Override
    public Option<World> getWorld(INetHandler handler) {
        if (handler instanceof NetHandlerPlayServer)
            return Option.apply(((NetHandlerPlayServer) handler).player.getEntityWorld());
        return Option.empty();
    }

    @Override
    public void registerTESR() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
