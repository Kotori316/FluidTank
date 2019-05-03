package com.kotori316.fluidtank.network;

import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import scala.Option;

public abstract class SideProxy {

    public abstract scala.Option<World> getWorld(NetworkEvent.Context context);

    public abstract void registerTESR();

    public static boolean isServer(TileEntity entity) {
        if (entity == null) return false;
        World world = entity.getWorld();
        return world != null && !world.isRemote();
    }

    public abstract Item.Properties getTankProperties();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // Just a converting method.
    protected static <T> Option<T> fromJava(Optional<T> option) {
        if (option.isPresent()) {
            return Option.apply(option.get());
        } else {
            return Option.empty();
        }
    }
}
