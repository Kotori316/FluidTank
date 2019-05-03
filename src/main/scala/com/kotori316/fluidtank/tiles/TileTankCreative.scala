package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank._

class TileTankCreative extends TileTank(Tiers.CREATIVE, ModObjects.TANK_CREATIVE_TYPE) {

  override val tank = new CreativeTank

  class CreativeTank extends Tank {
    override def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Int) = {
      if (fluidAmount.nonEmpty) {
        if (doFill) {
          if (fluid.isEmpty) {
            fluid = fluidAmount.setAmount(capacity)
            onContentsChanged()
            fluidAmount
          } else {
            FluidAmount.EMPTY
          }
        } else {
          if (fluid.isEmpty) fluidAmount else FluidAmount.EMPTY
        }
      } else {
        FluidAmount.EMPTY
      }
    }

    override def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Int) = {
      if (fluid.isEmpty || fluidAmount.amount < min) FluidAmount.EMPTY
      else fluid.setAmount(fluidAmount.amount)
    }

    def drainAll(): Unit = {
      fluid = FluidAmount.EMPTY
      onContentsChanged()
    }
  }

}
