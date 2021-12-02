package com.kotori316.fluidtank.integration.top
/*
import com.kotori316.fluidtank.integration.Localize._
import com.kotori316.fluidtank.tiles.{FluidSourceTile, TileTank, TileTankVoid}
import com.kotori316.fluidtank.{Config, FluidTank}
import mcjty.theoneprobe.api.{IProbeHitData, IProbeInfo, IProbeInfoProvider, ProbeMode}
import net.minecraft.network.chat.{TextComponent, TranslatableComponent}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class TankDataProvider extends IProbeInfoProvider {
  override def getID = new ResourceLocation(FluidTank.modID, "toptank")

  private lazy val showTOP = Config.content.showTOP.get().booleanValue()

  override def addProbeInfo(probeMode: ProbeMode, probeInfo: IProbeInfo, playerEntity: Player,
                            world: Level, blockState: BlockState, data: IProbeHitData): Unit = {
    if (!showTOP) return
    val entity = world.getBlockEntity(data.getPos)
    entity match {
      case v: TileTankVoid =>
        probeInfo.text(new TranslatableComponent(TIER, v.tier))
      case tank: TileTank =>
        val tier = new TranslatableComponent(TIER, tank.tier) //I18n.translateToLocalFormatted(WAILA_TIER, tank.tier.toString)
        val fluid = new TranslatableComponent(CONTENT, tank.connection.getFluidStack.map(_.getLocalizedName).getOrElse(FLUID_NULL))
        val list = if (tank.connection.hasCreative) Seq(tier, fluid)
        else {
          val amount = new TranslatableComponent(AMOUNT, Long.box(tank.connection.amount))
          val capacity = new TranslatableComponent(CAPACITY, Long.box(tank.connection.capacity))
          val comparator = new TranslatableComponent(COMPARATOR, Int.box(tank.getComparatorLevel))
          Seq(tier, fluid, amount, capacity, comparator)
        }
        list.foreach(probeInfo.text)
      case source: FluidSourceTile =>
        probeInfo.text(new TextComponent(source.fluid.toString))
      case _ =>
    }
  }
}*/
