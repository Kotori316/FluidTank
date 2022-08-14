package com.kotori316.fluidtank.tiles;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.kotori316.fluidtank.BeforeAllTest;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.items.TankItemFluidHandler;
import com.kotori316.fluidtank.recipes.RecipeInventoryUtil;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidHandlerWrapperTest extends BeforeAllTest {
    private final BlockTank woodTank = ModObjects.tierToBlock().apply(Tier.WOOD);

    @Test
    void emptyInventory() {
        var items = new EmptyHandler();
        var handler = new CATTile.FluidHandlerWrapper(items);
        assertEquals(0, handler.getTanks());
        assertEquals(List.of(), handler.fluidList());
    }

    @Test
    void oneBucketInventory() {
        var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.BUCKET)));
        var handler = new CATTile.FluidHandlerWrapper(items);
        assertEquals(1, handler.getTanks());
        assertEquals(1000, handler.getTankCapacity(0));
        assertEquals(List.of(), handler.fluidList());
    }

    @Test
    void oneTankInventory() {
        var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(woodTank)));
        var handler = new CATTile.FluidHandlerWrapper(items);
        assertEquals(1, handler.getTanks());
        assertEquals(woodTank.tier().amount(), handler.getTankCapacity(0));
        assertEquals(List.of(), handler.fluidList());
    }

    @Test
    void oneWaterBucketInventory() {
        var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET)));
        var handler = new CATTile.FluidHandlerWrapper(items);
        assertEquals(1, handler.getTanks());
        assertEquals(1000, handler.getTankCapacity(0));
        assertEquals(List.of(FluidAmount.BUCKET_WATER()), handler.fluidList());
    }

    @Test
    void twoWaterBucketInventory() {
        var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.WATER_BUCKET)));
        var handler = new CATTile.FluidHandlerWrapper(items);
        assertEquals(2, handler.getTanks());
        assertEquals(1000, handler.getTankCapacity(0));
        assertEquals(List.of(FluidAmount.BUCKET_WATER().setAmount(2000)), handler.fluidList());
    }

    @Test
    void twoBucketInventory() {
        var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.LAVA_BUCKET)));
        var handler = new CATTile.FluidHandlerWrapper(items);
        assertEquals(2, handler.getTanks());
        assertEquals(1000, handler.getTankCapacity(0));
        assertEquals(List.of(FluidAmount.BUCKET_WATER(), FluidAmount.BUCKET_LAVA()), handler.fluidList());
    }

    @Nested
    class FillTest {
        @Test
        void fillEmpty() {
            var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.BUCKET)));
            var handler = new CATTile.FluidHandlerWrapper(items);
            var filled = handler.fill(FluidAmount.EMPTY().toStack(), IFluidHandler.FluidAction.SIMULATE);
            assertEquals(0, filled);
        }

        @Test
        void water1000BucketSimulate() {
            var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.BUCKET)));
            var handler = new CATTile.FluidHandlerWrapper(items);
            var filled = handler.fill(FluidAmount.BUCKET_WATER().toStack(), IFluidHandler.FluidAction.SIMULATE);
            assertEquals(1000, filled);
            assertEquals(Items.BUCKET, items.getStackInSlot(0).getItem());
        }

        @Test
        void water1000Bucket() {
            var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.BUCKET)));
            var handler = new CATTile.FluidHandlerWrapper(items);
            var filled = handler.fill(FluidAmount.BUCKET_WATER().toStack(), IFluidHandler.FluidAction.EXECUTE);
            assertEquals(1000, filled);
            assertEquals(Items.WATER_BUCKET, items.getStackInSlot(0).getItem());
        }

        @Test
        void water1000Bucket2() {
            var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.BUCKET), new ItemStack(Items.BUCKET)));
            var handler = new CATTile.FluidHandlerWrapper(items);
            var filled = handler.fill(FluidAmount.BUCKET_WATER().toStack(), IFluidHandler.FluidAction.EXECUTE);
            assertEquals(1000, filled);
            assertEquals(Items.WATER_BUCKET, items.getStackInSlot(0).getItem());
            assertEquals(Items.BUCKET, items.getStackInSlot(1).getItem());
        }

        @Test
        void water2000Bucket2() {
            var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.BUCKET), new ItemStack(Items.BUCKET)));
            var handler = new CATTile.FluidHandlerWrapper(items);
            var filled = handler.fill(FluidAmount.BUCKET_WATER().setAmount(2000).toStack(), IFluidHandler.FluidAction.EXECUTE);
            assertEquals(2000, filled);
            assertEquals(Items.WATER_BUCKET, items.getStackInSlot(0).getItem());
            assertEquals(Items.WATER_BUCKET, items.getStackInSlot(1).getItem());
        }

        @Test
        void water1500Bucket2() {
            var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.BUCKET), new ItemStack(Items.BUCKET)));
            var handler = new CATTile.FluidHandlerWrapper(items);
            var filled = handler.fill(FluidAmount.BUCKET_WATER().setAmount(1500).toStack(), IFluidHandler.FluidAction.EXECUTE);
            assertEquals(1000, filled, "Just one bucket must be filled and the other must be empty.");
            assertEquals(Items.WATER_BUCKET, items.getStackInSlot(0).getItem());
            assertEquals(Items.BUCKET, items.getStackInSlot(1).getItem());
        }

        @Test
        void water1500Tank() {
            var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.BUCKET), new ItemStack(woodTank)));
            var handler = new CATTile.FluidHandlerWrapper(items);
            var filled = handler.fill(FluidAmount.BUCKET_WATER().setAmount(1500).toStack(), IFluidHandler.FluidAction.EXECUTE);
            assertEquals(1500, filled);
            assertEquals(Items.WATER_BUCKET, items.getStackInSlot(0).getItem());
            var tankHandler = new TankItemFluidHandler(woodTank.tier(), items.getStackInSlot(1));
            assertEquals(FluidAmount.BUCKET_WATER().setAmount(500), tankHandler.getFluid());
        }

        @Test
        void water500Tank() {
            var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.BUCKET), new ItemStack(woodTank)));
            var handler = new CATTile.FluidHandlerWrapper(items);
            var filled = handler.fill(FluidAmount.BUCKET_WATER().setAmount(500).toStack(), IFluidHandler.FluidAction.EXECUTE);
            var tankHandler = new TankItemFluidHandler(woodTank.tier(), items.getStackInSlot(1));
            assertAll(
                () -> assertEquals(500, filled),
                () -> assertEquals(Items.BUCKET, items.getStackInSlot(0).getItem()),
                () -> assertEquals(FluidAmount.BUCKET_WATER().setAmount(500), tankHandler.getFluid())
            );
        }

        @Test
        void fillLava() {
            var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET)));
            var handler = new CATTile.FluidHandlerWrapper(items);
            var filled = handler.fill(FluidAmount.BUCKET_LAVA().toStack(), IFluidHandler.FluidAction.EXECUTE);
            assertEquals(1000, filled);
            assertEquals(Items.LAVA_BUCKET, items.getStackInSlot(1).getItem());
        }
    }

    @Nested
    class DrainTest {
        @Nested
        class DrainAnyTest {
            @ParameterizedTest
            @ValueSource(ints = {0, 1, 500, 1000, 2000, 16000})
            void drainFromEmpty(int amount) {
                var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.BUCKET), new ItemStack(Items.BUCKET)));
                var handler = new CATTile.FluidHandlerWrapper(items);
                var drained = handler.drain(amount, IFluidHandler.FluidAction.EXECUTE);
                assertTrue(drained.isEmpty());
            }

            @Test
            void drain0FromWater() {
                var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET)));
                var handler = new CATTile.FluidHandlerWrapper(items);
                var drained = handler.drain(0, IFluidHandler.FluidAction.EXECUTE);
                assertTrue(drained.isEmpty());
                assertEquals(Items.WATER_BUCKET, items.getStackInSlot(0).getItem());
            }

            @ParameterizedTest
            @ValueSource(ints = {1000, 1001, 2000, 16000})
            void drain1000FromWater(int amount) {
                var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET)));
                var handler = new CATTile.FluidHandlerWrapper(items);
                var drained = handler.drain(amount, IFluidHandler.FluidAction.EXECUTE);
                assertEquals(FluidAmount.BUCKET_WATER(), FluidAmount.fromStack(drained));
                assertEquals(Items.BUCKET, items.getStackInSlot(0).getItem());
            }

            @ParameterizedTest
            @ValueSource(ints = {1000, 1001, 2000, 16000})
            void drain1000FromWaterSimulate(int amount) {
                var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET)));
                var handler = new CATTile.FluidHandlerWrapper(items);
                var drained = handler.drain(amount, IFluidHandler.FluidAction.SIMULATE);
                assertEquals(FluidAmount.BUCKET_WATER(), FluidAmount.fromStack(drained));
                assertEquals(Items.WATER_BUCKET, items.getStackInSlot(0).getItem());
            }

            @ParameterizedTest
            @ValueSource(ints = {1000, 1001, 2000, 16000})
            void drain1000FromLava(int amount) {
                var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.BUCKET)));
                var handler = new CATTile.FluidHandlerWrapper(items);
                var drained = handler.drain(amount, IFluidHandler.FluidAction.EXECUTE);
                assertEquals(FluidAmount.BUCKET_LAVA(), FluidAmount.fromStack(drained));
                assertEquals(Items.BUCKET, items.getStackInSlot(0).getItem());
            }

            @ParameterizedTest
            @MethodSource("com.kotori316.fluidtank.fluids.TransferOperationTest#normalFluids")
            void drain1500FromTank(FluidAmount amount) {
                ItemStackHandler items;
                {
                    var tank1 = new ItemStack(woodTank);
                    var tank2 = new ItemStack(woodTank);
                    var h1 = new TankItemFluidHandler(woodTank.tier(), tank1);
                    var h2 = new TankItemFluidHandler(woodTank.tier(), tank2);
                    h1.fill(amount.setAmount(800).toStack(), IFluidHandler.FluidAction.EXECUTE);
                    h2.fill(amount.setAmount(1000).toStack(), IFluidHandler.FluidAction.EXECUTE);
                    items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, tank1, tank2));
                }
                var handler = new CATTile.FluidHandlerWrapper(items);
                var drained = handler.drain(1500, IFluidHandler.FluidAction.EXECUTE);
                assertEquals(amount.setAmount(1500), FluidAmount.fromStack(drained));
                var h1 = new TankItemFluidHandler(woodTank.tier(), items.getStackInSlot(0));
                var h2 = new TankItemFluidHandler(woodTank.tier(), items.getStackInSlot(1));
                assertTrue(h1.getFluid().isEmpty());
                assertEquals(amount.setAmount(300), h2.getFluid());
            }

            @Test
            void drainFromMixed1() {
                ItemStackHandler items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY,
                    RecipeInventoryUtil.getFilledTankStack(Tier.WOOD, FluidAmount.BUCKET_LAVA().setAmount(800)),
                    RecipeInventoryUtil.getFilledTankStack(Tier.WOOD, FluidAmount.BUCKET_WATER().setAmount(1000)),
                    RecipeInventoryUtil.getFilledTankStack(Tier.WOOD, FluidAmount.BUCKET_LAVA().setAmount(1000))
                ));
                var handler = new CATTile.FluidHandlerWrapper(items);

                assertEquals(FluidAmount.BUCKET_LAVA().setAmount(1500), FluidAmount.fromStack(
                    handler.drain(1500, IFluidHandler.FluidAction.SIMULATE)));
                assertEquals(FluidAmount.BUCKET_LAVA().setAmount(1500), FluidAmount.fromStack(
                    handler.drain(1500, IFluidHandler.FluidAction.EXECUTE)));

                assertAll(
                    () -> assertTrue(RecipeInventoryUtil.getFluidHandler(items.getStackInSlot(0)).getFluid().isEmpty()),
                    () -> assertEquals(FluidAmount.BUCKET_WATER(), RecipeInventoryUtil.getFluidHandler(items.getStackInSlot(1)).getFluid()),
                    () -> assertEquals(FluidAmount.BUCKET_LAVA().setAmount(300), RecipeInventoryUtil.getFluidHandler(items.getStackInSlot(2)).getFluid())
                );
            }
        }

        @Nested
        class DrainFluid {
            @TestFactory
            Stream<DynamicTest> drainFromEmpty() {
                return Stream.of(FluidStack.EMPTY, FluidAmount.BUCKET_WATER().toStack(), FluidAmount.BUCKET_LAVA().toStack())
                    .map(f -> DynamicTest.dynamicTest(f.getDisplayName().getString(), () -> drainFromEmpty(f)));
            }

            void drainFromEmpty(FluidStack stack) {
                var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.BUCKET), new ItemStack(Items.BUCKET)));
                var handler = new CATTile.FluidHandlerWrapper(items);
                var drained = handler.drain(stack, IFluidHandler.FluidAction.EXECUTE);
                assertTrue(drained.isEmpty());
            }

            @ParameterizedTest
            @ValueSource(ints = {1000, 1001, 2000, 16000})
            void drain1000FromWaterSimulate(int amount) {
                var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET)));
                var handler = new CATTile.FluidHandlerWrapper(items);
                var drained = handler.drain(FluidAmount.BUCKET_WATER().setAmount(amount).toStack(), IFluidHandler.FluidAction.SIMULATE);
                assertEquals(FluidAmount.BUCKET_WATER(), FluidAmount.fromStack(drained));
                assertEquals(Items.WATER_BUCKET, items.getStackInSlot(0).getItem());
            }

            @ParameterizedTest
            @ValueSource(ints = {1000, 1001, 2000, 16000})
            void drain1000FromWater(int amount) {
                var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET)));
                var handler = new CATTile.FluidHandlerWrapper(items);
                var drained = handler.drain(FluidAmount.BUCKET_WATER().setAmount(amount).toStack(), IFluidHandler.FluidAction.EXECUTE);
                assertEquals(FluidAmount.BUCKET_WATER(), FluidAmount.fromStack(drained));
                assertEquals(Items.BUCKET, items.getStackInSlot(0).getItem());
            }

            @Test
            void drainLavaFromWater() {
                var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET)));
                var handler = new CATTile.FluidHandlerWrapper(items);
                var drained = handler.drain(FluidAmount.BUCKET_LAVA().toStack(), IFluidHandler.FluidAction.EXECUTE);
                assertTrue(FluidAmount.fromStack(drained).isEmpty());
                assertEquals(Items.WATER_BUCKET, items.getStackInSlot(0).getItem());
            }

            @Test
            void drainLava() {
                var items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.LAVA_BUCKET)));
                var handler = new CATTile.FluidHandlerWrapper(items);
                var drained = handler.drain(FluidAmount.BUCKET_LAVA().toStack(), IFluidHandler.FluidAction.EXECUTE);
                assertEquals(FluidAmount.BUCKET_LAVA(), FluidAmount.fromStack(drained));
                assertEquals(Items.WATER_BUCKET, items.getStackInSlot(0).getItem());
                assertEquals(Items.BUCKET, items.getStackInSlot(1).getItem());
            }

            @ParameterizedTest
            @MethodSource("com.kotori316.fluidtank.fluids.TransferOperationTest#normalFluids")
            void drain1500FromTank(FluidAmount amount) {
                ItemStackHandler items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY,
                    RecipeInventoryUtil.getFilledTankStack(Tier.WOOD, amount.setAmount(800)),
                    RecipeInventoryUtil.getFilledTankStack(Tier.WOOD, amount.setAmount(1000))
                ));
                var handler = new CATTile.FluidHandlerWrapper(items);
                var toDrain = amount.setAmount(1500);
                var drained = handler.drain(toDrain.toStack(), IFluidHandler.FluidAction.EXECUTE);
                assertEquals(toDrain, FluidAmount.fromStack(drained));
                var h1 = new TankItemFluidHandler(woodTank.tier(), items.getStackInSlot(0));
                var h2 = new TankItemFluidHandler(woodTank.tier(), items.getStackInSlot(1));
                assertTrue(h1.getFluid().isEmpty());
                assertEquals(amount.setAmount(300), h2.getFluid());
            }

            @Test
            void drainFromMixed1() {
                ItemStackHandler items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY,
                    RecipeInventoryUtil.getFilledTankStack(Tier.WOOD, FluidAmount.BUCKET_LAVA().setAmount(800)),
                    RecipeInventoryUtil.getFilledTankStack(Tier.WOOD, FluidAmount.BUCKET_WATER().setAmount(1000)),
                    RecipeInventoryUtil.getFilledTankStack(Tier.WOOD, FluidAmount.BUCKET_LAVA().setAmount(1000))
                ));
                var handler = new CATTile.FluidHandlerWrapper(items);

                assertEquals(FluidAmount.BUCKET_LAVA().setAmount(1500), FluidAmount.fromStack(
                    handler.drain(FluidAmount.BUCKET_LAVA().setAmount(1500).toStack(), IFluidHandler.FluidAction.SIMULATE)));
                assertEquals(FluidAmount.BUCKET_LAVA().setAmount(1500), FluidAmount.fromStack(
                    handler.drain(FluidAmount.BUCKET_LAVA().setAmount(1500).toStack(), IFluidHandler.FluidAction.EXECUTE)));

                assertAll(
                    () -> assertTrue(RecipeInventoryUtil.getFluidHandler(items.getStackInSlot(0)).getFluid().isEmpty()),
                    () -> assertEquals(FluidAmount.BUCKET_WATER(), RecipeInventoryUtil.getFluidHandler(items.getStackInSlot(1)).getFluid()),
                    () -> assertEquals(FluidAmount.BUCKET_LAVA().setAmount(300), RecipeInventoryUtil.getFluidHandler(items.getStackInSlot(2)).getFluid())
                );
            }

            @Test
            void drainFromMixed2() {
                ItemStackHandler items = new ItemStackHandler(NonNullList.of(ItemStack.EMPTY,
                    RecipeInventoryUtil.getFilledTankStack(Tier.WOOD, FluidAmount.BUCKET_LAVA().setAmount(800)),
                    RecipeInventoryUtil.getFilledTankStack(Tier.WOOD, FluidAmount.BUCKET_WATER().setAmount(1000)),
                    RecipeInventoryUtil.getFilledTankStack(Tier.WOOD, FluidAmount.BUCKET_LAVA().setAmount(1000))
                ));
                var handler = new CATTile.FluidHandlerWrapper(items);

                assertEquals(FluidAmount.BUCKET_WATER().setAmount(1000), FluidAmount.fromStack(
                    handler.drain(FluidAmount.BUCKET_WATER().setAmount(1500).toStack(), IFluidHandler.FluidAction.SIMULATE)));
                assertEquals(FluidAmount.BUCKET_WATER().setAmount(1000), FluidAmount.fromStack(
                    handler.drain(FluidAmount.BUCKET_WATER().setAmount(1500).toStack(), IFluidHandler.FluidAction.EXECUTE)));

                assertAll(
                    () -> assertTrue(RecipeInventoryUtil.getFluidHandler(items.getStackInSlot(1)).getFluid().isEmpty()),
                    () -> assertEquals(FluidAmount.BUCKET_LAVA().setAmount(800), RecipeInventoryUtil.getFluidHandler(items.getStackInSlot(0)).getFluid()),
                    () -> assertEquals(FluidAmount.BUCKET_LAVA().setAmount(1000), RecipeInventoryUtil.getFluidHandler(items.getStackInSlot(2)).getFluid())
                );
            }
        }
    }
}