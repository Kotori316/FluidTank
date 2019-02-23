package com.kotori316.fluidtank.integration.ae2

import appeng.capabilities.Capabilities
import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.tiles.{Connection, TileTankNoDisplay}
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.{Loader, Mod, Optional}

@Mod(name = "FluidTank_AE2", modid = "fluidtank_ae2", version = "${version}", certificateFingerprint = "@FINGERPRINT@", modLanguage = "scala",
  dependencies = "required-after:fluidtank;")
object AE2 {
  lazy val loaded = Loader.isModLoaded(TileTankNoDisplay.ae2id)
  val LOCATION = new ResourceLocation(FluidTank.modID + "_ae2", "attach_ae2")

  @Mod.EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit = {
    val metadata = event.getModMetadata
    metadata.parent = FluidTank.modID
  }

  @Mod.EventHandler
  def init(event: FMLInitializationEvent): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  @SubscribeEvent
  def attachCapability(event: AttachCapabilitiesEvent[Connection]): Unit = {
    if (loaded) {
      event.addCapability(LOCATION, new AE2Provider(event.getObject))
    }
  }

  def getIntegration(connection: Connection): Any = {
    if (loaded) {
      Class.forName("com.kotori316.fluidtank.integration.ae2.AEFluidInv").getConstructor(classOf[Connection]).newInstance(connection)
    } else {
      null
    }
  }

  class AE2Provider(connection: Connection) extends ICapabilityProvider {

    val ae2Integration = AE2.getIntegration(connection)
    if (loaded) {
      connection.updateActions.append(onContentUpdate)
    }

    override def hasCapability(capability: Capability[_], facing: EnumFacing) = {
      if (loaded) {
        isMonitorableAccessor_Internal(capability)
      } else {
        false
      }
    }

    override def getCapability[T](capability: Capability[T], facing: EnumFacing) = {
      if (loaded) {
        getMonitorableAccessor_Internal(connection, capability)
      } else {
        null.asInstanceOf[T]
      }
    }

    @Optional.Method(modid = TileTankNoDisplay.ae2id)
    private def isMonitorableAccessor_Internal(capability: Capability[_]): Boolean = {
      capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR
    }

    @Optional.Method(modid = TileTankNoDisplay.ae2id)
    private def getMonitorableAccessor_Internal[T](connection: Connection, capability: Capability[T]): T = {
      if (capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR) {
        Capabilities.STORAGE_MONITORABLE_ACCESSOR.cast(ae2Integration.asInstanceOf[AEFluidInv])
      } else {
        null.asInstanceOf[T]
      }
    }

    def onContentUpdate(): Unit = {
      if (loaded) {
        ae2Integration.asInstanceOf[AEFluidInv].postChanges()
      }
    }
  }

}
