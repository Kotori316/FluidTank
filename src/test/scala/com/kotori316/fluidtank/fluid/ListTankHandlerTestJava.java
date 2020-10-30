package com.kotori316.fluidtank.fluid;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import cats.data.Chain;
import javax.annotation.Nonnull;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import scala.Option;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.ListTankHandler;
import com.kotori316.fluidtank.fluids.Tank;
import com.kotori316.fluidtank.fluids.TankHandler;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test calls {@link ListTankHandler} from Java.
 */
class ListTankHandlerTestJava extends BeforeAllTest {
    static final Tank EMPTY_TANK = new Tank(FluidAmount.EMPTY(), 4000L);

    //-------------------- PARAMETER PROVIDERS --------------------
    static Fluid[] fluids() {
        return new Fluid[]{Fluids.WATER, Fluids.LAVA};
    }

    static Object[] fluidWithAmount24() {
        return Stream.of(fluids())
            .flatMap(f -> IntStream.iterate(1000, operand -> operand + 1000).limit(24).mapToObj(i -> new Object[]{f, i}))
            .toArray();
    }

    //-------------------- TESTS --------------------
    @Test
    void tankProperty() {
        ListTankHandler handler = createEmptyHandler();
        assertAll(
            () -> assertTrue(handler.getTankList().forall(t -> t.equals(EMPTY_TANK))),
            () -> assertEquals(8000, handler.getSumOfCapacity()),
            () -> assertEquals(8000, handler.getTankCapacity(0)),
            () -> assertTrue(handler.getFluidInTank(0).isEmpty())
        );
    }

    static class FillSimulate {

        static Object[] fluidWithAmount8() {
            return Stream.of(fluids())
                .flatMap(f -> IntStream.iterate(1000, operand -> operand + 1000).limit(8).mapToObj(i -> new Object[]{f, i}))
                .toArray();
        }

        @ParameterizedTest
        @MethodSource("fluidWithAmount8")
        void fillSimulate(Fluid fluid, long amount) {
            ListTankHandler handler = createEmptyHandler();
            FluidAmount toFill = FluidAmount.apply(fluid, amount, Option.empty());
            FluidAmount filled = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
            assertEquals(toFill, filled, String.format("Test of %s, %d", fluid, amount));
        }

    }

    static class FillSimulateOver {

        static Object[] fluidWithAmount9to24() {
            return Stream.of(fluids())
                .flatMap(f -> IntStream.iterate(9000, operand -> operand + 1000).limit(24 - 9 + 1).mapToObj(i -> new Object[]{f, i}))
                .toArray();
        }

