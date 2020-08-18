package com.kotori316.fluidtank

import net.minecraftforge.common.ForgeConfigSpec

object Config {
  val CATEGORY_RECIPE = "recipe"
  private var mContent: Content = _

  def content: Content = mContent

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

  class Content(builder: ForgeConfigSpec.Builder) {
    builder.comment("Settings for FluidTank.").push("common")
    val removeRecipe: ForgeConfigSpec.BooleanValue = builder.worldRestart().comment("Remove all recipe to make tanks.")
      .define("RemoveRecipe", false)

    builder.comment("Recipe settings").push(CATEGORY_RECIPE)

    val easyRecipe: ForgeConfigSpec.BooleanValue = builder.comment("True to use easy recipe.").define("easyRecipe", false)

    builder.pop()

    val showInvisibleTank: ForgeConfigSpec.BooleanValue = builder.worldRestart().comment("True to show invisible tank in creative tabs. Recipe and block aren't removed.")
      .define("showInvisibleTankInTab", false)

    val showTOP: ForgeConfigSpec.BooleanValue = builder.comment("Show tank info on TOP tooltip.").define("showTOP", true)

    val enableWailaAndTOP: ForgeConfigSpec.BooleanValue = builder.comment("True to enable waila and top to show tank info.").define("showToolTipOnMods", true)

    val enableFluidSupplier: ForgeConfigSpec.BooleanValue = builder.comment("True to allow fluid supplier to work.").define("enableFluidSupplier", false)

    val enablePipeRainbowRenderer: ForgeConfigSpec.BooleanValue = builder.worldRestart().comment("False to disable rainbow renderer for pipe.").define("enablePipeRainbowRenderer", false)
    val pipeColor: ForgeConfigSpec.ConfigValue[Integer] = builder.worldRestart().comment("Default color of pipe. Works only if \'enablePipeRainbowRenderer\' is false.").define("pipeColor", Int.box(0xF0000000 + 0xFFFFFF))
    builder.pop()

  }

}
