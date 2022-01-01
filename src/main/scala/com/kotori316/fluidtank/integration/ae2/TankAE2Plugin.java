package com.kotori316.fluidtank.integration.ae2;

import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.MEStorage;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.tiles.TileTank;

public class TankAE2Plugin {
    public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "attach_ae2");

    public static void onAPIAvailable() {
        if (ModList.get().isLoaded("ae2"))
            MinecraftForge.EVENT_BUS.register(new CapHandler());
    }

    static class CapHandler {
        @SubscribeEvent
        public void event(AttachCapabilitiesEvent<BlockEntity> event) {
            if (event.getObject() instanceof TileTank tank) {
                AEConnectionCapabilityProvider provider = new AEConnectionCapabilityProvider(tank);
                event.addCapability(LOCATION, provider);
            }
        }
    }
}

class AEConnectionCapabilityProvider implements ICapabilityProvider, IStorageMonitorableAccessor {
    static Capability<IStorageMonitorableAccessor> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private final TileTank tank;
    private AEFluidInv aeFluidInv;

    public AEConnectionCapabilityProvider(TileTank tank) {
        this.tank = tank;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this).cast());
    }

    @Override
    public MEStorage getInventory(IActionSource iActionSource) {
        if (aeFluidInv == null) aeFluidInv = new AEFluidInv(tank);
        return aeFluidInv;
    }
}
