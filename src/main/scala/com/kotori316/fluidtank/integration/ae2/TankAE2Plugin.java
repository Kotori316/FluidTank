package com.kotori316.fluidtank.integration.ae2;

import appeng.api.AEAddon;
import appeng.api.IAEAddon;
import appeng.api.IAppEngApi;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.me.helpers.BaseActionSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import scala.runtime.BoxedUnit;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.tiles.Connection;

@AEAddon
public class TankAE2Plugin implements IAEAddon {
    public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "attach_ae2");

    @Override
    public void onAPIAvailable(IAppEngApi iAppEngApi) {
        MinecraftForge.EVENT_BUS.register(new CapHandler(iAppEngApi));
    }

    static class CapHandler {
        private final IAppEngApi api;

        CapHandler(IAppEngApi api) {
            this.api = api;
        }

        @SubscribeEvent
        public void event(AttachCapabilitiesEvent<Connection> event) {
            event.addCapability(LOCATION, new AEConnectionCapabilityProvider(api, event.getObject()));
        }
    }
}

class AEConnectionCapabilityProvider implements ICapabilityProvider {
    @CapabilityInject(IStorageMonitorableAccessor.class)
    static Capability<IStorageMonitorableAccessor> CAPABILITY = null;
    private final AEFluidInv aeFluidInv;

    public AEConnectionCapabilityProvider(IAppEngApi iAppEngApi, Connection connection) {
        aeFluidInv = new AEFluidInv(iAppEngApi, connection);
        connection.updateActions().append(this::postUpdate);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, LazyOptional.of(() -> aeFluidInv).cast());
    }

    public BoxedUnit postUpdate() {
        aeFluidInv.postChange(aeFluidInv, aeFluidInv.getStorageList(), new BaseActionSource());
        return BoxedUnit.UNIT;
    }
}
