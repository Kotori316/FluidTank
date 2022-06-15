package com.kotori316.fluidtank;

import java.lang.reflect.Field;
import java.util.Optional;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.unsafe.UnsafeHacks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.junit.jupiter.api.BeforeAll;
import sun.misc.Unsafe;

import com.kotori316.testutil.MCTestInitializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class BeforeAllTest {
    @BeforeAll
    static void beforeAll() {
        MCTestInitializer.setUp(FluidTank.modID, BeforeAllTest::setup);
    }

    public static synchronized void setup() {
        setConfig();
        mockCapability();
        mockRegistries();
        setFluidType();
    }

    @SuppressWarnings("unchecked")
    private static void mockCapability() {
        try {
            var method = CapabilityManager.class.getDeclaredMethod("get", String.class, boolean.class);
            method.setAccessible(true);
            var cap_IFluidHandler = (Capability<IFluidHandler>) method.invoke(CapabilityManager.INSTANCE, "IFluidHandler", false);
            var cap_IFluidHandlerItem = (Capability<IFluidHandlerItem>) method.invoke(CapabilityManager.INSTANCE, "IFluidHandlerItem", false);
            var cap_IItemHandler = (Capability<IItemHandler>) method.invoke(CapabilityManager.INSTANCE, "IItemHandler", false);
            try (var mocked = mockStatic(CapabilityManager.class)) {
                mocked.when(() -> CapabilityManager.get(any())).thenReturn(cap_IFluidHandler).thenReturn(cap_IFluidHandlerItem).thenReturn(cap_IItemHandler);
                assertEquals(cap_IFluidHandler, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                assertEquals(cap_IFluidHandlerItem, CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
                assertEquals(cap_IItemHandler, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            }
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        assertNotNull(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
        assertNotNull(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
        assertNotNull(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    private static void mockRegistries() {
        try {
            mockRegistry(ForgeRegistries.ITEMS, ForgeRegistries.class.getDeclaredField("ITEMS"));
            mockRegistry(ForgeRegistries.BLOCKS, ForgeRegistries.class.getDeclaredField("BLOCKS"));
            mockRegistry(ForgeRegistries.FLUIDS, ForgeRegistries.class.getDeclaredField("FLUIDS"));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private static <T> void mockRegistry(IForgeRegistry<T> registry, Field field) throws ReflectiveOperationException {
        var wrapperGetter = ForgeRegistry.class.getDeclaredMethod("getWrapper");
        wrapperGetter.setAccessible(true);
        var wrapper = (Registry<T>) wrapperGetter.invoke(registry);

        var s = spy(registry);
        when(s.getDelegate((T) any())) // Return: Optional<Holder.Reference<V>>
            .thenAnswer(invocation -> {
                T arg = invocation.getArgument(0);
                return Optional.of(Holder.Reference.createIntrusive(wrapper, arg));
            });

        var theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        var unsafe = (Unsafe) theUnsafe.get(null);
        unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), s);
    }

    private static void setConfig() {
        var builder = new ForgeConfigSpec.Builder();
        Config.sync(builder);
        var config = builder.build();
        var commentedConfig = CommentedConfig.inMemory();
        config.correct(commentedConfig);
        config.acceptConfig(commentedConfig);
        Config.content().debug().set(true);
    }

    private static void setFluidType() {
        var airType = new FluidType(FluidType.Properties.create()
            .descriptionId("block.minecraft.air")
            .motionScale(1D)
            .canPushEntity(false)
            .canSwim(false)
            .canDrown(false)
            .fallDistanceModifier(1F)
            .pathType(null)
            .adjacentPathType(null)
            .density(0)
            .temperature(0)
            .viscosity(0));
        var waterType = new FluidType(FluidType.Properties.create()
            .descriptionId("block.minecraft.water")
            .fallDistanceModifier(0F)
            .canExtinguish(true)
            .canConvertToSource(true)
            .supportsBoating(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
            .canHydrate(true));
        var lavaType = new FluidType(FluidType.Properties.create()
            .descriptionId("block.minecraft.lava")
            .canSwim(false)
            .canDrown(false)
            .pathType(BlockPathTypes.LAVA)
            .adjacentPathType(null)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
            .lightLevel(15)
            .density(3000)
            .viscosity(6000)
            .temperature(1300));
        try {
            var field = Fluid.class.getDeclaredField("forgeFluidType");
            UnsafeHacks.setField(field, Fluids.EMPTY, airType);
            UnsafeHacks.setField(field, Fluids.WATER, waterType);
            UnsafeHacks.setField(field, Fluids.LAVA, lavaType);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
