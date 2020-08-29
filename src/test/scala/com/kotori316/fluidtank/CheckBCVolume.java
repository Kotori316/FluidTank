package com.kotori316.fluidtank;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckBCVolume {
    @Test
    void bucket() {
        FluidAmount bucket = FluidAmount.BUCKET;
        FluidAmount maybeBucket = FluidAmount.of(1000L, 1000L);
        assertEquals(bucket, maybeBucket);
    }

    @Test
    void bucket2() {
        FluidAmount bucket = FluidAmount.BUCKET.mul(2);
        FluidAmount maybeBucket = FluidAmount.of(2000L, 1000L);
        assertEquals(bucket, maybeBucket);
    }

    @Test
    void bucket3() {
        FluidAmount bucket = FluidAmount.BUCKET.add(FluidAmount.of(3, 2));
        FluidAmount maybeBucket = FluidAmount.of(2500L, 1000L);
        assertEquals(bucket, maybeBucket);
    }

    @Test
    void toLong() {
        FluidAmount bucket = FluidAmount.BUCKET;
        assertAll(
            () -> assertEquals(1000L, bucket.asLong(1000L)),
            () -> assertEquals(2000L, bucket.mul(2).asLong(1000L)),
            () -> assertEquals(2500L, bucket.add(FluidAmount.of(3, 2)).asLong(1000L))
        );
    }
}
