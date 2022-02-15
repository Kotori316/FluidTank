package com.kotori316.fluidtank.integration;

import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.tank.ItemTank;
import com.kotori316.fluidtank.tank.TankBlockItem;
import com.kotori316.fluidtank.tank.Tiers;

@SuppressWarnings({"UnstableApiUsage", "ClassCanBeRecord"})
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
        var inserted = tank.fill(fluid, true, 0);
        if (inserted.nonEmpty() &&
            context.exchange(createNewVariant(tank.createTag(tankId())), 1, transaction) == 1) {
            return FabricFluidTankStorage.asFabricAmount(inserted.fluidVolume().amount());
        }
        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        var fluid = FabricFluidTankStorage.getFluidAmount(resource, maxAmount);
        var tank = getItemTank();
        var drained = tank.drain(fluid, true, 0);
        if (drained.nonEmpty() &&
            context.exchange(createNewVariant(tank.createTag(tankId())), 1, transaction) == 1) {
            return FabricFluidTankStorage.asFabricAmount(drained.fluidVolume().amount());
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
        return getItemTank().getFluid().fluidVolume().amount().asLong(FluidConstants.BUCKET);
    }

    @Override
    public long getCapacity() {
        return getItemTank().getCapacity();
    }

    public ItemTank getItemTank() {
        var item = context.getItemVariant();
        var tag = item.getNbt();
        if (tag != null && tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            var tileTag = tag.getCompound("BlockEntityTag");
            if (item.getItem() instanceof TankBlockItem blockItem) {
                return ItemTank.from(tileTag, blockItem.blockTank.tiers);
            } else {
                // What?
                LOGGER.warn("Invalid item {} was given. Tag: {}", item, tag);
                return ItemTank.from(tileTag, Tiers.Invalid);
            }
        } else {
            if (item.getItem() instanceof TankBlockItem blockItem) {
                return ItemTank.empty(blockItem.blockTank.tiers.amount(), blockItem.blockTank.tiers);
            } else {
                // What?
                LOGGER.warn("Invalid item {} was given. Tag: {}", item, tag);
                return ItemTank.empty(0, Tiers.Invalid);
            }
        }
    }

    private ItemVariant createNewVariant(Option<CompoundTag> tankTag) {
        var item = context.getItemVariant();
        var tag = item.copyNbt();
        if (tag == null) {
            var newTag = OptionConverters.toJava(tankTag)
                .map(t -> {
                    var stackTag = new CompoundTag();
                    stackTag.put("BlockEntityTag", t);
                    return stackTag;
                }).orElse(null);
            return ItemVariant.of(item.getItem(), newTag);
        } else {
            if (tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
                OptionConverters.toJava(tankTag)
                    .ifPresentOrElse(t -> tag.getCompound("BlockEntityTag").merge(t),
                        () -> tag.remove("BlockEntityTag"));
            } else {
                // put
                OptionConverters.toJava(tankTag).ifPresent(t ->
                    tag.put("BlockEntityTag", t));
            }
            return ItemVariant.of(item.getItem(), tag.isEmpty() ? null : tag);
        }
    }

    static String tankId() {
        return Optional.ofNullable(Registry.BLOCK_ENTITY_TYPE.getKey(ModTank.Entries.TANK_BLOCK_ENTITY_TYPE))
            .map(ResourceLocation::toString)
            .orElseThrow(() -> new IllegalStateException("No key for tank entity type."));
    }
}
