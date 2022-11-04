package com.kotori316.fluidtank.integration.mekanism_gas;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.tiles.Connection;

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
            Constant.LOGGER.warn("Called {}#getMessage when mekanism is not available.", getClass().getName());
            return new TextComponent("Invalid request.");
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
    }

    @SuppressWarnings("SameParameterValue") // WHAT?
    static Runnable loadTask(TileGasTank tile) {
        return () -> {
            Objects.requireNonNull(tile.getLevel()).getProfiler().push("Connection Loading");
            var c = ((Holder) tile.tileInfo().getHolder()).gasConnection;
            if (Utils.isInDev()) {
                FluidTank.LOGGER.debug(ModObjects.MARKER_TileGasTank(),
                    "Connection {} loaded in delayed task. At={}, connection={}",
                    c.isDummy() ? "will be" : "won't",
                    tile.getBlockPos(), c);
            }
            if (c.isDummy()) {
                Connection.load(tile.getLevel(), tile.getBlockPos(), TileGasTank.class, GasConnection.GasConnectionHelper());
            }
            tile.getLevel().getProfiler().pop();
        };
    }
}
