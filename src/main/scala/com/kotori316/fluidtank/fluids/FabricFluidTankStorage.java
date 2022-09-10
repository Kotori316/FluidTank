package com.kotori316.fluidtank.fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import scala.Option;
import scala.jdk.javaapi.StreamConverters;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.tiles.Connection;
import com.kotori316.fluidtank.tiles.TileTank;

@SuppressWarnings({"UnstableApiUsage"})
public class FabricFluidTankStorage extends SnapshotParticipant<FluidAmount> implements SingleSlotStorage<FluidVariant> {
    private final Connection connection;

    public FabricFluidTankStorage(Connection connection) {
        this.connection = connection;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        var toFill = getFluidAmount(resource, maxAmount);
        var tried = connection.handler().fill(toFill, FluidAction.SIMULATE);
        if (tried.nonEmpty()) {
            updateSnapshots(transaction);
            var inserted = connection.handler().fill(toFill, FluidAction.EXECUTE);
            return inserted.fabricAmount();
        } else {
            return 0;
        }
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        var toDrain = getFluidAmount(resource, maxAmount);
        var tried = connection.handler().drain(toDrain, FluidAction.SIMULATE);
        if (tried.nonEmpty()) {
            updateSnapshots(transaction);
            var extracted = connection.handler().drain(toDrain, FluidAction.EXECUTE);
            return extracted.fabricAmount();
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
        return VariantUtil.convertForgeAmountToFabric(connection.amount());
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
        connection.handler().drain(connection.getFluidStack().getOrElse(FluidAmount::EMPTY), FluidAction.EXECUTE);
        connection.handler().fill(snapshot, FluidAction.EXECUTE);
    }

   public static FluidAmount getFluidAmount(FluidVariant variant, long fabricAmount) {
        return FluidAmount.apply(variant.getFluid(), fabricAmount, Option.apply(variant.copyNbt()));
    }

    public static void register() {
        FluidStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
            if (blockEntity instanceof TileTank tank) {
                return new FabricFluidTankStorage(tank.connection());
            } else {
                return null;
            }
        }, ModObjects.TANK_TYPE(), ModObjects.TANK_CREATIVE_TYPE(), ModObjects.TANK_VOID_TYPE());

        var items = StreamConverters.asJavaSeqStream(ModObjects.blockTanks())
            .map(BlockTank::itemBlock)
            .toArray(ItemBlockTank[]::new);
        FluidStorage.ITEM.registerForItems((itemStack, context) -> {
            if (itemStack.getItem().getClass() == ItemBlockTank.class)
                // Don't allow subclasses.
                return new FabricTankItemStorage(context);
            else
                return null;
        }, items);
    }
}
