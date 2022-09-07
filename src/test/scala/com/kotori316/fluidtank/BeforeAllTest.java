package com.kotori316.fluidtank;

import java.io.InputStream;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.server.LanguageHook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.support.ReflectionSupport;

import com.kotori316.testutil.MCTestInitializer;

public abstract class BeforeAllTest {
    @BeforeAll
    static void beforeAll() {
        MCTestInitializer.setUp(FluidTank.modID, BeforeAllTest::setup);
    }

    public static synchronized void setup() {
        setConfig();
        setLanguage();
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

    private static void setLanguage() {
        LanguageHook.loadForgeAndMCLangs();
        Try.call(() -> LanguageHook.class.getDeclaredMethod("loadLocaleData", InputStream.class))
            .andThenTry(m -> ReflectionSupport.invokeMethod(m, null, BeforeAllTest.class.getResourceAsStream("/assets/fluidtank/lang/en_us.json")));
    }
}
