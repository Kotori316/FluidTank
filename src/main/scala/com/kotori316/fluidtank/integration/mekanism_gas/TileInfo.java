package com.kotori316.fluidtank.integration.mekanism_gas;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.integration.Localize;
import com.kotori316.fluidtank.tiles.Connection;
import com.kotori316.fluidtank.tiles.TileTank;

final class TileInfo implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final TileGasTank tile;
    private Object holder;

    TileInfo(TileGasTank tile) {
        this.tile = tile;

        if (Constant.isMekanismLoaded()) {
            this.setHolder();
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (Constant.isMekanismLoaded()) {
            return this.getGasCapability(cap, side);
        } else {
            return LazyOptional.empty();
        }
    }

    private <T> LazyOptional<T> getGasCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return Optional.ofNullable((Holder) this.holder)
            .map(h -> h.gasConnection)
            .map(c -> c.getCapability(cap, side))
            .orElseGet(LazyOptional::empty);
    }

    private void setHolder() {
        this.holder = new Holder(this.tile);
    }

    Object getHolder() {
        return this.holder;
    }

    void updateInfo() {
        if (Constant.isMekanismLoaded())
            this.setHolder();
    }

    Component getMessage() {
        if (Constant.isMekanismLoaded()) {
            return ((Holder) getHolder()).gasConnection.getTextComponent();
        } else {
            Constant.LOGGER.error("Called {}#getMessage. This is UNREACHABLE.", getClass().getName());
            return Component.literal("%sThis tile is unavailable.%s".formatted(ChatFormatting.RED, ChatFormatting.RESET));
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (Constant.isMekanismLoaded())
            serializeHolder(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (Constant.isMekanismLoaded())
            deserializeHolder(nbt);
    }

    private void serializeHolder(CompoundTag tag) {
        var holder = (Holder) getHolder();
        tag.merge(holder.gasTankHandler.serializeNBT());
    }

    private void deserializeHolder(CompoundTag tag) {
        var holder = (Holder) getHolder();
        holder.gasTankHandler.deserializeNBT(tag);
    }

    static final class Holder {
        @NotNull
        final GasTankHandler gasTankHandler;
        @NotNull
        GasConnection gasConnection;

        private Holder(TileGasTank tile) {
            this.gasTankHandler = GasTankHandler.forTank(tile);
            this.gasConnection = GasConnection.invalid();
        }

        void setGasConnection(@NotNull GasConnection gasConnection) {
            this.gasConnection = gasConnection;
        }
    }

    @SuppressWarnings("SameParameterValue") // WHAT?
    static Runnable loadTask(TileGasTank tile) {
        return () -> {
            var level = Objects.requireNonNull(tile.getLevel());
            level.getProfiler().push("Connection Loading");
            var c = ((Holder) tile.tileInfo().getHolder()).gasConnection;
            if (Utils.isInDev()) {
                FluidTank.LOGGER.debug(ModObjects.MARKER_TileGasTank(),
                    "Connection {} be loaded in delayed task. At={}, connection={}",
                    c.isDummy() ? "will" : "won't",
                    tile.getBlockPos(), c);
            }
            if (c.isDummy() && !tile.isRemoved()) {
                Connection.load(level, tile.getBlockPos(), TileGasTank.class, GasConnection.GasConnectionHelper());
            }
            level.getProfiler().pop();
        };
    }

    static void unloadTask(TileGasTank tile) {
        var c = ((Holder) tile.tileInfo().getHolder()).gasConnection;
        c.remove(tile);
    }

    static void setItemTag(ItemStack stack, TileGasTank tile) {
        var holder = (Holder) tile.tileInfo().getHolder();
        if (!holder.gasTankHandler.isEmpty()) {
            // Set tag only when the tank has content.
            Utils.setTileTag(stack, tile.tileInfo().serializeNBT());
        }
    }

    static void addItemDescription(CompoundTag stackTag, List<Component> texts) {
        var c = stackTag.getLong(TileTank.NBT_Capacity());
        var stored = stackTag.getCompound("stored");
        var amount = stored.getLong("amount");
        var gasName = stored.getString("gasName");
        texts.add(Component.translatable(Localize.TOOLTIP, gasName, amount, c));
    }
}
