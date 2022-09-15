package com.kotori316.fluidtank;

interface DefaultTestInterface {
    default int getDefault() {
        return 100;
    }

    int nonDefault();
}
