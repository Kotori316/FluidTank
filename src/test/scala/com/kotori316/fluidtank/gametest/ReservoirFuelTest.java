package com.kotori316.fluidtank.gametest;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.GenericAmount;
import com.kotori316.fluidtank.items.ReservoirItem;
import com.kotori316.fluidtank.recipes.RecipeInventoryUtil;
import com.kotori316.testutil.GameTestUtil;

import static com.kotori316.fluidtank.gametest.DropTest.contentString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
final class ReservoirFuelTest {
    static final String BATCH = "reservoirFuelTestBatch";

    void burn(ReservoirItem reservoirItem, @Nullable GenericAmount<Fluid> amount, int expect) {
        var stack = new ItemStack(reservoirItem);
        var handler = RecipeInventoryUtil.getFluidHandler(stack);
        if (amount != null)
            handler.fill(FluidAmount.toStack(amount), IFluidHandler.FluidAction.EXECUTE);

        var result = stack.getBurnTime(RecipeType.SMELTING);
        assertEquals(expect, result);
    }

    @GameTestGenerator
    List<TestFunction> notBurn() {
        return Stream.of(FluidAmount.EMPTY(), FluidAmount.BUCKET_WATER())
            .flatMap(f -> IntStream.of(0, 10, 100, 1000, 1001, 5000).mapToObj(f::setAmount))
            .flatMap(f -> CollectionConverters.asJava(ModObjects.itemReservoirs()).stream()
                .map(i -> GameTestUtil.create(FluidTank.modID, BATCH, "notBurn_%s_%s".formatted(i.registryName().getPath(), contentString(f)),
                    () -> burn(i, f, -1))))
            .toList();
    }

    @GameTest(template = GameTestUtil.NO_PLACE_STRUCTURE, batch = BATCH)
    void nullFluid() {
        burn(ModObjects.itemReservoirs().apply(0), null, -1);
    }

    @GameTestGenerator
    List<TestFunction> lavaBurn() {
        var pairs = List.of(
            Pair.of(1000, 100 * 200),
            Pair.of(500, 50 * 200),
            Pair.of(100, 10 * 200),
            Pair.of(10, 200),
            Pair.of(0, -1)
        );
        return CollectionConverters.asJava(ModObjects.itemReservoirs()).stream()
            .flatMap(item -> pairs.stream().map(p ->
                GameTestUtil.create(FluidTank.modID, BATCH, "lavaBurn_%s_%d".formatted(item.registryName().getPath(), p.getLeft()),
                    () -> burn(item, FluidAmount.BUCKET_LAVA().setAmount(p.getLeft()), p.getRight()))
            )).toList();
    }
}
