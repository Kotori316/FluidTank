package com.kotori316.fluidtank.integration;

import java.util.stream.Stream;

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.tank.Connection;
import com.kotori316.fluidtank.tank.TankBlock;
import com.kotori316.fluidtank.tank.TankBlockItem;
import com.kotori316.fluidtank.tank.TileTank;

@SuppressWarnings({"UnstableApiUsage"})
public class FabricFluidTankStorage extends SnapshotParticipant<FluidAmount> implements SingleSlotStorage<FluidVariant> {
    private final Connection connection;

    public FabricFluidTankStorage(Connection connection) {
        this.connection = connection;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        var toFill = getFluidAmount(resource, maxAmount);
        var tried = connection.handler().fill(toFill, false, 0);
        if (tried.nonEmpty()) {
            updateSnapshots(transaction);
            var inserted = connection.handler().fill(toFill, true, 0);
            return asFabricAmount(inserted.fluidVolume().amount());
        } else {
            return 0;
        }
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        var toDrain = getFluidAmount(resource, maxAmount);
        var tried = connection.handler().drain(toDrain, false, 0);
        if (tried.nonEmpty()) {
            updateSnapshots(transaction);
            var extracted = connection.handler().drain(toDrain, true, 0);
            return asFabricAmount(extracted.fluidVolume().amount());
        } else {
            return 0;
        }
    }

    @Override
    public boolean isResourceBlank() {
        return connection.getFluidStack().isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        var fluid = connection.getFluidStack();
        return fluid.fold(FluidVariant::blank, f -> FluidVariant.of(f.fluid()));
    }

    @Override
    public long getAmount() {
        return connection.amountInBCAmount().asLong(FluidConstants.BUCKET);
    }

    @Override
    public long getCapacity() {
        return connection.capacity() / FluidAmount.AMOUNT_BUCKET() * FluidConstants.BUCKET;
    }

    @Override
    protected FluidAmount createSnapshot() {
        return connection.getFluidStack().getOrElse(FluidAmount::EMPTY);
    }

    @Override
    protected void readSnapshot(FluidAmount snapshot) {
        connection.handler().drain(connection.getFluidStack().getOrElse(FluidAmount::EMPTY), true, 0);
        connection.handler().fill(snapshot, true, 0);
    }

    static FluidAmount getFluidAmount(FluidVariant resource, long amount) {
        return FluidAmount.apply(FluidKeys.get(resource.getFluid()).withAmount(asBCAmount(amount)));
    }

    static alexiil.mc.lib.attributes.fluid.amount.FluidAmount asBCAmount(long fabricAmount) {
        return alexiil.mc.lib.attributes.fluid.amount.FluidAmount.of(fabricAmount, FluidConstants.BUCKET);
    }

    static long asFabricAmount(alexiil.mc.lib.attributes.fluid.amount.FluidAmount bcAmount) {
        return bcAmount.asLong(FluidConstants.BUCKET);
    }

    public static void register() {
        FluidStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
            if (blockEntity instanceof TileTank tank) {
                return new FabricFluidTankStorage(tank.connection());
            } else {
                return null;
            }
        }, ModTank.Entries.TANK_BLOCK_ENTITY_TYPE, ModTank.Entries.CREATIVE_BLOCK_ENTITY_TYPE, ModTank.Entries.VOID_BLOCK_ENTITY_TYPE);

        var items = Stream.concat(Stream.of(ModTank.Entries.WOOD_TANK), ModTank.Entries.TANK_BLOCKS.stream())
            .map(TankBlock::blockItem)
            .toArray(TankBlockItem[]::new);
        FluidStorage.ITEM.registerForItems((itemStack, context) -> {
            if (itemStack.getItem() instanceof TankBlockItem)
                // Don't allow subclasses.
                return new FabricTankItemStorage(context);
            else
                return null;
        }, items);
    }
}
