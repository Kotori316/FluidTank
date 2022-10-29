package com.kotori316.fluidtank.integration.mekanism_gas

import com.kotori316.fluidtank.FluidTank
import net.minecraft.world.item.BlockItem

class ItemBlockGasTank(val blockGasTank: BlockGasTank)
  extends BlockItem(blockGasTank, FluidTank.proxy.getTankProperties) {
  setRegistryName(FluidTank.modID, "gas_tank_" + blockGasTank.tier.toString.toLowerCase)

  override def toString: String = String.valueOf(getRegistryName)
}
