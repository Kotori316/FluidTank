package com.kotori316.fluidtank.integration.ae2;

import appeng.api.IAEAddonEntrypoint;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.MEStorage;
import net.fabricmc.loader.api.FabricLoader;

import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.TankConstant;
import com.kotori316.fluidtank.tank.TileTank;

public final class TankAE2Plugin implements IAEAddonEntrypoint {
    // public static final ResourceLocation LOCATION = new ResourceLocation(ModTank.modID, "attach_ae2");

    @Override
    public void onAe2Initialized() {
        if (FabricLoader.getInstance().isModLoaded("ae2") && TankConstant.config.enableAE2Integration) {
            CapHandler.event();
        }
    }

    static class CapHandler {
        public static void event() {
            IStorageMonitorableAccessor.SIDED.registerForBlockEntities((blockEntity, context) -> {
                if (blockEntity instanceof TileTank tank) return new AEConnectionCapabilityProvider(tank);
                else return null;
            }, ModTank.Entries.TANK_BLOCK_ENTITY_TYPE, ModTank.Entries.VOID_BLOCK_ENTITY_TYPE, ModTank.Entries.CREATIVE_BLOCK_ENTITY_TYPE);
        }
    }

    static class AEConnectionCapabilityProvider implements IStorageMonitorableAccessor {
        private final TileTank tank;
        private AEFluidInv aeFluidInv;

        public AEConnectionCapabilityProvider(TileTank tank) {
            this.tank = tank;
        }

        @Override
        public MEStorage getInventory(IActionSource iActionSource) {
            if (aeFluidInv == null) aeFluidInv = new AEFluidInv(tank);
            return aeFluidInv;
        }
    }
}
