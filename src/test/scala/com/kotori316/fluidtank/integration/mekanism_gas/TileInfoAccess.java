package com.kotori316.fluidtank.integration.mekanism_gas;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import org.jetbrains.annotations.VisibleForTesting;
import org.opentest4j.AssertionFailedError;

public class TileInfoAccess {
    @VisibleForTesting
    public static GasConnection getConnection(GameTestHelper helper, BlockPos relatedPos) {
        return Optional.ofNullable((TileGasTank) helper.getBlockEntity(relatedPos))
            .map(TileGasTank::tileInfo)
            .map(i -> (TileInfo.Holder) i.getHolder())
            .map(h -> h.gasConnection)
            .orElseThrow(() -> new AssertionFailedError("No connection at %s".formatted(relatedPos)));
    }

    @VisibleForTesting
    public static GasTankHandler getHandler(GameTestHelper helper, BlockPos relatedPos) {
        return Optional.ofNullable((TileGasTank) helper.getBlockEntity(relatedPos))
            .map(TileGasTank::tileInfo)
            .map(i -> (TileInfo.Holder) i.getHolder())
            .map(h -> h.gasTankHandler)
            .orElseThrow(() -> new AssertionFailedError("No handler at %s".formatted(relatedPos)));
    }
}
