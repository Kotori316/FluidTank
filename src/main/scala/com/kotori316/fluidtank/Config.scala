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
    val removeRecipe = builder.worldRestart().comment("Remove all recipe to make tanks.")
      .define("RemoveRecipe", false)

    builder.comment("Recipe settings").push(CATEGORY_RECIPE)

    val easyRecipe = builder.comment("True to use easy recipe.").define("easyRecipe", false)

    builder.pop()

    val showInvisibleTank = builder.worldRestart().comment("True to show invisible tank in creative tabs. Recipe and block aren't removed.")
      .define("showInvisibleTankInTab", false)

    val showTOP = builder.comment("Show tank info on TOP tooltip.").define("showTOP", true)

    val enableWailaAndTOP = builder.comment("True to enable waila and top to show tank info.").define("showToolTipOnMods", true)

    val enableFluidSupplier = builder.comment("True to allow fluid supplier to work.").define("enableFluidSupplier", true)

    val enablePipeRainbowRenderer = builder.worldRestart().comment("False to disable rainbow renderer for pipe.").define("enablePipeRainbowRenderer", true)
    val pipeColor = builder.worldRestart().comment("Color of pipe. Works only if \'enablePipeRainbowRenderer\' is false.").define("pipeColor", Int.box(0xFFFFFFFF))
    builder.pop()

  }

}
