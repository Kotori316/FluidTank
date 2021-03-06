package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.fluids.VoidTankHandler

class TileTankVoid extends TileTankNoDisplay(Tiers.VOID, ModObjects.TANK_VOID_TYPE) {
  override val internalTank = new VoidTank

  class VoidTank extends VoidTankHandler with TileTankNoDisplay.RealTank {
    override def toString = "Void Tank"

    override def tile: TileTankNoDisplay = TileTankVoid.this
  }

}
