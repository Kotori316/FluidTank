package com.kotori316.fluidtank.integration.jade

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.blocks.BlockTank
import com.kotori316.fluidtank.integration.Localize
import com.kotori316.fluidtank.tiles.TileTank
import mcp.mobius.waila.api.{IRegistrar, IWailaPlugin, TooltipPosition, WailaPlugin}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity

@WailaPlugin
object TankWailaPlugin extends IWailaPlugin {
  final val NBT_Tier = TileTank.NBT_Tier
  final val NBT_ConnectionAmount = "ConnectionAmount"
  final val NBT_ConnectionCapacity = "ConnectionCapacity"
  final val NBT_ConnectionComparator = "Comparator"
  final val NBT_ConnectionFluidName = "FluidName"
  final val NBT_Creative = "Creative"
  final val KEY_TANK_INFO = new ResourceLocation(FluidTank.modID, Localize.WAILA_TANK_INFO)
  final val KEY_SHORT_INFO = new ResourceLocation(FluidTank.modID, Localize.WAILA_SHORT_INFO)

  override def register(registrar: IRegistrar): Unit = {
    val tankDataProvider = new TankDataProvider
    registrar.registerBlockDataProvider(tankDataProvider, classOf[BlockEntity])
    registrar.registerComponentProvider(tankDataProvider, TooltipPosition.BODY, classOf[BlockTank])
    registrar.addConfig(TankWailaPlugin.KEY_TANK_INFO, true)
    registrar.addConfig(TankWailaPlugin.KEY_SHORT_INFO, true)
  }
}
