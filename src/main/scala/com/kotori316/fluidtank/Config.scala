package com.kotori316.fluidtank

import net.minecraftforge.common.ForgeConfigSpec

object Config {
  final val CATEGORY_RECIPE = "recipe"
  private var mContent: Content = _

  def content: Content = mContent

  def sync(builder: ForgeConfigSpec.Builder): ForgeConfigSpec = {
    mContent = new Content(builder)
    val spec = builder.build()
    spec
  }

  /*def onChanged(event: OnConfigChangedEvent): Unit = {
    if (event.getModID == FluidTank.modID) {
      sync()
    }
  }*/

  class Content(builder: ForgeConfigSpec.Builder) {
    builder.comment("Settings for FluidTank.").push("common")
    val removeRecipe: ForgeConfigSpec.BooleanValue = builder.worldRestart().comment("Remove all recipe to make tanks.")
      .define("RemoveRecipe", false)
    val debug: ForgeConfigSpec.BooleanValue = builder.comment("Debug Mode").define("debug", false)

    builder.comment("Recipe settings").push(CATEGORY_RECIPE)

    val easyRecipe: ForgeConfigSpec.BooleanValue = builder.worldRestart().comment("True to use easy recipe.")
      .define("easyRecipe", false)
    val usableUnavailableTankInRecipe: ForgeConfigSpec.BooleanValue = builder.worldRestart()
      .comment("False to prohibit tanks with no recipe from being used in recipes of other tanks.")
      .define("usableUnavailableTankInRecipe", true)

    builder.pop()

    val showInvisibleTank: ForgeConfigSpec.BooleanValue = builder.worldRestart()
      .comment("True to show invisible tank in creative tabs. Recipe and block aren't removed.")
      .define("showInvisibleTankInTab", false)

    val showTOP: ForgeConfigSpec.BooleanValue = builder.comment("Show tank info on TOP tooltip.")
      .define("showTOP", true)
    val topShort: ForgeConfigSpec.BooleanValue = builder.comment("Use short format for TOP tooltip")
      .define("topShort", false)
    val topCompact: ForgeConfigSpec.BooleanValue = builder.comment("Use compact number format for TOP tooltip")
      .define("topCompact", false)

    val enableWailaAndTOP: ForgeConfigSpec.BooleanValue = builder.comment("True to enable waila and top to show tank info.")
      .define("showToolTipOnMods", true)

    val enableFluidSupplier: ForgeConfigSpec.BooleanValue = builder.comment("True to allow fluid supplier to work.")
      .define("enableFluidSupplier", false)

    val enablePipeRainbowRenderer: ForgeConfigSpec.BooleanValue = builder.worldRestart()
      .comment("False to disable rainbow renderer for pipe.").define("enablePipeRainbowRenderer", false)
    val pipeColor: ForgeConfigSpec.ConfigValue[Integer] = builder.worldRestart()
      .comment("Default color of pipe. Works only if \'enablePipeRainbowRenderer\' is false.")
      .define("pipeColor", Int.box(0xF0000000 + 0xFFFFFF))
    val renderLowerBound: ForgeConfigSpec.DoubleValue = builder.worldRestart()
      .comment("The lower bound of position of fluid rendering. Default 0.001")
      .defineInRange("renderLowerBound", 0.001d, 0d, 1d)
    val renderUpperBound: ForgeConfigSpec.DoubleValue = builder.worldRestart()
      .comment("The upper bound of position of fluid rendering. Default 0.999")
      .defineInRange("renderUpperBound", 1d - 0.001d, 0d, 1d)
    builder.pop()

  }

}
