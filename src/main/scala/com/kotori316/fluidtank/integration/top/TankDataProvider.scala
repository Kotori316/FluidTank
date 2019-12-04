package com.kotori316.fluidtank.integration.top

import com.kotori316.fluidtank.integration.Localize._
import com.kotori316.fluidtank.tiles.TileTankNoDisplay
import com.kotori316.fluidtank.{Config, FluidTank}
import mcjty.theoneprobe.api.{IProbeHitData, IProbeInfo, IProbeInfoProvider, ProbeMode}
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World

class TankDataProvider extends IProbeInfoProvider {
  override def getID = FluidTank.modID + ":toptank"

  //noinspection ScalaDeprecation
  override def addProbeInfo(probeMode: ProbeMode, probeInfo: IProbeInfo, playerEntity: PlayerEntity,
                            world: World, blockState: BlockState, data: IProbeHitData): Unit = {
    val entity = world.getTileEntity(data.getPos)
    entity match {
      case tank: TileTankNoDisplay if Config.content.showTOP.get() =>
        val tier = s"Tier: ${tank.tier.toString}" //I18n.translateToLocalFormatted(WAILA_TIER, tank.tier.toString)
        val fluid = s"Content: ${tank.connection.getFluidStack.map(_.getLocalizedName).getOrElse(FLUID_NULL)}" //I18n.translateToLocalFormatted(WAILA_CONTENT, tank.connection.getFluidStack.map(_.getLocalizedName).getOrElse(FLUID_NULL))
        val list = if (tank.connection.hasCreative) Seq(tier, fluid)
        else {
          val amount = s"Amount: ${Long.box(tank.connection.amount)}" //I18n.translateToLocalFormatted(WAILA_AMOUNT, Long.box(tank.connection.amount))
          val capacity = s"Capacity: ${Long.box(tank.connection.capacity)}" //I18n.translateToLocalFormatted(WAILA_CAPACITY, Long.box(tank.connection.capacity))
          val comparator = s"Level: ${Int.box(tank.getComparatorLevel)}" //I18n.translateToLocalFormatted(WAILA_COMPARATOR, Int.box(tank.getComparatorLevel))
          Seq(tier, fluid, amount, capacity, comparator)
        }
        list.foreach(probeInfo.text)
      case _ =>
    }
  }
}
