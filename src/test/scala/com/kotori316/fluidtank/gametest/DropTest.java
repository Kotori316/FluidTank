package com.kotori316.fluidtank.gametest;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.recipes.RecipeInventoryUtil;

import static com.kotori316.fluidtank.gametest.Utils.EMPTY_STRUCTURE;
import static com.kotori316.fluidtank.gametest.Utils.getConnection;
import static com.kotori316.fluidtank.gametest.Utils.placeTank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
public final class DropTest {

    @GameTest(template = EMPTY_STRUCTURE)
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
            .map(f -> Utils.create("dropOfFilledTank" + f, g -> dropOfWaterTank1(g, f)))
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
}
