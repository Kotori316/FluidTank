package com.kotori316.fluidtank.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;

import com.kotori316.fluidtank.fluids.FluidAmount;

public final class SerializeTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void emptySerialize1(GameTestHelper helper) {
        CompoundTag expect = new CompoundTag();
        expect.putString("fluid", "minecraft:empty");
        expect.putLong("fabric_amount", 0);

        CompoundTag actual = FluidAmount.EMPTY().write(new CompoundTag());

        assert expect.equals(actual) : "Expect %s, Actual %s".formatted(expect, actual);

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void emptyDeserialize1(GameTestHelper helper) {
        CompoundTag from = new CompoundTag();
        from.putString("fluid", "minecraft:empty");
        from.putLong("fabric_amount", 0);

        var result = FluidAmount.fromNBT(from);
        assert result.equals(FluidAmount.EMPTY()) :
            "Expect %s, Actual %s".formatted(FluidAmount.EMPTY(), result);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void emptyDeserialize2(GameTestHelper helper) {
        CompoundTag from = new CompoundTag();
        from.putString("fluid", "minecraft:empty");
        from.putLong("amount", 0);

        var result = FluidAmount.fromNBT(from);
        assert result.equals(FluidAmount.EMPTY()) :
            "Expect %s, Actual %s".formatted(FluidAmount.EMPTY(), result);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void waterDeserialize1(GameTestHelper helper) {
        CompoundTag from = new CompoundTag();
        from.putString("fluid", "minecraft:water");
        from.putLong("fabric_amount", 81000);

        var result = FluidAmount.fromNBT(from);
        assert result.equals(FluidAmount.BUCKET_WATER()) :
            "Expect %s, Actual %s".formatted(FluidAmount.BUCKET_WATER(), result);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void waterDeserialize2(GameTestHelper helper) {
        CompoundTag from = new CompoundTag();
        from.putString("fluid", "minecraft:water");
        from.putLong("amount", 1000);

        var result = FluidAmount.fromNBT(from);
        assert result.equals(FluidAmount.BUCKET_WATER()) :
            "Expect %s, Actual %s".formatted(FluidAmount.BUCKET_WATER(), result);
        helper.succeed();
    }
}
