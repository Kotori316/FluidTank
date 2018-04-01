package com.kotori316.fluidtank

import java.io.File

import com.kotori316.fluidtank.tiles.Tiers
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.{ConfigElement, Configuration}
import net.minecraftforge.fml.client.IModGuiFactory
import net.minecraftforge.fml.client.config.IConfigElement
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object Config {
    val CATEGORY_RECIPE = "recipe"
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

    def getElements: java.util.List[IConfigElement] = {
        new ConfigElement(configuration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements
    }

    class Content {
        private val removeRecipeProperty = configuration.get(Configuration.CATEGORY_GENERAL, "RemoveRecipe", false)
        removeRecipeProperty.setRequiresMcRestart(true)
        val removeRecipe = removeRecipeProperty.getBoolean

        val oreNameMap: Map[Tiers, String] = Tiers.list.drop(2).map(tier => {
            val property = configuration.get(CATEGORY_RECIPE, tier + "OreName", tier.oreName)
            property.setRequiresMcRestart(true)
            (tier, property.getString)
        }).toMap + ((Tiers.Invalid, "Unknown")) + ((Tiers.WOOD, "logWood"))

        if (configuration.hasChanged)
            configuration.save()
    }

}

@SideOnly(Side.CLIENT)
class GuiConfig(parent: GuiScreen) extends net.minecraftforge.fml.client.config.GuiConfig(
    parent, Config.getElements, FluidTank.modID, false, false, "Config"
)

@SideOnly(Side.CLIENT)
class GuiFactory extends IModGuiFactory {
    override def createConfigGui(parentScreen: GuiScreen): GuiConfig = new GuiConfig(parentScreen)

    override def hasConfigGui: Boolean = true

    override def runtimeGuiCategories() = null

    override def initialize(minecraftInstance: Minecraft) = ()
}
