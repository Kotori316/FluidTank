package com.kotori316.fluidtank;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import org.junit.jupiter.api.BeforeAll;

import com.kotori316.testutil.MCTestInitializer;

public abstract class BeforeAllTest {
    @BeforeAll
    static void beforeAll() {
        MCTestInitializer.setUp(FluidTank.modID, BeforeAllTest::setup);
    }

    public static synchronized void setup() {
        setConfig();
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

}
