package com.kotori316.fluidtank.integration.top;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class TankTOPPlugin {
    public static void register() {
        if (Loader.isModLoaded("theoneprobe")) {
            String packageName = TankTOPPlugin.class.getPackage().getName();
            assert (packageName + ".TankTOPFunction").equals(TankTOPFunction.class.getName());
            FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe",
                packageName + ".TankTOPFunction");
        }
    }
}
