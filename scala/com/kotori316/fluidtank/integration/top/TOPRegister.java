package com.kotori316.fluidtank.integration.top;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class TOPRegister {
    public static void register() {
        if (Loader.isModLoaded("theoneprobe")) {
            String packageName = TOPRegister.class.getPackage().getName();
            assert (packageName + ".TankTOPPlugin").equals(TankTOPPlugin.class.getName());
            FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe",
                packageName + ".TankTOPPlugin");
        }
    }
}
