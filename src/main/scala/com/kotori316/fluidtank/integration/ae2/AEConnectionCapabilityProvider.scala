package com.kotori316.fluidtank.integration.ae2

import appeng.api.networking.security.IActionSource
import appeng.api.storage.{IStorageMonitorableAccessor, MEStorage}
import cats.Eval
import com.kotori316.fluidtank.tiles.TileTank
import net.minecraft.core.Direction
import net.minecraftforge.common.capabilities.{Capability, CapabilityManager, CapabilityToken, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional

class AEConnectionCapabilityProvider(tank: TileTank)
  extends ICapabilityProvider
    with IStorageMonitorableAccessor {
  private val aeFluidInv = Eval.later(AEFluidInv(tank))

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    AEConnectionCapabilityProvider.CAPABILITY.orEmpty(cap, LazyOptional.of[IStorageMonitorableAccessor](() => this))
  }

  override def getInventory(iActionSource: IActionSource): MEStorage = {
    aeFluidInv.value
  }
}

object AEConnectionCapabilityProvider {
  private val CAPABILITY: Capability[IStorageMonitorableAccessor] =
    CapabilityManager.get[IStorageMonitorableAccessor](new CapabilityToken[IStorageMonitorableAccessor]() {})
}
