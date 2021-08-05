package com.kotori316.fluidtank.tank

import com.kotori316.fluidtank.{FluidAmount, ModTank}
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class TileTankVoid(pos: BlockPos, state: BlockState) extends TileTank(Tiers.VOID, ModTank.Entries.VOID_BLOCK_ENTITY_TYPE, pos, state) {
  override val tank: Tank = new VoidTank

  class VoidTank extends Tank {
    override def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Long): FluidAmount = {
      // Accept all fluid, but it'll be ignored.
      fluidAmount
    }

    override def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Long): FluidAmount = {
      FluidAmount.EMPTY
    }
  }
}
