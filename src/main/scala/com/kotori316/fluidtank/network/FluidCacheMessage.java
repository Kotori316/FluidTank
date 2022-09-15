package com.kotori316.fluidtank.network;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.CATTile;

/**
 * To Client Only.
 */
public class FluidCacheMessage implements IMessage<FluidCacheMessage> {
    public static final ResourceLocation NAME = new ResourceLocation(FluidTank.modID, "fluid_cache_message");
    private final ResourceKey<Level> dimensionId;
    private final BlockPos pos;
    private final List<FluidAmount> amounts;

    public FluidCacheMessage(CATTile tile) {
        pos = tile.getBlockPos();
        dimensionId = Optional.ofNullable(tile.getLevel()).map(Level::dimension).orElse(Level.OVERWORLD);
        amounts = tile.fluidAmountList();
    }

    public FluidCacheMessage(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        dimensionId = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation());
        int size = buffer.readInt();
        amounts = Stream.generate(buffer::readNbt).limit(size)
            .map(FluidAmount::fromNBT)
            .toList();
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dimensionId.location());
        buffer.writeInt(amounts.size());
        amounts.forEach(a -> buffer.writeNbt(a.write(new CompoundTag())));
    }

    @Override
    public ResourceLocation getIdentifier() {
        return NAME;
    }

    @Environment(EnvType.CLIENT)
    static class HandlerHolder {
        static final ClientPlayNetworking.PlayChannelHandler HANDLER = (client, handler, buf, responseSender) -> {
            var message = new FluidCacheMessage(buf);
            var world = client.level;
            if (world != null && world.dimension().equals(message.dimensionId)) {
                client.execute(() -> {
                    if (world.getBlockEntity(message.pos) instanceof CATTile tile) {
                        tile.fluidCache = message.amounts;
                    }
                });
            }
        };
    }
}
