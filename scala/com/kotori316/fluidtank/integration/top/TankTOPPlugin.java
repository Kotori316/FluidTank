package com.kotori316.fluidtank.integration.top;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.kotori316.fluidtank.FluidTank;

@Mod(name = FluidTank.MOD_NAME + "_TheOneProbe", modid = FluidTank.modID + "_theoneprobe", version = "${version}", certificateFingerprint = "@FINGERPRINT@",
    dependencies = "required-after:fluidtank;")
public class TankTOPPlugin {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModMetadata metadata = event.getModMetadata();
        metadata.parent = FluidTank.modID;
        if (Loader.isModLoaded("theoneprobe")) {
            String packageName = TankTOPPlugin.class.getPackage().getName();
            assert (packageName + ".TankTOPFunction").equals(TankTOPFunction.class.getName());
            FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe",
                packageName + ".TankTOPFunction");
        }
    }
}
