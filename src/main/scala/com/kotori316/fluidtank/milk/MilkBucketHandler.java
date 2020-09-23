package com.kotori316.fluidtank.milk;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import com.kotori316.fluidtank.fluids.FluidAmount;

public class MilkBucketHandler extends FluidBucketWrapper {

    public MilkBucketHandler(ItemStack container) {
        super(container);
    }

    @Nonnull
    @Override
    public FluidStack getFluid() {
        if (container.getItem() == Items.MILK_BUCKET) {
            return FluidAmount.BUCKET_MILK().toStack();
        } else {
            return FluidStack.EMPTY;
        }
    }

}
