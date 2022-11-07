package com.kotori316.fluidtank.integration.top

import com.kotori316.fluidtank.integration.Localize._
import com.kotori316.fluidtank.tiles.{FluidSourceTile, TileTank, TileTankVoid}
import com.kotori316.fluidtank.{Config, FluidTank}
import mcjty.theoneprobe.api.{IProbeHitData, IProbeInfo, IProbeInfoProvider, ProbeMode}
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
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
        probeInfo.text(Component.translatable(TIER, v.tier))
      case tank: TileTank =>
        val list = FluidTankTOPPlugin.toInfo(compact, short,
          contentName = tank.connection.getFluidStack.map(_.getLocalizedName).getOrElse(FLUID_NULL),
          amount = tank.connection.amount,
          capacity = tank.connection.capacity,
          tier = tank.tier,
          hasCreative = tank.connection.hasCreative,
          comparatorLevel = tank.getComparatorLevel,
        )
        list.foreach(probeInfo.text)
      case source: FluidSourceTile =>
        probeInfo.text(Component.literal(source.fluid.toString))
      case _ =>
    }
  }
}
