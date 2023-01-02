package com.kotori316.fluidtank;

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
        FluidTank.registerConfig(true);
        Config.content().debug().set(true);
    }

}
