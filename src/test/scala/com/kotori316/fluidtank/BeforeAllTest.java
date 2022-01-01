package com.kotori316.fluidtank;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import cpw.mods.modlauncher.Launcher;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.targets.FMLDataUserdevLaunchHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.junit.jupiter.api.BeforeAll;
import scala.jdk.javaapi.CollectionConverters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

public abstract class BeforeAllTest {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @BeforeAll
    static void beforeAll() {
        BeforeAllTest.setup();
    }

    public static synchronized void setup() {
        if (!INITIALIZED.getAndSet(true)) {
            SharedConstants.tryDetectVersion();
            // initLoader();
            changeDist();
            setHandler();
            assertEquals(Dist.CLIENT, FMLEnvironment.dist);
            Bootstrap.bootStrap();
            Map<String, Object> map = new HashMap<>(CollectionConverters.asJava(Config.defaultConfig()));
            map.put("debug", true);
            Config.dummyContent_$eq(Utils.TestConfig.getTestInstance(map));
            mockCapability();
        }
    }

    private static void changeDist() {
        try {
            Field dist = FMLLoader.class.getDeclaredField("dist");
            dist.setAccessible(true);
            dist.set(null, Dist.CLIENT);
        } catch (Exception e) {
            fail(e);
        }
    }

    private static void setHandler() {
        try {
            Field handler = FMLLoader.class.getDeclaredField("commonLaunchHandler");
            handler.setAccessible(true);
            handler.set(null, new FMLDataUserdevLaunchHandler());
        } catch (Exception e) {
            fail(e);
        }
    }

    private static void initLoader() {
        try {
            Constructor<Launcher> constructor = Launcher.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (Exception e) {
            fail(e);
        }
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
}
