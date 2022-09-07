package com.kotori316.fluidtank.integration.top

import java.math.RoundingMode
import java.text.NumberFormat

import com.kotori316.fluidtank.integration.Localize._
import com.kotori316.fluidtank.tiles.{FluidSourceTile, TileTank, TileTankVoid}
import com.kotori316.fluidtank.{Config, FluidTank}
import mcjty.theoneprobe.api.{IProbeHitData, IProbeInfo, IProbeInfoProvider, ProbeMode}
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.{Component, TextComponent, TranslatableComponent}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class TankDataProvider extends IProbeInfoProvider {
  override def getID = new ResourceLocation(FluidTank.modID, "toptank")

  private lazy val showTOP = Config.content.showTOP.get().booleanValue()
  private lazy val short = Config.content.topShort.get().booleanValue()
  private lazy val compact = Config.content.topCompact.get().booleanValue()

  override def addProbeInfo(probeMode: ProbeMode, probeInfo: IProbeInfo, playerEntity: Player,
                            world: Level, blockState: BlockState, data: IProbeHitData): Unit = {
    if (!showTOP) return
    val entity = world.getBlockEntity(data.getPos)
    entity match {
      case v: TileTankVoid =>
        probeInfo.text(new TranslatableComponent(TIER, v.tier))
      case tank: TileTank =>
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
        val list = if (short) shortInfo(tank, numberFormatter, fluidNameFormatter) else longInfo(tank, numberFormatter, fluidNameFormatter)
        list.foreach(probeInfo.text)
      case source: FluidSourceTile =>
        probeInfo.text(new TextComponent(source.fluid.toString))
      case _ =>
    }
  }

  def shortInfo(tank: TileTank, numberFormatter: Number => String, fluidNameFormatter: String => String): Seq[Component] = {
    val fluid = fluidNameFormatter(tank.connection.getFluidStack.map(_.getLocalizedName).getOrElse(FLUID_NULL))
    if (tank.connection.hasCreative) {
      Seq(new TextComponent(fluid))
    } else {
      val amount = numberFormatter(Long.box(tank.connection.amount))
      val capacity = numberFormatter(Long.box(tank.connection.capacity))
      Seq(new TranslatableComponent(WAILA_SHORT, fluid, amount, capacity))
    }
  }

  def longInfo(tank: TileTank, numberFormatter: Number => String, fluidNameFormatter: String => String): Seq[Component] = {
    val tier = new TranslatableComponent(TIER, tank.tier)
    val fluid = new TranslatableComponent(CONTENT, fluidNameFormatter(tank.connection.getFluidStack.map(_.getLocalizedName).getOrElse(FLUID_NULL)))
    if (tank.connection.hasCreative) {
      Seq(tier, fluid)
    } else {
      val amount = new TranslatableComponent(AMOUNT, numberFormatter(Long.box(tank.connection.amount)))
      val capacity = new TranslatableComponent(CAPACITY, numberFormatter(Long.box(tank.connection.capacity)))
      val comparator = new TranslatableComponent(COMPARATOR, Int.box(tank.getComparatorLevel))
      Seq(tier, fluid, amount, capacity, comparator)
    }
  }
}
