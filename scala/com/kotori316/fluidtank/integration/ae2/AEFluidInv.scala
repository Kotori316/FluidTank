package com.kotori316.fluidtank.integration.ae2

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.storage.channels.IFluidStorageChannel
import appeng.api.storage.data.{IAEFluidStack, IAEStack, IItemList}
import appeng.api.storage.{IMEInventory, IMEMonitor, IStorageChannel, IStorageMonitorable, IStorageMonitorableAccessor}
import appeng.me.helpers.BaseActionSource
import appeng.me.storage.{MEMonitorPassThrough, NullInventory}
import com.kotori316.fluidtank.tiles.Connection

/**
  * DON'T CALL IF AE2 ISN'T PRESENTED.
  * A helper class for AE2 integration. Connection of tanks behaves as a ME fluid inventory.
  *
  * @param self Connection
  */
class AEFluidInv(self: Connection) extends IMEInventory[IAEFluidStack] with IStorageMonitorableAccessor {

    val mePassThrough = new MEMonitorPassThrough[IAEFluidStack](new NullInventory[IAEFluidStack], getChannel)

    val monitorable = new IStorageMonitorable {
        override def getInventory[T <: IAEStack[T]](channel: IStorageChannel[T]): IMEMonitor[T] = {
            if (channel == getChannel) {
                mePassThrough.setInternal(AEFluidInv.this)
                mePassThrough.asInstanceOf[IMEMonitor[T]]
            } else {
                null
            }
        }
    }

    override def getChannel: IFluidStorageChannel =
        AEApi.instance().storage().getStorageChannel[IAEFluidStack, IFluidStorageChannel](classOf[IFluidStorageChannel])

    override def injectItems(input: IAEFluidStack, mode: Actionable, src: IActionSource): IAEFluidStack = {
        val fluidStack = input.getFluidStack
        val filled = self.handler.fill(fluidStack, mode == Actionable.MODULATE)
        if (filled == 0) {
            // Input isn't modified. Tank is full.
            input
        } else {
            val remaing = fluidStack.amount - filled
            fluidStack.amount = remaing
            getChannel.createStack(fluidStack)
        }
    }

    override def extractItems(request: IAEFluidStack, mode: Actionable, src: IActionSource): IAEFluidStack = {
        val fluidStack = request.getFluidStack
        Option(self.handler.drain(fluidStack, mode == Actionable.MODULATE)).map(getChannel.createStack).orNull
    }

    override def getAvailableItems(out: IItemList[IAEFluidStack]): IItemList[IAEFluidStack] = {
        self.getFluidStack.map(getChannel.createStack).map(_.setStackSize(self.amount)).foreach(out.add)
        out
    }

    override def getInventory(src: IActionSource): IStorageMonitorable = monitorable

    def postChanges(): Unit = {
        mePassThrough.postChange(mePassThrough, mePassThrough.getStorageList, new BaseActionSource())
    }
}
