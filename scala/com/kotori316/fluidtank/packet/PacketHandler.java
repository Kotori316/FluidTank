package com.kotori316.fluidtank.packet;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import com.kotori316.fluidtank.FluidTank;

public class PacketHandler {
    public static final SimpleNetworkWrapper WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(FluidTank.MOD_NAME);

    public static void init() {
        WRAPPER.registerMessage(TileMessage.instance(), TileMessage.class, 0, Side.CLIENT);
        WRAPPER.registerMessage(TileMessage.instance(), TileMessage.class, 1, Side.SERVER);
    }
}
