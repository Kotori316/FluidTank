package com.kotori316.fluidtank.gametest;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.integration.mekanism_gas.TileGasTank;

import static com.kotori316.testutil.GameTestUtil.EMPTY_STRUCTURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
public final class PlaceMekanismTankTest {
    static final String BATCH = PlaceTest.BATCH;

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void placeWood(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, ModObjects.woodGasTank());
        helper.assertBlockPresent(ModObjects.woodGasTank(), BlockPos.ZERO);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void capabilityExist(GameTestHelper helper) {
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(BlockPos.ZERO, ModObjects.woodGasTank()))
            .thenExecute(() -> {
                var cap = Objects.requireNonNull((TileGasTank) helper.getBlockEntity(BlockPos.ZERO))
                    .getCapability(Capabilities.GAS_HANDLER_CAPABILITY);
                assertTrue(cap.isPresent());
            })
            .thenExecuteAfter(1, () -> {
                var tank = Objects.requireNonNull((TileGasTank) helper.getBlockEntity(BlockPos.ZERO));
                var cap = tank.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).resolve();
                cap.ifPresentOrElse(h -> assertEquals(1, h.getTanks()), () -> fail("No capability at %s".formatted(tank)));
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void capabilityValid(GameTestHelper helper) {
        AtomicReference<LazyOptional<IGasHandler>> ref = new AtomicReference<>();
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(BlockPos.ZERO, ModObjects.woodGasTank()))
            .thenExecute(() -> ref.set(Objects.requireNonNull((TileGasTank) helper.getBlockEntity(BlockPos.ZERO))
                .getCapability(Capabilities.GAS_HANDLER_CAPABILITY)))
            .thenExecuteAfter(1, () -> assertFalse(ref.get().isPresent()))
            .thenExecuteAfter(1, () -> assertTrue(Objects.requireNonNull((TileGasTank) helper.getBlockEntity(BlockPos.ZERO))
                .getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent()))
            .thenSucceed();
    }
}