        @ParameterizedTest
        @MethodSource("fluidWithAmount9to24")
        void fillSimulateOver(Fluid fluid, long amount) {
            ListTankHandler handler = createEmptyHandler();
            FluidAmount toFill = FluidAmount.apply(fluid, amount, Option.empty());
            {
                FluidAmount filled = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
                assertEquals(FluidAmount.apply(fluid, 8000, Option.empty()), filled, String.format("Test of %s, %d", fluid, amount));
            }
            {
                FluidAmount filled = handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                assertEquals(FluidAmount.apply(fluid, 8000, Option.empty()), filled, String.format("Test of %s, %d", fluid, amount));
                assertEquals(asList(new Tank(FluidAmount.apply(fluid, 4000, Option.empty()), 4000),
                    new Tank(FluidAmount.apply(fluid, 4000, Option.empty()), 4000)), handler.getTankList().toList());
            }
        }

    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1000L, 10000L, 100000L, Integer.MAX_VALUE, Long.MAX_VALUE})
    void fillEmpty(long amount) {
        ListTankHandler handler = createEmptyHandler();
        FluidAmount toFill = FluidAmount.EMPTY().setAmount(amount);

        FluidAmount filled = handler.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
        assertEquals(FluidAmount.EMPTY(), filled, String.format("Test of %s, %d", FluidAmount.EMPTY(), amount));
    }

    static class DrainInt {
        static Object[] fluidWithAmount24() {
            return ListTankHandlerTestJava.fluidWithAmount24();
        }

        @ParameterizedTest
        @MethodSource("fluidWithAmount24")
        void drainInt(Fluid tankContent, int drainAmount) {
            IFluidHandler handler = new ListTankHandler(Chain.fromSeq(asList(
                new Tank(FluidAmount.apply(tankContent, 4000, Option.empty()), 4000),
                new Tank(FluidAmount.apply(tankContent, 4000, Option.empty()), 4000)
            )).map(TankHandler::apply));
            FluidStack drained = handler.drain(drainAmount, IFluidHandler.FluidAction.SIMULATE);
            assertEquals(FluidAmount.apply(tankContent, Math.min(drainAmount, 8000), Option.empty()), FluidAmount.fromStack(drained),
                String.format("Test of %s, %d", tankContent, drainAmount));
        }
    }

    static class DrainWater {
        static Object[] fluidWithAmount24() {
            return ListTankHandlerTestJava.fluidWithAmount24();
        }

        @ParameterizedTest
        @MethodSource("fluidWithAmount24")
        void drainWater(Fluid drain, int drainAmount) {
            IFluidHandler handler = new ListTankHandler(Chain.fromSeq(asList(
                new Tank(FluidAmount.BUCKET_WATER().setAmount(4000), 4000),
                new Tank(FluidAmount.BUCKET_WATER().setAmount(4000), 4000)
            )).map(TankHandler::apply));

            FluidStack drained = handler.drain(new FluidStack(drain, drainAmount), IFluidHandler.FluidAction.SIMULATE);
            if (drain == Fluids.WATER) {
                assertEquals(FluidAmount.apply(drain, Math.min(drainAmount, 8000), Option.empty()), FluidAmount.fromStack(drained),
                    String.format("Test of %s, %d", drain, drainAmount));
            } else {
                assertEquals(FluidAmount.apply(drain, 0, Option.empty()), FluidAmount.fromStack(drained),
                    String.format("Test of %s, %d", drain, drainAmount));
            }
        }
    }

    static class DrainLava {
        static Object[] fluidWithAmount24() {
            return ListTankHandlerTestJava.fluidWithAmount24();
        }

        @ParameterizedTest
        @MethodSource("fluidWithAmount24")
        void drainLava(Fluid drain, int drainAmount) {
            IFluidHandler handler = new ListTankHandler(Chain.fromSeq(asList(
                new Tank(FluidAmount.BUCKET_LAVA().setAmount(4000), 4000),
                new Tank(FluidAmount.BUCKET_LAVA().setAmount(4000), 4000)
            )).map(TankHandler::apply));

            FluidStack drained = handler.drain(new FluidStack(drain, drainAmount), IFluidHandler.FluidAction.SIMULATE);
            if (drain == Fluids.LAVA) {
                assertEquals(FluidAmount.apply(drain, Math.min(drainAmount, 8000), Option.empty()), FluidAmount.fromStack(drained),
                    String.format("Test of %s, %d", drain, drainAmount));
            } else {
                assertEquals(FluidAmount.apply(drain, 0, Option.empty()), FluidAmount.fromStack(drained),
                    String.format("Test of %s, %d", drain, drainAmount));
            }
        }
    }

    static class DrainEmpty {
        static List<FluidAmount> tankContents() {
            return Stream.of(FluidAmount.BUCKET_WATER(), FluidAmount.BUCKET_LAVA())
                .flatMap(f -> IntStream.of(1000, 4000, 8000, Integer.MAX_VALUE).mapToObj(f::setAmount))
                .collect(Collectors.toList());
        }

        @ParameterizedTest
        @MethodSource("tankContents")
        void drainFromFilledTank(FluidAmount filled) {
            IFluidHandler handler = new ListTankHandler(Chain.fromSeq(asList(
                new Tank(filled, 4000),
                new Tank(filled, 4000)
            )).map(TankHandler::apply));

            assertAll(
                () -> assertEquals(FluidAmount.EMPTY(), FluidAmount.fromStack(handler.drain(0, IFluidHandler.FluidAction.SIMULATE)),
                    String.format("Drained 0 from %s tank", filled)),
                () -> assertEquals(FluidAmount.EMPTY(), FluidAmount.fromStack(handler.drain(FluidStack.EMPTY, IFluidHandler.FluidAction.SIMULATE)),
                    String.format("Drained 0 from %s tank", filled))
            );
        }
    }

    //-------------------- Utility methods --------------------
    @Nonnull
    private static ListTankHandler createEmptyHandler() {
        return new ListTankHandler(Chain.fromSeq(asList(EMPTY_TANK, EMPTY_TANK).map(TankHandler::apply)));
    }

    @SafeVarargs
    static <T> scala.collection.immutable.List<T> asList(T... ts) {
        scala.collection.mutable.Builder<T, scala.collection.immutable.List<T>> builder = scala.collection.immutable.List.newBuilder();
        for (T t : ts) {
            builder.addOne(t);
        }
        return builder.result();
    }
}