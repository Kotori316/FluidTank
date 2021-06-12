package com.kotori316.fluidtank.tank

import com.kotori316.fluidtank.ModTank.Entries
import com.kotori316.fluidtank._
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class TileTankCreative(pos: BlockPos, state: BlockState) extends TileTank(Tiers.CREATIVE, Entries.CREATIVE_BLOCK_ENTITY_TYPE, pos, state) {

  override val tank = new CreativeTank

  class CreativeTank extends Tank {
    override def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Long): FluidAmount = {
      if (fluidAmount.nonEmpty) {
        if (doFill) {
          if (fluid.isEmpty) {
            fluid = fluidAmount.setAmount(capacity)
            onContentsChanged(FluidAmount.EMPTY)
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

    override def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Long): FluidAmount = {
      if (FluidAmount.EMPTY.fluidEqual(fluidAmount) || fluidAmount.fluidVolume.amount().asLong(1000L) >= min) fluid.setAmount(fluidAmount.fluidVolume.amount())
      else FluidAmount.EMPTY
    }

    def drainAll(): Unit = {
      val previous = fluid
      fluid = FluidAmount.EMPTY
      onContentsChanged(previous)
    }
  }

}
