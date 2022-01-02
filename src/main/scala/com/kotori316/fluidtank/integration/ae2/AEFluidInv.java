package com.kotori316.fluidtank.integration.ae2;

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.tank.TileTank;

@SuppressWarnings("ClassCanBeRecord")
class AEFluidInv implements MEStorage {
    private final TileTank tank;

    AEFluidInv(TileTank tank) {
        this.tank = tank;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable actionable, IActionSource source) {
        FluidAmount fluidAmount = fromAEStack(what, amount);
        FluidAmount filled = tank.connection().handler().fill(fluidAmount, actionable == Actionable.MODULATE, 0);
        return filled.fluidVolume().amount().asLong(AEFluidKey.AMOUNT_BUCKET);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable actionable, IActionSource source) {
        FluidAmount fluidAmount = fromAEStack(what, amount);
        FluidAmount drained = tank.connection().handler().drain(fluidAmount, actionable == Actionable.MODULATE, 0);
        return drained.fluidVolume().amount().asLong(AEFluidKey.AMOUNT_BUCKET);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        var amount = OptionConverters.toJava(tank.connection().getFluidStack());
        amount.ifPresent(fluidAmount -> out.add(AEFluidKey.of(fluidAmount.fluid(), null /*No NBT*/),
            Math.min(fluidAmount.fluidVolume().amount().asLong(AEFluidKey.AMOUNT_BUCKET), Long.MAX_VALUE - Integer.MAX_VALUE * 2L)));
    }

    @NotNull
    static FluidAmount fromAEStack(@Nullable AEKey what, long amount) {
        if (what instanceof AEFluidKey fluidKey) {
            return new FluidAmount(FluidKeys.get(fluidKey.getFluid()).withAmount(alexiil.mc.lib.attributes.fluid.amount.FluidAmount.of(amount, AEFluidKey.AMOUNT_BUCKET)));
        } else {
            return FluidAmount.EMPTY();
        }
    }

    @Override
    public Component getDescription() {
        return this.tank.getName();
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        if (what instanceof AEFluidKey fluidKey) {
            var tankKey = tank.connection().fluidType();
            if (tankKey.fluid().isSame(fluidKey.getFluid())) {
                return !fluidKey.hasTag(); // Currently, NBT tag is not supported.
            }
        }
        return false;
    }
}
