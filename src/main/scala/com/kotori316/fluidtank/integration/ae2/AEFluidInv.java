package com.kotori316.fluidtank.integration.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.fluids.FluidAction;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.FluidKey;
import com.kotori316.fluidtank.fluids.VariantUtil;
import com.kotori316.fluidtank.tiles.TileTank;

record AEFluidInv(TileTank tank) implements MEStorage {

    @Override
    public long insert(AEKey what, long amount, Actionable actionable, IActionSource source) {
        FluidAmount fluidAmount = fromAEStack(what, amount);
        FluidAmount filled = tank.connection().handler().fill(fluidAmount, actionable == Actionable.MODULATE ? FluidAction.EXECUTE : FluidAction.SIMULATE);
        return filled.fabricAmount();
    }

    @Override
    public long extract(AEKey what, long amount, Actionable actionable, IActionSource source) {
        FluidAmount fluidAmount = fromAEStack(what, amount);
        FluidAmount drained = tank.connection().handler().drain(fluidAmount, actionable == Actionable.MODULATE ? FluidAction.EXECUTE : FluidAction.SIMULATE);
        return drained.fabricAmount();
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        var amount = OptionConverters.toJava(tank.connection().getFluidStack());
        amount.ifPresent(fluidAmount -> out.add(AEFluidKey.of(fluidAmount.fluid(), OptionConverters.toJava(fluidAmount.nbt()).orElse(null)),
            Math.min(fluidAmount.fabricAmount(), Long.MAX_VALUE - Integer.MAX_VALUE * 2L)));
    }

    @NotNull
    static FluidAmount fromAEStack(@Nullable AEKey what, long amount) {
        if (what instanceof AEFluidKey fluidKey) {
            return FluidAmount.apply(fluidKey.getFluid(), amount, Option.apply(fluidKey.copyTag()));
        } else {
            return FluidAmount.EMPTY();
        }
    }

    @Override
    public Component getDescription() {
        return this.tank.getName();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        if (what instanceof AEFluidKey fluidKey) {
            var tankKey = FluidKey.from(tank.connection().fluidType());
            if (tankKey.fluid().isSame(fluidKey.getFluid())) {
                if (tankKey.tag().isEmpty() && !fluidKey.hasTag())
                    return true; // No NBT is in stack.
                else
                    return fluidKey.matches(VariantUtil.convert(tankKey)); // Check NBT as FluidStack
            }
        }
        return false;
    }
}
