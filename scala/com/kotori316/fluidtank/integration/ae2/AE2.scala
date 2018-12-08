package com.kotori316.fluidtank.integration.ae2

import appeng.capabilities.Capabilities
import com.kotori316.fluidtank.tiles.{Connection, TileTankNoDisplay}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fml.common.{Loader, Optional}

object AE2 {
  lazy val loaded = Loader.isModLoaded(TileTankNoDisplay.ae2id)

  def getMonitorableAccessor[T](connection: Connection, capability: Capability[T]): T = {
    if (loaded) {
      getMonitorableAccessor_Internal(connection, capability)
    } else {
      null.asInstanceOf[T]
    }
  }

  def isMonitorableAccessor(capability: Capability[_]): Boolean = {
    if (loaded) {
      isMonitorableAccessor_Internal(capability)
    } else {
      false
    }
  }

  def onContentUpdate(connection: Connection): Unit = {
    if (loaded) {
      connection.ae2Integration.asInstanceOf[AEFluidInv].postChanges()
    }
  }

  def getIntegration(connection: Connection): Any = {
    if (loaded) {
      Class.forName("com.kotori316.fluidtank.integration.ae2.AEFluidInv").getConstructor(classOf[Connection]).newInstance(connection)
    } else {
      null
    }
  }

  @Optional.Method(modid = TileTankNoDisplay.ae2id)
  private def isMonitorableAccessor_Internal(capability: Capability[_]): Boolean = {
    capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR
  }

  @Optional.Method(modid = TileTankNoDisplay.ae2id)
  private def getMonitorableAccessor_Internal[T](connection: Connection, capability: Capability[T]): T = {
    if (capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR) {
      Capabilities.STORAGE_MONITORABLE_ACCESSOR.cast(connection.ae2Integration.asInstanceOf[AEFluidInv])
    } else {
      null.asInstanceOf[T]
    }
  }
}
