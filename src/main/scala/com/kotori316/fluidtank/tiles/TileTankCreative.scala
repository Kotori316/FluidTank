package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.fluids.CreativeTankHandler

class TileTankCreative extends TileTank(Tiers.CREATIVE, ModObjects.TANK_CREATIVE_TYPE) {

  override val internalTank = new CreativeTank

  class CreativeTank extends CreativeTankHandler with TileTank.RealTank {
    override def tile: TileTank = TileTankCreative.this
  }

}
