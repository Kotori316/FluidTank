package com.kotori316.fluidtank.milk;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
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
        return FluidAmount.fromItem(this.container).toStack();
    }

}
