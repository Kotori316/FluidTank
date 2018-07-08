package com.kotori316.fluidtank

import java.io.File

import com.kotori316.fluidtank.tiles.Tiers
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.{ConfigElement, Configuration}
import net.minecraftforge.fml.client.IModGuiFactory
import net.minecraftforge.fml.client.config.{DummyConfigElement, IConfigElement}
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
        val elements = new ConfigElement(configuration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements
        elements.add(new DummyConfigElement.DummyCategoryElement("Recipe Setting", FluidTank.modID + "." + CATEGORY_RECIPE,
            new ConfigElement(configuration.getCategory(CATEGORY_RECIPE)).getChildElements))
        elements
    }

    class Content {
        private val removeRecipeProperty = configuration.get(Configuration.CATEGORY_GENERAL, "RemoveRecipe", false)
        removeRecipeProperty.setRequiresMcRestart(true)
        removeRecipeProperty.setComment("Remove all recipe to make tanks.")
        val removeRecipe = removeRecipeProperty.getBoolean

        private val enableOldRenderProperty = configuration.get(Configuration.CATEGORY_CLIENT, "enableOldRender", false)
        enableOldRenderProperty.setRequiresMcRestart(true)
        enableOldRenderProperty.setComment("True to use other render system for item. It doesn't show the content of tanks.")
        val enableOldRender = enableOldRenderProperty.getBoolean

        val oreNameMap: Map[Tiers, String] = Tiers.list.drop(2).map(tier => {
            val property = configuration.get(CATEGORY_RECIPE, tier + "OreName", tier.oreName)
            property.setRequiresMcRestart(true)
            property.setComment(s"Set oredict name of items to craft $tier tank.")
            (tier, property.getString)
        }).toMap + (Tiers.Invalid -> "Unknown") + (Tiers.WOOD -> "logWood")

        private val showInvisibleTankProperty = configuration.get(Configuration.CATEGORY_GENERAL, "showInvisibleTankInTab", false)
        showInvisibleTankProperty.setRequiresMcRestart(true)
        showInvisibleTankProperty.setComment("True to show invisible tank in creative tabs. Recipe and block aren't removed.")
        val showInvisibleTank = showInvisibleTankProperty.getBoolean

        assert(Tiers.list.forall(oreNameMap.contains))

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
