package com.kotori316.fluidtank.milk;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import com.kotori316.fluidtank.FluidAmount;

public class MilkBucketHandler implements IFluidHandlerItem {

    private ItemStack container;

    public MilkBucketHandler(ItemStack container) {
        this.container = container;
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        if (container.getItem() == Items.MILK_BUCKET) {
            return FluidAmount.BUCKET_MILK().toStack();
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        return FluidAttributes.BUCKET_VOLUME;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return stack.isFluidEqual(FluidAmount.BUCKET_MILK().toStack());
    }

    protected void setFluid(@Nonnull FluidStack fluidStack) {
        if (fluidStack.isEmpty())
            container = new ItemStack(Items.BUCKET);
        else
            container = FluidUtil.getFilledBucket(fluidStack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 ||
            !getFluidInTank(0).isEmpty() ||
            !isFluidValid(0, resource)) {
            return 0;
        }

        if (action.execute()) {
            setFluid(resource);
        }

        return FluidAttributes.BUCKET_VOLUME;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < FluidAttributes.BUCKET_VOLUME) {
            return FluidStack.EMPTY;
        }

        FluidStack fluidStack = getFluidInTank(0);
        if (!fluidStack.isEmpty() && fluidStack.isFluidEqual(resource)) {
            if (action.execute()) {
                setFluid(FluidStack.EMPTY);
            }
            return fluidStack;
        }

        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain < FluidAttributes.BUCKET_VOLUME) {
            return FluidStack.EMPTY;
        }

        FluidStack fluidStack = getFluidInTank(0);
        if (!fluidStack.isEmpty()) {
            if (action.execute()) {
                setFluid(FluidStack.EMPTY);
            }
            return fluidStack;
        }

        return FluidStack.EMPTY;
    }

}
