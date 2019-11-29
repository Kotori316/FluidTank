package com.kotori316.fluidtank.tiles;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import scala.Option;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.ModObjects;

public class CATTile extends TileEntity implements INamedContainerProvider {
    // The direction of FACING is facing to you, people expect to use itemBlock targeting an inventory so the chest exists on the opposite side of FACING.
    public List<FluidAmount> fluidCache = Collections.emptyList();

    public CATTile() {
        super(ModObjects.CAT_TYPE());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        Direction direction = getBlockState().get(BlockStateProperties.FACING);
        if (side != direction) {
            if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return getFluidHandler(direction).cast();
            }
        }
        return super.getCapability(cap, side);
    }

    public LazyOptional<FluidHandlerWrapper> getFluidHandler(Direction direction) {
        assert world != null;
        TileEntity entity = world.getTileEntity(pos.offset(direction));
        if (entity == null) {
            return LazyOptional.empty();
        } else {
            LazyOptional<IItemHandler> capability = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
            if (capability.isPresent()) {
                IItemHandlerModifiable inventory = capability.filter(i -> i instanceof IItemHandlerModifiable).<IItemHandlerModifiable>cast().orElseGet(EmptyHandler::new);
                return LazyOptional.of(() -> new FluidHandlerWrapper(inventory));
            } else {
                return LazyOptional.empty();
            }
        }
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(ModObjects.blockCat().getTranslationKey());
    }

    @Override
    public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
        return new CATContainer(id, player, pos);
    }

    public static class FluidHandlerWrapper implements IFluidHandler {
        private final IItemHandlerModifiable inventory;

        public FluidHandlerWrapper(IItemHandlerModifiable inventory) {
            this.inventory = inventory;
        }

        public LazyOptional<IFluidHandlerItem> getFluidHandler(int tank) {
            return FluidUtil.getFluidHandler(inventory.getStackInSlot(tank));
        }

        @Override
        public int getTanks() {
            return inventory.getSlots();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            ItemStack stackInSlot = inventory.getStackInSlot(tank);
            if (stackInSlot.getItem() == Items.MILK_BUCKET)
                return FluidAmount.BUCKET_MILK().toStack();
            return FluidUtil.getFluidContained(stackInSlot).orElse(FluidStack.EMPTY);
        }

        @Override
        public int getTankCapacity(int tank) {
            return getFluidHandler(tank).map(h -> h.getTankCapacity(0)).orElse(0);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
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

        @Nonnull
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

        @Nonnull
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

        @Nonnull
        public Stream<FluidAmount> getFluidAmountStream() {
            return IntStream.range(0, this.getTanks())
                .mapToObj(this::getFluidInTank)
                .collect(Collectors.groupingBy(FluidKey::new, LinkedHashMap::new, Collectors.summingLong(FluidStack::getAmount)))
                .entrySet().stream()
                .map(e -> e.getKey().toAmount(e.getValue()))
                .filter(FluidAmount::nonEmpty);
        }
    }

    public List<FluidAmount> fluidAmountList() {
        Direction direction = getBlockState().get(BlockStateProperties.FACING);
        LazyOptional<FluidHandlerWrapper> opt = getFluidHandler(direction);
        return opt.map(FluidHandlerWrapper::fluidList).orElse(Collections.emptyList());
    }

    private static class FluidKey {

        private final Fluid fluid;
        private final CompoundNBT tag;

        public FluidKey(FluidStack stack) {
            fluid = stack.getFluid();
            tag = stack.getTag();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FluidKey fluidKey = (FluidKey) o;
            return fluid.equals(fluidKey.fluid) &&
                Objects.equals(tag, fluidKey.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fluid, tag);
        }

        public FluidAmount toAmount(long amount) {
            return new FluidAmount(this.fluid, amount, Option.apply(this.tag));
        }
    }
}
