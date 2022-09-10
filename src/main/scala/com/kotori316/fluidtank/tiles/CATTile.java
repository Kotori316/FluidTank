package com.kotori316.fluidtank.tiles;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import scala.Option;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.fluids.FabricFluidTankStorage;
import com.kotori316.fluidtank.fluids.FluidAction;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.FluidContainer;
import com.kotori316.fluidtank.fluids.FluidKey;
import com.kotori316.fluidtank.fluids.VariantUtil;

public class CATTile extends BlockEntity implements MenuProvider, ExtendedScreenHandlerFactory {
    // The direction of FACING is facing to you, people expect to use itemBlock targeting an inventory so the chest exists on the opposite side of FACING.
    public List<FluidAmount> fluidCache = Collections.emptyList();

    public CATTile(BlockPos pos, BlockState state) {
        super(ModObjects.CAT_TYPE(), pos, state);
    }

    @SuppressWarnings("UnstableApiUsage")
    public Option<FluidHandlerWrapper> getFluidHandler(Direction direction) {
        assert level != null;
        BlockEntity entity = level.getBlockEntity(getBlockPos().relative(direction));
        if (entity == null) {
            return Option.empty();
        } else {
            var storage = ItemStorage.SIDED.find(level, getBlockPos().relative(direction), direction.getOpposite());
            if (storage instanceof InventoryStorage inventoryStorage && inventoryStorage.supportsInsertion()) {
                return Option.apply(new FluidHandlerWrapper(inventoryStorage));
            } else {
                return Option.empty();
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new CATContainer(id, player, getBlockPos());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(getBlockPos());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class FluidHandlerWrapper implements Storage<FluidVariant>, FluidContainer {
        private final InventoryStorage inventory;

        public FluidHandlerWrapper(InventoryStorage inventory) {
            this.inventory = inventory;
        }

        public Optional<Storage<FluidVariant>> getFluidHandler(int slot) {
            var item = this.inventory.getSlot(slot);
            return Optional.ofNullable(FluidStorage.ITEM.find(item.getResource().toStack(Utils.toInt(item.getAmount())), ContainerItemContext.ofSingleSlot(item)));
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) return 0;
            AtomicLong rest = new AtomicLong(maxAmount);
            for (int i = 0; i < this.inventory.getSlots().size(); i++) {
                var handler = getFluidHandler(i);
                handler.map(s -> s.insert(resource, rest.get(), transaction))
                    .ifPresent(l -> rest.addAndGet(-l));
                if (rest.get() <= 0) return maxAmount;
            }
            return maxAmount - rest.get();
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) return 0;
            AtomicLong rest = new AtomicLong(maxAmount);
            for (int i = 0; i < this.inventory.getSlots().size(); i++) {
                var handler = getFluidHandler(i);
                handler.map(s -> s.extract(resource, rest.get(), transaction))
                    .ifPresent(l -> rest.addAndGet(-l));
                if (rest.get() <= 0) return maxAmount;
            }
            return maxAmount - rest.get();
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator() {
            return inventory.getSlots().stream()
                .map(s -> FluidStorage.ITEM.find(s.getResource().toStack(Utils.toInt(s.getAmount())), ContainerItemContext.ofSingleSlot(s)))
                .filter(Objects::nonNull)
                .flatMap(f -> StreamSupport.stream(f.spliterator(), false))
                .iterator();
        }

        public List<FluidAmount> fluidList() {
            return getFluidAmountStream().toList();
        }

        @NotNull
        public Stream<FluidAmount> getFluidAmountStream() {
            return IntStream.range(0, this.inventory.getSlots().size())
                .mapToObj(this::getFluidHandler)
                .flatMap(Optional::stream)
                .flatMap(s -> StreamSupport.stream(s.spliterator(), false))
                .map(s -> FabricFluidTankStorage.getFluidAmount(s.getResource(), s.getAmount()))
                .collect(Collectors.groupingBy(FluidKey::from, LinkedHashMap::new, Collectors.summingLong(FluidAmount::fabricAmount)))
                .entrySet().stream()
                .map(e -> e.getKey().toAmount(e.getValue()))
                .filter(FluidAmount::nonEmpty);
        }

        @Override
        public FluidAmount fill(FluidAmount resource, FluidAction action) {
            var variant = VariantUtil.convert(resource);
            var amount = resource.fabricAmount();
            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = this.insert(variant, amount, transaction);
                if (action.execute()) {
                    transaction.commit();
                }
                return resource.setAmountF(inserted);
            }
        }

        @Override
        public FluidAmount drain(FluidAmount resource, FluidAction action) {
            var variant = VariantUtil.convert(resource);
            var amount = resource.fabricAmount();
            try (Transaction transaction = Transaction.openOuter()) {
                var extracted = this.extract(variant, amount, transaction);
                if (action.execute()) {
                    transaction.commit();
                }
                return resource.setAmountF(extracted);
            }
        }
    }

    public List<FluidAmount> fluidAmountList() {
        Direction direction = getBlockState().getValue(BlockStateProperties.FACING);
        var opt = OptionConverters.toJava(getFluidHandler(direction));
        return opt.map(FluidHandlerWrapper::fluidList).orElse(Collections.emptyList());
    }

}
