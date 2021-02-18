package com.kotori316.fluidtank

import java.util.function.Supplier

import net.minecraftforge.common.ForgeConfigSpec

import scala.jdk.javaapi.CollectionConverters

object Config {
  final val CATEGORY_RECIPE = "recipe"
  final val defaultConfig: Map[String, AnyVal] = Map(
    "removeRecipe" -> false,
    "debug" -> false,
    "easyRecipe" -> false,
    "usableInvisibleInRecipe" -> true,
    "usableUnavailableTankInRecipe" -> true,
    "showInvisibleTank" -> true,
    "showTOP" -> true,
    "enableWailaAndTOP" -> true,
    "enableFluidSupplier" -> false,
    "enablePipeRainbowRenderer" -> false,
    "pipeColor" -> (0xF0000000 + 0xFFFFFF)
  )
  private var mContent: IContent = _
  var dummyContent: IContent = new Utils.TestConfig(CollectionConverters.asJava(defaultConfig))

  def content: IContent = if (mContent == null) dummyContent else mContent

  def sync(): ForgeConfigSpec = {
    val builder = new ForgeConfigSpec.Builder
    mContent = new Content(builder)
    val spec = builder.build()
    spec
  }

  /*def onChanged(event: OnConfigChangedEvent): Unit = {
    if (event.getModID == FluidTank.modID) {
      sync()
    }
  }*/
  trait BoolSupplier extends java.util.function.BooleanSupplier {
    def get(): Boolean

    override def getAsBoolean: Boolean = get()
  }

  trait IContent {
    val removeRecipe: BoolSupplier
    val debug: BoolSupplier
    val easyRecipe: BoolSupplier
    val usableInvisibleInRecipe: BoolSupplier
    val usableUnavailableTankInRecipe: BoolSupplier
    val showInvisibleTank: BoolSupplier
    val showTOP: BoolSupplier
    val enableWailaAndTOP: BoolSupplier
    val enableFluidSupplier: BoolSupplier
    val enablePipeRainbowRenderer: BoolSupplier
    val pipeColor: Supplier[Integer]
  }

  class Content(builder: ForgeConfigSpec.Builder) extends IContent {
    private def asSupplier(b: ForgeConfigSpec.BooleanValue): BoolSupplier = () => b.get()

    private def asSupplier[T](b: ForgeConfigSpec.ConfigValue[T]): java.util.function.Supplier[T] = () => b.get()

    builder.comment("Settings for FluidTank.").push("common")
    val removeRecipe: BoolSupplier = asSupplier(builder.worldRestart().comment("Remove all recipe to make tanks.")
      .define("RemoveRecipe", false))
    val debug: BoolSupplier = asSupplier(builder.comment("Debug Mode").define("debug", false))

    builder.comment("Recipe settings").push(CATEGORY_RECIPE)

    val easyRecipe: BoolSupplier = asSupplier(builder.worldRestart().comment("True to use easy recipe.").define("easyRecipe", false))
    val usableInvisibleInRecipe: BoolSupplier = asSupplier(builder.worldRestart().comment("False to prohibit invisible tanks to be used in recipes")
      .define("usableInvisibleInRecipe", true))
    val usableUnavailableTankInRecipe: BoolSupplier = asSupplier(builder.worldRestart().comment("False to prohibit tanks with no recipe to be used in recipes of other tanks")
      .define("usableUnavailableTankInRecipe", true))

    builder.pop()

    val showInvisibleTank: BoolSupplier = asSupplier(builder.worldRestart().comment("True to show invisible tank in creative tabs. Recipe and block aren't removed.")
      .define("showInvisibleTankInTab", false))

    val showTOP: BoolSupplier = asSupplier(builder.comment("Show tank info on TOP tooltip.").define("showTOP", true))

    val enableWailaAndTOP: BoolSupplier = asSupplier(builder.comment("True to enable waila and top to show tank info.").define("showToolTipOnMods", true))

    val enableFluidSupplier: BoolSupplier = asSupplier(builder.comment("True to allow fluid supplier to work.").define("enableFluidSupplier", false))

    val enablePipeRainbowRenderer: BoolSupplier = asSupplier(builder.worldRestart()
      .comment("False to disable rainbow renderer for pipe.").define("enablePipeRainbowRenderer", false))
    val pipeColor: java.util.function.Supplier[Integer] = asSupplier(builder.worldRestart()
      .comment("Default color of pipe. Works only if \'enablePipeRainbowRenderer\' is false.").define("pipeColor", Int.box(0xF0000000 + 0xFFFFFF)))
    builder.pop()

  }

}
