package com.kotori316.fluidtank.integration.top

import com.kotori316.fluidtank.integration.Localize._
import com.kotori316.fluidtank.tiles.{FluidSourceTile, TileTankNoDisplay, TileTankVoid}
import com.kotori316.fluidtank.{Config, FluidTank}
import mcjty.theoneprobe.api.{IProbeHitData, IProbeInfo, IProbeInfoProvider, ProbeMode}
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.{StringTextComponent, TranslationTextComponent}
import net.minecraft.world.World

class TankDataProvider extends IProbeInfoProvider {
  override def getID: String = FluidTank.modID + ":toptank"

  private lazy val showTOP = Config.content.showTOP.get().booleanValue()

  override def addProbeInfo(probeMode: ProbeMode, probeInfo: IProbeInfo, playerEntity: PlayerEntity,
                            world: World, blockState: BlockState, data: IProbeHitData): Unit = {
    if (!showTOP) return
    val entity = world.getTileEntity(data.getPos)
    entity match {
      case v: TileTankVoid =>
        probeInfo.text(new TranslationTextComponent(WAILA_TIER, v.tier))
      case tank: TileTankNoDisplay =>
        val tier = new TranslationTextComponent(WAILA_TIER, tank.tier) //I18n.translateToLocalFormatted(WAILA_TIER, tank.tier.toString)
        val fluid = new TranslationTextComponent(WAILA_CONTENT, tank.connection.getFluidStack.map(_.getLocalizedName).getOrElse(FLUID_NULL))
        val list = if (tank.connection.hasCreative) Seq(tier, fluid)
        else {
          val amount = new TranslationTextComponent(WAILA_AMOUNT, Long.box(tank.connection.amount))
          val capacity = new TranslationTextComponent(WAILA_CAPACITY, Long.box(tank.connection.capacity))
          val comparator = new TranslationTextComponent(WAILA_COMPARATOR, Int.box(tank.getComparatorLevel))
          Seq(tier, fluid, amount, capacity, comparator)
        }
        list.foreach(probeInfo.text)
      case source: FluidSourceTile =>
        probeInfo.text(new StringTextComponent(source.fluid.toString))
      case _ =>
    }
  }
}
