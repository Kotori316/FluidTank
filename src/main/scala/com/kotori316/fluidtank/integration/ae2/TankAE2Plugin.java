package com.kotori316.fluidtank.integration.ae2;
/*
import appeng.api.AEAddon;
import appeng.api.IAEAddon;
import appeng.api.IAppEngApi;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.me.helpers.BaseActionSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.tileentity.BlockEntity;
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
import com.kotori316.fluidtank.tiles.TileTank;

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
        public void event(AttachCapabilitiesEvent<BlockEntity> event) {
            if (event.getObject() instanceof TileTank) {
                TileTank tank = (TileTank) event.getObject();
                AEConnectionCapabilityProvider provider = new AEConnectionCapabilityProvider(api, tank);
                event.addCapability(LOCATION, provider);
            }
        }
    }
}

class AEConnectionCapabilityProvider implements ICapabilityProvider, IStorageMonitorableAccessor, IStorageMonitorable {
    @CapabilityInject(IStorageMonitorableAccessor.class)
    static Capability<IStorageMonitorableAccessor> CAPABILITY = null;
    private final IAppEngApi api;
    private final TileTank tank;
    AEFluidInv aeFluidInv;

    public AEConnectionCapabilityProvider(IAppEngApi api, TileTank tank) {
        this.api = api;
        this.tank = tank;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this).cast());
    }

    @Override
    public IStorageMonitorable getInventory(IActionSource iActionSource) {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> iStorageChannel) {
        if (iStorageChannel.equals(api.storage().getStorageChannel(IFluidStorageChannel.class))) {
            if (aeFluidInv == null) {
                aeFluidInv = new AEFluidInv(api, tank);
                tank.connectionAttaches().append(this::addUpdater);
                this.addUpdater(tank.connection());
            }
            return (IMEMonitor<T>) aeFluidInv;
        } else {
            return null;
        }
    }


    public BoxedUnit postUpdate() {
        if (aeFluidInv != null)
            aeFluidInv.postChange(aeFluidInv, aeFluidInv.getStorageList(), new BaseActionSource());
        return BoxedUnit.UNIT;
    }

    public BoxedUnit addUpdater(Connection connection) {
        connection.updateActions().append(this::postUpdate);
        this.postUpdate();
        return BoxedUnit.UNIT;
    }
}*/
