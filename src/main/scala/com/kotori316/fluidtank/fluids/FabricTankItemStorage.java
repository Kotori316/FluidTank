package com.kotori316.fluidtank.fluids;

import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.items.ReservoirItem;
import com.kotori316.fluidtank.items.TankItemFluidHandler;

@SuppressWarnings({"UnstableApiUsage"})
public final class FabricTankItemStorage implements SingleSlotStorage<FluidVariant> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricTankItemStorage.class);
    private final ContainerItemContext context;

    public FabricTankItemStorage(ContainerItemContext context) {
        this.context = context;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        var fluid = FabricFluidTankStorage.getFluidAmount(resource, maxAmount);
        var tank = getItemTank();
        var inserted = tank.fill(fluid, FluidAction.EXECUTE);
        if (inserted.nonEmpty() &&
            context.exchange(createNewVariant(tank.createTag()), 1, transaction) == 1) {
            return VariantUtil.convertForgeAmountToFabric(inserted.amount());
        }
        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        var fluid = FabricFluidTankStorage.getFluidAmount(resource, maxAmount);
        var tank = getItemTank();
        var drained = tank.drain(fluid, FluidAction.EXECUTE);
        if (drained.nonEmpty() &&
            context.exchange(createNewVariant(tank.createTag()), 1, transaction) == 1) {
            return VariantUtil.convertForgeAmountToFabric(drained.amount());
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return getResource().isBlank();
    }

    @Override
    public FluidVariant getResource() {
        var fluid = getItemTank().getFluid();
        return FluidVariant.of(fluid.fluid(), null);
    }

    @Override
    public long getAmount() {
        return VariantUtil.convertForgeAmountToFabric(getItemTank().getFluid().amount());
    }

    @Override
    public long getCapacity() {
        return VariantUtil.convertForgeAmountToFabric(getItemTank().getCapacity());
    }

    public TankItemFluidHandler getItemTank() {
        var item = context.getItemVariant();
        var count = Utils.toInt(context.getAmount());
        var stack = item.toStack(count);
        if (item.getItem() instanceof ItemBlockTank itemTank) {
            return new TankItemFluidHandler(itemTank.blockTank().tier(), stack);
        } else if (item.getItem() instanceof ReservoirItem reservoirItem) {
            return new TankItemFluidHandler(reservoirItem.tier(), stack);
        } else {
            throw new IllegalArgumentException("How do I get tier from %s?".formatted(item));
        }
    }

    private ItemVariant createNewVariant(CompoundTag newTag) {
        var item = context.getItemVariant();
        return ItemVariant.of(item.getItem(), newTag);
    }

    static String tankId() {
        return Optional.ofNullable(Registry.BLOCK_ENTITY_TYPE.getKey(ModObjects.TANK_TYPE()))
            .map(ResourceLocation::toString)
            .orElseThrow(() -> new IllegalStateException("No key for tank entity type."));
    }
}
