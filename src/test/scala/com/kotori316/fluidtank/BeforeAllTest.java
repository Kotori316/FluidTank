package com.kotori316.fluidtank;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;

public class BeforeAllTest {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @BeforeAll
    static void beforeAll() {
        BeforeAllTest.setup();
    }

    public static synchronized void setup() {
        if (!INITIALIZED.getAndSet(true)) {
            SharedConstants.tryDetectVersion();
            initLoader();
            changeDist();
            Bootstrap.bootStrap();
            FluidTank.config = new TankConfig();
        }
    }

    private static void changeDist() {

    }

    private static void initLoader() {

    }
}
