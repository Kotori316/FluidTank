package com.kotori316.fluidtank.integration.top

import com.kotori316.fluidtank.integration.Localize._
import com.kotori316.fluidtank.tiles.TileTankNoDisplay
import com.kotori316.fluidtank.{Config, FluidTank}
import mcjty.theoneprobe.api.{IProbeHitData, IProbeInfo, IProbeInfoProvider, ProbeMode}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

class TankDataProvider extends IProbeInfoProvider {
  override def getID = FluidTank.modID + ":toptank"

  //noinspection ScalaDeprecation
  override def addProbeInfo(mode: ProbeMode, probeInfo: IProbeInfo, player: EntityPlayer, world: World, blockState: IBlockState, data: IProbeHitData): Unit = {
    import net.minecraft.util.text.translation.I18n
    val entity = world.getTileEntity(data.getPos)
    entity match {
      case tank: TileTankNoDisplay if Config.content.showTOP =>
        val tier = I18n.translateToLocalFormatted(WAILA_TIER, tank.tier.toString)
        val fluid = I18n.translateToLocalFormatted(WAILA_CONTENT, tank.connection.getFluidStack.map(_.getLocalizedName).getOrElse(FLUID_NULL))
        val list = if (tank.connection.hasCreative) Seq(tier, fluid)
        else {
          val amount = I18n.translateToLocalFormatted(WAILA_AMOUNT, Long.box(tank.connection.amount))
          val capacity = I18n.translateToLocalFormatted(WAILA_CAPACITY, Long.box(tank.connection.capacity))
          val comparator = I18n.translateToLocalFormatted(WAILA_COMPARATOR, Int.box(tank.getComparatorLevel))
          Seq(tier, fluid, amount, capacity, comparator)
        }
        list.foreach(probeInfo.text)
      case _ =>
    }
  }
}