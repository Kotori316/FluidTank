package com.kotori316.fluidtank.network;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.CATTile;

public class FluidCacheMessage {
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

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dimensionId.location());
        buffer.writeInt(amounts.size());
        amounts.forEach(a -> buffer.writeNbt(a.write(new CompoundTag())));
    }

    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        OptionConverters.toJava(FluidTank.proxy.getLevel(ctx.get()))
            .filter(w -> w.dimension().equals(dimensionId))
            .map(w -> w.getBlockEntity(pos))
            .filter(CATTile.class::isInstance)
            .map(CATTile.class::cast)
            .ifPresent(tile -> ctx.get().enqueueWork(() -> tile.fluidCache = this.amounts));
        ctx.get().setPacketHandled(true);
    }
}
