package com.kotori316.fluidtank.integration.ae2;
/*
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
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.jdk.javaapi.FunctionConverters;

import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.TileTank;

public class AEFluidInv implements IMEMonitor<IAEFluidStack>, IMEMonitorHandlerReceiver<IAEFluidStack> {
    private final IAppEngApi api;
    private final TileTank tank;

    public AEFluidInv(IAppEngApi api, TileTank tank) {
        this.api = api;
        this.tank = tank;
    }

    /**
     * Store new items, or simulate the addition of new items into the ME Inventory.
     *
     * @param input      item to add.
     * @param actionable action type
     * @param src        action source
     * @return returns the number of items not added.
     *//*
    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable actionable, IActionSource src) {
        FluidAmount fluidAmount = fromAEStack(input);
        FluidAmount filled = tank.connection().handler().fill(fluidAmount, actionable.getFluidAction());
        return toAEStack(fluidAmount.$minus(filled));
    }

    /**
     * Extract the specified item from the ME Inventory
     *
     * @param request    item to request ( with stack size. )
     * @param actionable simulate, or perform action?
     * @return returns the number of items extracted, null
     *//*
    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable actionable, IActionSource src) {
        FluidAmount fluidAmount = fromAEStack(request);
        FluidAmount drained = tank.connection().handler().drain(fluidAmount, actionable.getFluidAction());
        return toAEStack(drained);
    }

    @Override
    @Deprecated
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> iItemList) {
        tank.connection().getFluidStack()
            .map(this::toAEStack)
            .map(s -> s.setStackSize(tank.connection().amount()))
            .foreach(FunctionConverters.asScalaFromConsumer(iItemList::add));
        return iItemList;
    }

    @Override
    public IItemList<IAEFluidStack> getStorageList() {
        IItemList<IAEFluidStack> list = getChannel().createList();
        tank.connection().getFluidStack()
            .map(this::toAEStack)
            .map(s -> s.setStackSize(tank.connection().amount()))
            .foreach(FunctionConverters.asScalaFromConsumer(list::add));
        return list;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return api.storage().getStorageChannel(IFluidStorageChannel.class);
    }

    @NotNull
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
    IAEFluidStack toAEStack(@NotNull FluidAmount amount) {
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
        return this.tank.connection() == o;
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    /**
     * determine if a particular item is prioritized for this inventory handler, if
     * it is, then it will be added to this inventory prior to any non-prioritized
     * inventories.
     *
     * @param input - item that might be added
     * @return if its prioritized
     *//*
    @Override
    public boolean isPrioritized(IAEFluidStack input) {
        return true;
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
*/