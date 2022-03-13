package com.kotori316.fluidtank

import java.lang.{Double => JDouble}
import java.util.function.Supplier

import net.minecraftforge.common.ForgeConfigSpec

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
    "pipeColor" -> (0xF0000000 + 0xFFFFFF),
    "renderLowerBound" -> 0.001d,
    "renderUpperBound" -> (1 - 0.001d),
  )
  private var mContent: IContent = _
  var dummyContent: IContent = Utils.getTestInstance(defaultConfig)

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


  class Content(builder: ForgeConfigSpec.Builder) extends IContent {
    private def asSupplier(b: ForgeConfigSpec.BooleanValue): BoolSupplier = () => b.get()

    private def asSupplier[T](b: ForgeConfigSpec.ConfigValue[T]): java.util.function.Supplier[T] = () => b.get()

    builder.comment("Settings for FluidTank.").push("common")
    val removeRecipe: BoolSupplier = asSupplier(builder.worldRestart().comment("Remove all recipe to make tanks.")
      .define("RemoveRecipe", false))
    val debug: BoolSupplier = asSupplier(builder.comment("Debug Mode").define("debug", false))

    builder.comment("Recipe settings").push(CATEGORY_RECIPE)

    val easyRecipe: BoolSupplier = asSupplier(builder.worldRestart().comment("True to use easy recipe.")
      .define("easyRecipe", false))
    val usableUnavailableTankInRecipe: BoolSupplier = asSupplier(builder.worldRestart()
      .comment("False to prohibit tanks with no recipe from being used in recipes of other tanks.")
      .define("usableUnavailableTankInRecipe", true))

    builder.pop()

    val showInvisibleTank: BoolSupplier = asSupplier(builder.worldRestart()
      .comment("True to show invisible tank in creative tabs. Recipe and block aren't removed.")
      .define("showInvisibleTankInTab", false))

    val showTOP: BoolSupplier = asSupplier(builder.comment("Show tank info on TOP tooltip.")
      .define("showTOP", true))

    val enableWailaAndTOP: BoolSupplier = asSupplier(builder.comment("True to enable waila and top to show tank info.")
      .define("showToolTipOnMods", true))

    val enableFluidSupplier: BoolSupplier = asSupplier(builder.comment("True to allow fluid supplier to work.")
      .define("enableFluidSupplier", false))

    val enablePipeRainbowRenderer: BoolSupplier = asSupplier(builder.worldRestart()
      .comment("False to disable rainbow renderer for pipe.").define("enablePipeRainbowRenderer", false))
    val pipeColor: Supplier[Integer] = asSupplier(builder.worldRestart()
      .comment("Default color of pipe. Works only if \'enablePipeRainbowRenderer\' is false.")
      .define("pipeColor", Int.box(0xF0000000 + 0xFFFFFF)))
    val renderLowerBound: Supplier[JDouble] = asSupplier(builder.worldRestart()
      .comment("The lower bound of position of fluid rendering. Default 0.001")
      .defineInRange("renderLowerBound", 0.001d, 0d, 1d))
    val renderUpperBound: Supplier[JDouble] = asSupplier(builder.worldRestart()
      .comment("The upper bound of position of fluid rendering. Default 0.999")
      .defineInRange("renderUpperBound", 1d - 0.001d, 0d, 1d))
    builder.pop()

  }

}

trait IContent {

  import com.kotori316.fluidtank.Config.BoolSupplier

  //noinspection MutatorLikeMethodIsParameterless
  def removeRecipe: BoolSupplier
  def debug: BoolSupplier
  def easyRecipe: BoolSupplier
  def usableUnavailableTankInRecipe: BoolSupplier
  def showInvisibleTank: BoolSupplier
  def showTOP: BoolSupplier
  def enableWailaAndTOP: BoolSupplier
  def enableFluidSupplier: BoolSupplier
  def enablePipeRainbowRenderer: BoolSupplier
  def pipeColor: Supplier[Integer]
  def renderLowerBound: Supplier[JDouble]
  def renderUpperBound: Supplier[JDouble]
}