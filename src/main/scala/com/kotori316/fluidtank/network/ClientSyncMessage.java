package com.kotori316.fluidtank.network;

import java.util.Objects;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.Utils;

/**
 * To Client Only.
 */
public final class ClientSyncMessage implements IMessage<ClientSyncMessage> {
    public static final ResourceLocation NAME = new ResourceLocation(FluidTank.modID, "client_sync_message");
    private final CompoundTag tag;
    private final BlockPos pos;
    private final ResourceKey<Level> dim;

    public ClientSyncMessage(BlockPos pos, ResourceKey<Level> dim, CompoundTag tag) {
        this.tag = tag;
        this.pos = pos;
        this.dim = dim;
    }

    public ClientSyncMessage(BlockEntity entity) {
        this.pos = entity.getBlockPos();
        this.dim = Objects.requireNonNull(entity.getLevel(), "Where is this tile?").dimension();
        this.tag = entity.saveWithoutMetadata();
    }

    public ClientSyncMessage(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()), buf.readNbt());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeResourceLocation(this.dim.location());
        buf.writeNbt(this.tag);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return NAME;
    }

    @Environment(EnvType.CLIENT)
    static class HandlerHolder {
        static final ClientPlayNetworking.PlayChannelHandler HANDLER = (client, handler, buf, responseSender) -> {
            var message = new ClientSyncMessage(buf);
            var world = client.level;
            if (world != null && world.dimension().equals(message.dim)) {
                client.execute(() -> {
                    var maybeTank = world.getBlockEntity(message.pos);
                    if (maybeTank instanceof ClientSync tile) {
                        tile.fromClientTag(message.tag);
                    } else {
                        Utils.runOnce("%s-%s-%s".formatted(message.dim.location(), message.pos, Utils.getClassName(maybeTank)),
                            () -> FluidTank.LOGGER.error("Tried to sync to {} in {}, but the tile isn't ClientSync({})",
                                message.pos, message.dim, maybeTank));
                    }
                });
            }
        };
    }
}
