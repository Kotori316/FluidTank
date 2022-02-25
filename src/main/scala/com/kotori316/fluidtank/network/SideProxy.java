package com.kotori316.fluidtank.network;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public abstract class SideProxy {

    public abstract Optional<Level> getLevel(NetworkEvent.Context context);

    public static boolean isServer(BlockEntity entity) {
        if (entity == null) return false;
        var world = entity.getLevel();
        return world != null && !world.isClientSide();
    }

    public abstract Item.Properties getTankProperties();

    public abstract Item.Properties getReservoirProperties();

    public static SideProxy get() {
        return switch (FMLEnvironment.dist) {
            case CLIENT -> new Client().get();
            case DEDICATED_SERVER -> new Server().get();
        };
    }

    private static class Client implements Supplier<SideProxy> {
        @Override
        @OnlyIn(Dist.CLIENT)
        public SideProxy get() {
            return new ClientProxy();
        }
    }

    private static class Server implements Supplier<SideProxy> {
        @Override
        public SideProxy get() {
            return new ServerProxy();
        }
    }
}
