package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.ModObjects
import net.minecraft.tileentity.TileEntityType

class TileTank(t: Tiers, ty: TileEntityType[_ <: TileTank]) extends TileTankNoDisplay(t, ty) {
  def this() = {
    this(Tiers.Invalid, ModObjects.TANK_TYPE)
  }

  def this(t: Tiers) = {
    this(t, ModObjects.TANK_TYPE)
  }
}
