package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.fluids.VoidTankHandler
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class TileTankVoid(p: BlockPos, s: BlockState) extends TileTank(Tier.VOID, ModObjects.TANK_VOID_TYPE, p, s) {
  override val internalTank = new VoidTank

  class VoidTank extends VoidTankHandler with TileTank.RealTank {
    override def toString = "Void Tank"

    override def tile: TileTank = TileTankVoid.this
  }

}
