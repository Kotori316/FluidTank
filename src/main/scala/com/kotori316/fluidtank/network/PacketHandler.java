package com.kotori316.fluidtank.network;

import java.util.List;

import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class PacketHandler {
    public static class Server {
        public static void initServer() {
            var list = List.<ServerPacketInit>of(
            );
            list.forEach(i -> ServerPlayNetworking.registerGlobalReceiver(i.name(), i.handler()));
        }

        private record ServerPacketInit(ResourceLocation name, ServerPlayNetworking.PlayChannelHandler handler) {
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void initClient() {
            var list = List.of(
                new ClientPacketInit(ClientSyncMessage.NAME, ClientSyncMessage.HandlerHolder.HANDLER),
                new ClientPacketInit(FluidCacheMessage.NAME, FluidCacheMessage.HandlerHolder.HANDLER)
            );
            list.forEach(i -> ClientPlayNetworking.registerGlobalReceiver(i.name(), i.handler()));
        }

        private record ClientPacketInit(ResourceLocation name, ClientPlayNetworking.PlayChannelHandler handler) {
        }
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(@Nonnull IMessage<?> message) {
        var packet = PacketByteBufs.create();
        message.writeToBuffer(packet);
        ClientPlayNetworking.send(message.getIdentifier(), packet);
    }

    public static void sendToClientPlayer(@Nonnull IMessage<?> message, @Nonnull ServerPlayer player) {
        var packet = PacketByteBufs.create();
        message.writeToBuffer(packet);
        ServerPlayNetworking.send(player, message.getIdentifier(), packet);
    }

    public static void sendToClientWorld(@Nonnull IMessage<?> message, Level level) {
        var packet = PacketByteBufs.create();
        message.writeToBuffer(packet);
        for (ServerPlayer player : PlayerLookup.world((ServerLevel) level)) {
            ServerPlayNetworking.send(player, message.getIdentifier(), packet);
        }
    }
}
