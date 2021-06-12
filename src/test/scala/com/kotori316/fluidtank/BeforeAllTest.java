package com.kotori316.fluidtank;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;

public class BeforeAllTest {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @BeforeAll
    static void beforeAll() {
        BeforeAllTest.setup();
    }

    public static synchronized void setup() {
        if (!INITIALIZED.getAndSet(true)) {
            SharedConstants.createGameVersion();
            initLoader();
            changeDist();
            Bootstrap.initialize();
        }
    }

    private static void changeDist() {

    }

    private static void initLoader() {

    }
}
