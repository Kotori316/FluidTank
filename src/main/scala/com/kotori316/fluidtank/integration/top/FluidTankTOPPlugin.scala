package com.kotori316.fluidtank.integration.top

import java.math.RoundingMode
import java.text.NumberFormat

import cats.data.{Kleisli, Reader}
import com.kotori316.fluidtank.Config
import com.kotori316.fluidtank.integration.Localize._
import com.kotori316.fluidtank.integration.mekanism_gas.GasTankTOPDataProvider
import com.kotori316.fluidtank.tiles.Tier
import mcjty.theoneprobe.api.ITheOneProbe
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.{Component, TextComponent, TranslatableComponent}
import net.minecraftforge.fml.{InterModComms, ModList}

object FluidTankTOPPlugin {
  private[this] final val TOP_ID = "theoneprobe"

  private class Function extends java.util.function.Function[ITheOneProbe, Void] {
    override def apply(t: ITheOneProbe): Void = {
      if (Config.content.enableWailaAndTOP.get()) {
        t.registerProvider(new TankDataProvider)
        if (ModList.get().isLoaded("mekanism")) {
          t.registerProvider(new GasTankTOPDataProvider)
        }
      }
      null
    }
  }

  val send: Reader[String, Boolean] = Reader {
    id =>
      InterModComms.sendTo(id, TOP_ID, "getTheOneProbe", () => new Function)
  }

  def sendIMC: Reader[String, Boolean] =
    if (ModList.get().isLoaded(TOP_ID)) send
    else Kleisli.pure(false)

  def toInfo(compact: Boolean, short: Boolean, contentName: String, amount: Long, capacity: Long, tier: Tier, comparatorLevel: Int, hasCreative: Boolean): Seq[Component] = {
    val numberFormatter: Number => String = if (compact) {
      val locale = Minecraft.getInstance.getLanguageManager.getSelected.getJavaLocale
      val formatter = NumberFormat.getCompactNumberInstance(locale, NumberFormat.Style.SHORT)
      formatter.setMinimumFractionDigits(1)
      formatter.setRoundingMode(RoundingMode.DOWN)
      formatter.format
    } else {
      _.toString
    }
    val fluidNameFormatter: String => String = if (compact) {
      s => {
        val array = s.split(":", 2)
        if (array.length == 2) array(1)
        else array(0)
      }
    } else {
      identity
    }

    val content = fluidNameFormatter(contentName)
    val amountStr = numberFormatter(Long.box(amount))
    val capacityStr = numberFormatter(Long.box(capacity))
    if (short) {
      if (hasCreative) Seq(new TextComponent(content))
      else Seq(new TranslatableComponent(WAILA_SHORT, content, amountStr, capacityStr))
    } else {
      val tierStr = new TranslatableComponent(TIER, tier)
      val contentStr = new TranslatableComponent(CONTENT, content)
      if (hasCreative) Seq(tierStr, contentStr)
      else Seq(
        tierStr,
        contentStr,
        new TranslatableComponent(AMOUNT, amountStr),
        new TranslatableComponent(CAPACITY, capacityStr),
        new TranslatableComponent(COMPARATOR, Int.box(comparatorLevel)),
      )
    }
  }
}
