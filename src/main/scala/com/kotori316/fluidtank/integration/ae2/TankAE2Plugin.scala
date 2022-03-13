package com.kotori316.fluidtank.integration.ae2

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.tiles.TileTank
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModList

object TankAE2Plugin {
  val LOCATION = new ResourceLocation(FluidTank.modID, "attach_ae2")

  def onAPIAvailable(): Unit = {
    if (ModList.get.isLoaded("ae2"))
      MinecraftForge.EVENT_BUS.register(new CapHandler)
  }

  private class CapHandler {
    //noinspection ScalaUnusedSymbol
    @SubscribeEvent
    def event(event: AttachCapabilitiesEvent[BlockEntity]): Unit = {
      event.getObject match {
        case tank: TileTank =>
          val provider = new AEConnectionCapabilityProvider(tank)
          event.addCapability(LOCATION, provider)
        case _ =>
      }
    }
  }
}
