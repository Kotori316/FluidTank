package com.kotori316.fluidtank.network;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.tiles.CATTile;

public class FluidCacheMessage {
    private ResourceLocation dimensionId;
    private BlockPos pos;
    private List<FluidAmount> amounts;

    public void write(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dimensionId);
        buffer.writeInt(amounts.size());
        amounts.forEach(a -> buffer.writeCompoundTag(a.write(new CompoundNBT())));
    }

    public static FluidCacheMessage apply(PacketBuffer buffer) {
        FluidCacheMessage message = new FluidCacheMessage();
        message.pos = buffer.readBlockPos();
        message.dimensionId = buffer.readResourceLocation();
        message.amounts = IntStream.range(0, buffer.readInt())
            .mapToObj(i -> buffer.readCompoundTag())
            .map(FluidAmount::fromNBT)
            .collect(Collectors.toList());
        return message;
    }

    public static FluidCacheMessage apply(CATTile tile) {
        FluidCacheMessage message = new FluidCacheMessage();
        message.pos = tile.getPos();
        message.dimensionId = Optional.ofNullable(tile.getWorld()).map(World::func_234923_W_).orElse(World.field_234918_g_).func_240901_a_();
        message.amounts = tile.fluidAmountList();
        return message;
    }

    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        OptionConverters.toJava(FluidTank.proxy.getWorld(ctx.get()))
            .filter(w -> w.func_234923_W_().func_240901_a_().equals(dimensionId))
            .map(w -> w.getTileEntity(pos))
            .filter(CATTile.class::isInstance)
            .map(CATTile.class::cast)
            .ifPresent(tile -> ctx.get().enqueueWork(() -> tile.fluidCache = this.amounts));
        ctx.get().setPacketHandled(true);
    }
}
