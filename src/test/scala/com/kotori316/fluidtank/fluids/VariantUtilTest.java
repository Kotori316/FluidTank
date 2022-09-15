package com.kotori316.fluidtank.fluids;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VariantUtilTest {
    @ParameterizedTest
    @MethodSource("pairs")
    void convertToFabric(LongPair pair) {
        var actual = VariantUtil.convertForgeAmountToFabric(pair.forgeAmount);
        assertEquals(pair.fabricAmount, actual);
    }

    @ParameterizedTest
    @MethodSource("pairs")
    void convertToForge(LongPair pair) {
        var actual = VariantUtil.convertFabricAmountToForge(pair.fabricAmount);
        assertEquals(pair.forgeAmount, actual);
    }

    @ParameterizedTest
    @MethodSource("pairs")
    void cycle(LongPair pair) {
        assertAll(
            () -> assertEquals(pair.forgeAmount, VariantUtil.convertFabricAmountToForge(VariantUtil.convertForgeAmountToFabric(pair.forgeAmount))),
            () -> assertEquals(pair.fabricAmount, VariantUtil.convertForgeAmountToFabric(VariantUtil.convertFabricAmountToForge(pair.fabricAmount)))
        );
    }

    static List<LongPair> pairs() {
        return List.of(
            new LongPair(0, 0),
            new LongPair(81000, 1000),
            new LongPair(84934656000L, 1048576000L),
            new LongPair(9223372036854775782L, 113868790578454022L)
        );
    }

    record LongPair(long fabricAmount, long forgeAmount) {
    }
}
