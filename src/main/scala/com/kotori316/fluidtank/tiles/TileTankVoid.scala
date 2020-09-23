package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.fluids.FluidAmount

class TileTankVoid extends TileTankNoDisplay(Tiers.VOID, ModObjects.TANK_VOID_TYPE) {
  override val tank = new VoidTank

  class VoidTank extends Tank {
    override def toString = "Void Tank"

    override def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Int): FluidAmount = fluidAmount

    override def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Int): FluidAmount = FluidAmount.EMPTY
  }

}
