package com.kotori316.fluidtank.gametest;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismGases;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.integration.mekanism_gas.GasAmount;
import com.kotori316.fluidtank.integration.mekanism_gas.TileGasTank;
import com.kotori316.fluidtank.integration.mekanism_gas.TileInfoAccess;
import com.kotori316.fluidtank.tiles.TileTank;

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
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, ModObjects.woodGasTank());
        helper.assertBlockPresent(ModObjects.woodGasTank(), pos);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void capabilityExist(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(pos, ModObjects.woodGasTank()))
            .thenExecute(() -> {
                var cap = Objects.requireNonNull((TileGasTank) helper.getBlockEntity(pos))
                    .getCapability(Capabilities.GAS_HANDLER);
                assertTrue(cap.isPresent());
            })
            .thenExecuteAfter(1, () -> {
                var tank = Objects.requireNonNull((TileGasTank) helper.getBlockEntity(pos));
                var cap = tank.getCapability(Capabilities.GAS_HANDLER).resolve();
                cap.ifPresentOrElse(h -> assertEquals(1, h.getTanks()), () -> fail("No capability at %s".formatted(tank)));
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void capabilityValid(GameTestHelper helper) {
        AtomicReference<LazyOptional<IGasHandler>> ref = new AtomicReference<>();
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(pos, ModObjects.woodGasTank()))
            .thenExecute(() -> ref.set(Objects.requireNonNull((TileGasTank) helper.getBlockEntity(pos))
                .getCapability(Capabilities.GAS_HANDLER)))
            .thenExecuteAfter(1, () -> assertFalse(ref.get().isPresent()))
            .thenExecuteAfter(1, () -> assertTrue(Objects.requireNonNull((TileGasTank) helper.getBlockEntity(pos))
                .getCapability(Capabilities.GAS_HANDLER).isPresent()))
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void place2AtOnce(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenExecute(() -> {
                helper.setBlock(pos, ModObjects.woodGasTank());
                helper.setBlock(pos.above(), ModObjects.woodGasTank());
            })
            .thenExecuteAfter(1, () -> {
                var c1 = TileInfoAccess.getConnection(helper, pos);
                var c2 = TileInfoAccess.getConnection(helper, pos.above());
                assertEquals(c1, c2);
                assertEquals(2, c1.sortedTanks().size());
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void place2AfterPlaced(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenExecute(() -> helper.setBlock(pos, ModObjects.woodGasTank()))
            .thenExecuteAfter(1, () -> {
                var c1 = TileInfoAccess.getConnection(helper, pos);
                assertEquals(1, c1.sortedTanks().size());
            })
            .thenExecuteAfter(1, () -> helper.setBlock(pos.above(), ModObjects.woodGasTank()))
            .thenExecuteAfter(1, () -> {
                var c1 = TileInfoAccess.getConnection(helper, pos);
                var c2 = TileInfoAccess.getConnection(helper, pos.above());
                assertEquals(c1, c2);
                assertEquals(2, c1.sortedTanks().size());
            })
            .thenSucceed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void loadNbt(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.startSequence()
            .thenExecute(() -> {
                helper.setBlock(pos, ModObjects.woodGasTank());
                var tile = Objects.requireNonNull((TileGasTank) helper.getBlockEntity(pos));
                var tag = new CompoundTag();
                {
                    tag.putLong(TileTank.NBT_Capacity(), 4000L);
                    var content = new CompoundTag();
                    content.putString("gasName", "mekanism:antimatter");
                    content.putLong("amount", 3000);
                    tag.put("stored", content);
                }
                tile.load(tag);
            })
            .thenExecuteAfter(1, () -> {
                var h = TileInfoAccess.getHandler(helper, pos);
                assertEquals(3000, h.getStored());
                assertEquals(4000, h.getCapacity());
                var tank = h.getTank();
                assertEquals(GasAmount.apply(MekanismGases.ANTIMATTER.get(), 3000), tank.genericAmount());
            })
            .thenSucceed();
    }
}
