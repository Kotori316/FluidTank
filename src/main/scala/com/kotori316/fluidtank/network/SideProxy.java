package com.kotori316.fluidtank.network;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public abstract class SideProxy {

    public abstract scala.Option<Level> getLevel(NetworkEvent.Context context);

    public static boolean isServer(BlockEntity entity) {
        if (entity == null) return false;
        var world = entity.getLevel();
        return world != null && !world.isClientSide();
    }

    public abstract Item.Properties getTankProperties();

    public abstract Item.Properties getReservoirProperties();

}
