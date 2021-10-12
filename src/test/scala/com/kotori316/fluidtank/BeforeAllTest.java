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
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.targets.FMLDataUserdevLaunchHandler;
import org.junit.jupiter.api.BeforeAll;
import scala.jdk.javaapi.CollectionConverters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
}
