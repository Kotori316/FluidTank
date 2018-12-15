package com.kotori316.fluidtank.integration.hwyla

import com.kotori316.fluidtank.tiles.TileTankNoDisplay
import mcp.mobius.waila.api.{IWailaPlugin, IWailaRegistrar, WailaPlugin}

@WailaPlugin
object Waila extends IWailaPlugin {
  override def register(registrar: IWailaRegistrar): Unit = {
    val provider = new TankDataProvider
    registrar.registerBodyProvider(provider, classOf[TileTankNoDisplay])
    registrar.registerNBTProvider(provider, classOf[TileTankNoDisplay])
  }
}
