package com.kotori316.fluidtank.network;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.tiles.CATTile;

public class FluidCacheMessage {
    private int dimensionId;
    private BlockPos pos;
    private List<FluidAmount> amounts;

    public void write(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dimensionId);
        buffer.writeInt(amounts.size());
        amounts.forEach(a -> buffer.writeCompoundTag(a.write(new NBTTagCompound())));
    }

    public static FluidCacheMessage apply(PacketBuffer buffer) {
        FluidCacheMessage message = new FluidCacheMessage();
        message.pos = buffer.readBlockPos();
        message.dimensionId = buffer.readInt();
        message.amounts = IntStream.range(0, buffer.readInt())
            .mapToObj(i -> buffer.readCompoundTag())
            .map(FluidAmount::fromNBT)
            .collect(Collectors.toList());
        return message;
    }

    public static FluidCacheMessage apply(CATTile tile) {
        FluidCacheMessage message = new FluidCacheMessage();
        message.pos = tile.getPos();
        message.dimensionId = Optional.ofNullable(tile.getWorld()).map(World::getDimension).map(Dimension::getType).map(DimensionType::getId).orElse(0);
        message.amounts = tile.fluidAmountList();
        return message;
    }

    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        FluidTank.proxy.getWorld(ctx.get()).filter(w -> w.getDimension().getType().getId() == dimensionId)
            .map(w -> ((CATTile) w.getTileEntity(pos)))
            .foreach(tile -> ctx.get().enqueueWork(() -> tile.fluidCache = this.amounts));
        ctx.get().setPacketHandled(true);
    }
}
