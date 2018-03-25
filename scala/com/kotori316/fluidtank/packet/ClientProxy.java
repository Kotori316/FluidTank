package com.kotori316.fluidtank.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.kotori316.fluidtank.tiles.RenderTank;
import com.kotori316.fluidtank.tiles.TileTank;

@SideOnly(Side.CLIENT)
public class ClientProxy extends SideProxy {

    @Override
    public World getWorld(INetHandler handler) {
        return Minecraft.getMinecraft().world;
    }

    @Override
    public void registerTESR() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new RenderTank());
    }
}
