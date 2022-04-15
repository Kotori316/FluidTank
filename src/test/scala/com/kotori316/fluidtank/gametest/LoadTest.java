package com.kotori316.fluidtank.gametest;

import java.util.Objects;

import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.fluidtank.tiles.TileTank;
import com.kotori316.testutil.GameTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
public final class LoadTest {
    static final Block STONE_TANK;
    static final String BATCH = "loadTestBatch";
    static final String TANK2_STRUCTURE = "tank2";

    static {
        STONE_TANK = Objects.requireNonNull(
            ForgeRegistries.BLOCKS.getValue(new ResourceLocation(FluidTank.modID, "tank_stone"))
        );
    }

    @BeforeBatch(batch = BATCH)
    public void beforeTest(ServerLevel level) {
        com.kotori316.fluidtank.Utils.setInDev(false);
    }

    @AfterBatch(batch = BATCH)
    public void afterTest(ServerLevel level) {
        com.kotori316.fluidtank.Utils.setInDev(true);
    }

    @GameTest(template = TANK2_STRUCTURE, batch = BATCH)
    public void stoneTankPresent(GameTestHelper helper) {
        var pos = GameTestUtil.getBasePos(helper).offset(1, 0, 0);
        helper.assertBlockPresent(STONE_TANK, pos);
        helper.assertBlockPresent(STONE_TANK, pos.above());
        helper.succeed();
    }

    @GameTest(template = TANK2_STRUCTURE, batch = BATCH)
    public void stoneTankTile(GameTestHelper helper) {
        var pos = GameTestUtil.getBasePos(helper).offset(1, 0, 0);
        var bottomTank = helper.getBlockEntity(pos);
        if (bottomTank instanceof TileTank tank) {
            assertTrue(tank.connection().isDummy());
        } else {
            fail("Tank entity is not presented at %s.".formatted(helper.absolutePos(pos)));
        }
        helper.succeed();
    }

    @GameTest(template = TANK2_STRUCTURE, batch = BATCH)
    public void stoneTankTile2(GameTestHelper helper) {
        var pos = GameTestUtil.getBasePos(helper).offset(1, 0, 0);
        var bottomTank = helper.getBlockEntity(pos);
        if (bottomTank instanceof TileTank tank) {
            tank.onBlockPlacedBy();
            assertFalse(tank.connection().isDummy());
            assertEquals(2, tank.connection().seq().size());
            assertEquals(24000L, tank.connection().amount());
            assertEquals(Fluids.WATER, tank.connection().fluidType().fluid());
        } else {
            fail("Tank entity is not presented at %s.".formatted(helper.absolutePos(pos)));
        }
        helper.succeed();
    }

    @GameTest(template = TANK2_STRUCTURE, batch = BATCH)
    public void woodTank1(GameTestHelper helper) {
        var pos = GameTestUtil.getBasePos(helper).offset(1, 1, 2);
        var bottomTank = helper.getBlockEntity(pos);
        if (bottomTank instanceof TileTank tank) {
            tank.onBlockPlacedBy();
            var connection = tank.connection();
            assertFalse(tank.connection().isDummy(), "Connection shouldn't be null. T: %s %s".formatted(tank, tank.internalTank()));
            assertEquals(Tier.WOOD.amount() * 2L, connection.amount(), "Connection: %s".formatted(connection));
            assertEquals(Tier.WOOD.amount() * 2L, connection.capacity(), "Connection: %s".formatted(connection));
        } else {
            fail("Tank entity is not presented at %s.".formatted(helper.absolutePos(pos)));
        }
        helper.succeed();
    }
}
