package com.kotori316.fluidtank.integration.ae2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import appeng.api.IAppEngApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import scala.Option;
import scala.jdk.javaapi.FunctionConverters;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.tiles.Connection;

public class AEFluidInv implements IStorageMonitorableAccessor, IMEMonitor<IAEFluidStack>, IMEMonitorHandlerReceiver<IAEFluidStack> {
    private final IAppEngApi api;
    private final Connection connection;

    public AEFluidInv(IAppEngApi api, Connection connection) {
        this.api = api;
        this.connection = connection;
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack stack, Actionable actionable, IActionSource iActionSource) {
        FluidAmount fluidAmount = fromAEStack(stack);
        FluidAmount filled = connection.handler().fill(fluidAmount, actionable == Actionable.MODULATE, 0);
        return toAEStack(fluidAmount.$minus(filled));
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack stack, Actionable actionable, IActionSource iActionSource) {
        return toAEStack(
            connection.handler().drain(fromAEStack(stack), actionable == Actionable.MODULATE, 0)
        );
    }

    @Override
    @Deprecated
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> iItemList) {
        connection.getFluidStack()
            .map(this::toAEStack)
            .map(s -> s.setStackSize(connection.amount()))
            .foreach(FunctionConverters.asScalaFromConsumer(iItemList::add));
        return iItemList;
    }

    @Override
    public IItemList<IAEFluidStack> getStorageList() {
        IItemList<IAEFluidStack> list = getChannel().createList();
        connection.getFluidStack()
            .map(this::toAEStack)
            .map(s -> s.setStackSize(connection.amount()))
            .foreach(FunctionConverters.asScalaFromConsumer(list::add));
        return list;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return api.storage().getStorageChannel(IFluidStorageChannel.class);
    }

    @Override
    public IStorageMonitorable getInventory(IActionSource iActionSource) {
        return new IStorageMonitorable() {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> iStorageChannel) {
                if (iStorageChannel == getChannel())
                    return ((IMEMonitor<T>) AEFluidInv.this);
                else
                    return null;
            }
        };
    }

    @Nonnull
    static FluidAmount fromAEStack(@Nullable IAEFluidStack stack) {
        if (stack == null) {
            return FluidAmount.EMPTY();
        } else {
            return new FluidAmount(
                stack.getFluid(),
                stack.getStackSize(),
                Option.apply(stack.getFluidStack().getTag())
            );
        }
    }

    @Nullable
    IAEFluidStack toAEStack(@Nonnull FluidAmount amount) {
        // Null when stack is empty.
        IAEFluidStack stack = getChannel().createStack(amount.toStack());
        if (stack != null) {
            stack.setStackSize(amount.amount());
        }
        return stack;
    }

    private final Map<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> listeners = new HashMap<>();

    @Override
    public void addListener(IMEMonitorHandlerReceiver<IAEFluidStack> receiver, Object o) {
        listeners.put(receiver, o);
    }

    @Override
    public void removeListener(IMEMonitorHandlerReceiver<IAEFluidStack> receiver) {
        listeners.remove(receiver);
    }

    @Override
    public void postChange(IBaseMonitor<IAEFluidStack> iBaseMonitor, Iterable<IAEFluidStack> iterable, IActionSource iActionSource) {
        Iterator<Map.Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object>> iterator = listeners.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> entry = iterator.next();
            if (entry.getKey().isValid(entry.getValue())) {
                entry.getKey().postChange(this, iterable, iActionSource);
            } else {
                iterator.remove();
            }
        }
    }

    @Override
    public void onListUpdate() {
        Iterator<Map.Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object>> iterator = listeners.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> entry = iterator.next();
            if (entry.getKey().isValid(entry.getValue())) {
                entry.getKey().onListUpdate();
            } else {
                iterator.remove();
            }
        }
    }

    @Override
    public boolean isValid(Object o) {
        return this.connection == o;
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(IAEFluidStack stack) {
        return false;
    }

    @Override
    public boolean canAccept(IAEFluidStack stack) {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return true;
    }

}
