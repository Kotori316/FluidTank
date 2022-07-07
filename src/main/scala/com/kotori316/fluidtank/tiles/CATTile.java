package com.kotori316.fluidtank.tiles;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.fluids.FluidAmount;

public class CATTile extends BlockEntity implements MenuProvider {
    // The direction of FACING is facing to you, people expect to use itemBlock targeting an inventory so the chest exists on the opposite side of FACING.
    public List<FluidAmount> fluidCache = Collections.emptyList();

    public CATTile(BlockPos pos, BlockState state) {
        super(ModObjects.CAT_TYPE(), pos, state);
    }
/*
    @SuppressWarnings("UnstableApiUsage")
    public Option<FluidHandlerWrapper> getFluidHandler(Direction direction) {
        assert level != null;
        BlockEntity entity = level.getBlockEntity(getBlockPos().relative(direction));
        if (entity == null) {
            return Option.empty();
        } else {
            var storage = ItemStorage.SIDED.find(level, getBlockPos().relative(direction), direction.getOpposite());
            if (storage == null || !storage.supportsInsertion()) return Option.empty();


            return entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())
                .resolve()
                .flatMap(i -> i instanceof IItemHandlerModifiable ? Optional.of((IItemHandlerModifiable) i) : Optional.empty())
                .map(i -> ((NonNullSupplier<FluidHandlerWrapper>) () -> new FluidHandlerWrapper(i)))
                .map(LazyOptional::of)
                .orElse(LazyOptional.empty());
        }
    }*/

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new CATContainer(id, player, getBlockPos());
    }
/*
    public static class FluidHandlerWrapper implements IFluidHandler {
        private final IItemHandlerModifiable inventory;

        public FluidHandlerWrapper(IItemHandlerModifiable inventory) {
            this.inventory = inventory;
        }

        public LazyOptional<IFluidHandlerItem> getFluidHandler(int tank) {
            ItemStack stackInSlot = inventory.getStackInSlot(tank);
            if (stackInSlot.isEmpty())
                return LazyOptional.empty();
            else
                return net.minecraftforge.fluids.FluidUtil.getFluidHandler(stackInSlot);
        }

        @Override
        public int getTanks() {
            return inventory.getSlots();
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return getFluidHandler(tank).map(h -> h.drain(Integer.MAX_VALUE, FluidAction.SIMULATE)).orElse(FluidStack.EMPTY);
        }

        @Override
        public int getTankCapacity(int tank) {
            return getFluidHandler(tank).map(h -> h.getTankCapacity(0)).orElse(0);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return getFluidHandler(tank).map(h -> h.isFluidValid(0, stack)).orElse(false);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) return 0;
            FluidStack toFill = resource.copy();
            for (int i = 0; i < getTanks(); i++) {
                int slot = i;
                getFluidHandler(slot).ifPresent(handler -> {
                    int filled = handler.fill(toFill, action);
                    toFill.setAmount(toFill.getAmount() - filled);
                    if (action.execute()) {
                        inventory.setStackInSlot(slot, handler.getContainer());
                    }
                });
                if (toFill.isEmpty())
                    return resource.getAmount();
            }
            return resource.getAmount() - toFill.getAmount();
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) return FluidStack.EMPTY;
            FluidStack toDrain = resource.copy();
            for (int i = 0; i < getTanks(); i++) {
                int slot = i;
                getFluidHandler(slot)
                    .map(h -> {
                        FluidStack drain = h.drain(toDrain, action);
                        if (action.execute()) {
                            inventory.setStackInSlot(slot, h.getContainer());
                        }
                        return drain;
                    })
                    .ifPresent(drained -> toDrain.setAmount(toDrain.getAmount() - drained.getAmount()));
                if (toDrain.isEmpty())
                    break;
            }
            return new FluidStack(resource, resource.getAmount() - toDrain.getAmount());
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) return FluidStack.EMPTY;
            Optional<FluidStack> first = getFluidAmountStream()
                .map(FluidAmount::toStack)
                .findFirst();
            return first.map(s -> this.drain(new FluidStack(s, Math.min(maxDrain, s.getAmount())), action)).orElse(FluidStack.EMPTY);
        }

        public List<FluidAmount> fluidList() {
            return getFluidAmountStream().collect(Collectors.toList());
        }

        @NotNull
        public Stream<FluidAmount> getFluidAmountStream() {
            return IntStream.range(0, this.getTanks())
                .mapToObj(this::getFluidInTank)
                .collect(Collectors.groupingBy(FluidKey::from, LinkedHashMap::new, Collectors.summingLong(FluidStack::getAmount)))
                .entrySet().stream()
                .map(e -> e.getKey().toAmount(e.getValue()))
                .filter(FluidAmount::nonEmpty);
        }
    }*/

    public List<FluidAmount> fluidAmountList() {
        return List.of();
        /*Direction direction = getBlockState().getValue(BlockStateProperties.FACING);
        var opt = OptionConverters.toJava(getFluidHandler(direction));
        return opt.map(FluidHandlerWrapper::fluidList).orElse(Collections.emptyList());*/
    }

}
