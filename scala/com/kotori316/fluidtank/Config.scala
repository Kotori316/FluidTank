package com.kotori316.fluidtank

import java.io.File

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent

object Config {
    private var configuration: Configuration = _
    private var mConetent: Content = _

    def load(file: File): Unit = {
        configuration = new Configuration(file)
        MinecraftForge.EVENT_BUS.register(this)
        sync()
    }

    def content = mConetent

    def sync(): Unit = {
        mConetent = new Content
    }

    def onChanged(event: OnConfigChangedEvent): Unit = {
        if (event.getModID == FluidTank.modID) {
            sync()
        }
    }

    class Content {
        // TODO: not changeable
        val removeRecipe = configuration.getBoolean("RemoveRecipe", Configuration.CATEGORY_GENERAL, false, "")
        if (configuration.hasChanged)
            configuration.save()
    }

}
