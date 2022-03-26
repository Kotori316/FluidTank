package com.kotori316.fluidtank.gametest;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.recipes.RecipeInventoryUtil;

import static com.kotori316.fluidtank.gametest.Utils.EMPTY_STRUCTURE;
import static com.kotori316.fluidtank.gametest.Utils.getConnection;
import static com.kotori316.fluidtank.gametest.Utils.modID;
import static com.kotori316.fluidtank.gametest.Utils.placeTank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@GameTestHolder(value = modID)
@PrefixGameTestTemplate(value = false)
public final class DropTest {
    static final String BATCH = "dropTestBatch";

    @BeforeBatch(batch = BATCH)
    public void beforeTest(ServerLevel level) {
        com.kotori316.fluidtank.Utils.setInDev(false);
    }

    @AfterBatch(batch = BATCH)
    public void afterTest(ServerLevel level) {
        com.kotori316.fluidtank.Utils.setInDev(true);
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void dropOfEmptyTank(GameTestHelper helper) {
        var pos = BlockPos.ZERO.east();
        placeTank(helper, pos, ModObjects.blockTanks().head());

        var drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
            helper.getBlockEntity(pos), helper.makeMockPlayer(), ItemStack.EMPTY);
        assertEquals(1, drops.size(), "Drop was " + drops);
        var stack = drops.get(0);
        assertEquals(ModObjects.blockTanks().head().itemBlock(), stack.getItem(), "Dropped item was " + stack);
        assertFalse(stack.hasTag(), "Stack must not have tag if dropped from empty tank. " + stack);

        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> dropOfFilledTank() {
        return Stream.of(
                FluidAmount.BUCKET_WATER(),
                FluidAmount.BUCKET_LAVA()
            ).flatMap(f -> IntStream.of(500, 1000, 1500, 2000, 3000, 4000).mapToObj(f::setAmount))
            .map(f -> Utils.create("dropOfFilledTank" + f, BATCH, g -> dropOfWaterTank1(g, f)))
            .toList();
    }

    void dropOfWaterTank1(GameTestHelper helper, FluidAmount amount) {
        var pos = BlockPos.ZERO.east();
        placeTank(helper, pos, ModObjects.blockTanks().head());
        var connection = getConnection(helper, pos);
        connection.handler().fill(amount, IFluidHandler.FluidAction.EXECUTE);

        var drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
            helper.getBlockEntity(pos), helper.makeMockPlayer(), ItemStack.EMPTY);
        assertEquals(1, drops.size(), "Drop was " + drops);
        var stack = drops.get(0);
        assertEquals(ModObjects.blockTanks().head().itemBlock(), stack.getItem(), "Dropped item was " + stack);
        var itemTank = RecipeInventoryUtil.getFluidHandler(stack);
        assertEquals(amount, itemTank.getFluid(), "Fluid must be given fluid. " + itemTank.getFluid());

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void dropOfLavaTank2(GameTestHelper helper) {
        var pos = BlockPos.ZERO.east();
        placeTank(helper, pos, ModObjects.blockTanks().head());
        var connection = getConnection(helper, pos);
        connection.handler().fill(FluidAmount.BUCKET_LAVA(), IFluidHandler.FluidAction.EXECUTE);
        var drops = Block.getDrops(helper.getBlockState(pos), helper.getLevel(), helper.absolutePos(pos),
            helper.getBlockEntity(pos), helper.makeMockPlayer(), ItemStack.EMPTY);
        assertEquals(1, drops.size(), "Drop was " + drops);
        var stack = drops.get(0);

        var entityTag = BlockItem.getBlockEntityData(stack);
        assertNotNull(entityTag, "BE tag must not be null. " + stack);
        // language=json
        var expected = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, GsonHelper.parse("""
            {
              "tier": "wood",
              "tank": {
                "amount": 1000,
                "fluid": "minecraft:lava",
                "capacity": 4000
              }
            }"""));
        ((CompoundTag) expected).getCompound("tank").putLong("amount", 1000L); // replace int value to long
        ((CompoundTag) expected).getCompound("tank").putLong("capacity", 4000L); // replace int value to long
        assertEquals(expected, entityTag);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void pickLavaTank3(GameTestHelper helper) {
        var pos = BlockPos.ZERO.east();
        placeTank(helper, pos, ModObjects.blockTanks().head());
        var connection = getConnection(helper, pos);
        connection.handler().fill(FluidAmount.BUCKET_LAVA(), IFluidHandler.FluidAction.EXECUTE);

        var stack = new ItemStack(ModObjects.blockTanks().head());
        ModObjects.blockTanks().head().saveTankNBT(helper.getBlockEntity(pos), stack);

        var entityTag = BlockItem.getBlockEntityData(stack);
        assertNotNull(entityTag, "BE tag must not be null. " + stack);
        // language=json
        var expected = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, GsonHelper.parse("""
            {
              "tier": "wood",
              "tank": {
                "amount": 1000,
                "fluid": "minecraft:lava",
                "capacity": 4000
              }
            }"""));
        ((CompoundTag) expected).getCompound("tank").putLong("amount", 1000L); // replace int value to long
        ((CompoundTag) expected).getCompound("tank").putLong("capacity", 4000L); // replace int value to long
        assertEquals(expected, entityTag);
        helper.succeed();
    }
}
