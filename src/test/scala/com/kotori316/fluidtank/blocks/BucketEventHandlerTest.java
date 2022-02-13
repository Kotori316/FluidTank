package com.kotori316.fluidtank.blocks;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.Tank;
import com.kotori316.fluidtank.fluids.TankHandler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BucketEventHandlerTest extends BeforeAllTest {
    @Test
    void fillLava1() {
        var handler = TankHandler.apply(4000);
        var stack = new ItemStack(Items.LAVA_BUCKET);

        var result = assertDoesNotThrow(() ->
            OptionConverters.<BucketEventHandler.TransferResult>toJava(BucketEventHandler.transferFluidTest(handler, stack))
                .orElseThrow());
        assertEquals(FluidAmount.BUCKET_LAVA().setAmount(1000L), handler.getTank().fluidAmount());
        assertEquals(Items.BUCKET, result.result().getResult().getItem());
        assertTrue(ItemStack.matches(stack, new ItemStack(Items.LAVA_BUCKET)));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 5, 16, 64, 65})
    void fillLavaN(int stackSize) {
        var handler = TankHandler.apply(4000);
        var stack = new ItemStack(Items.LAVA_BUCKET, stackSize);

        var result = assertDoesNotThrow(() ->
            OptionConverters.<BucketEventHandler.TransferResult>toJava(BucketEventHandler.transferFluidTest(handler, stack))
                .orElseThrow());
        assertEquals(FluidAmount.BUCKET_LAVA().setAmount(1000L), handler.getTank().fluidAmount());
        assertEquals(Items.BUCKET, result.result().getResult().getItem());
        assertTrue(ItemStack.matches(stack, new ItemStack(Items.LAVA_BUCKET, stackSize)));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 16, 64, 65})
    void drainLavaN(int stackSize) {
        var handler = TankHandler.apply(Tank.apply(FluidAmount.BUCKET_LAVA(), 4000));
        var stack = new ItemStack(Items.BUCKET, stackSize);

        var result = assertDoesNotThrow(() ->
            OptionConverters.<BucketEventHandler.TransferResult>toJava(BucketEventHandler.transferFluidTest(handler, stack))
                .orElseThrow());
        assertTrue(handler.getTank().isEmpty());
        assertTrue(ItemStack.matches(result.result().getResult(), new ItemStack(Items.LAVA_BUCKET, 1)));
        assertTrue(ItemStack.matches(stack, new ItemStack(Items.BUCKET, stackSize)));
    }
}
