package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.fluids.CreativeTankHandler
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class TileTankCreative(p: BlockPos, s: BlockState) extends TileTank(Tier.CREATIVE, ModObjects.TANK_CREATIVE_TYPE, p, s) {

  override val internalTank = new CreativeTank

  class CreativeTank extends CreativeTankHandler with TileTank.RealTank {
    override def tile: TileTank = TileTankCreative.this
  }

}
