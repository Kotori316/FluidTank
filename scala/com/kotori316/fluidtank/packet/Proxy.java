package com.kotori316.fluidtank.packet;

import net.minecraft.network.INetHandler;
import net.minecraft.world.World;

public interface Proxy {

    public World getWorld(INetHandler handler);

    public void registerTESR();
}
