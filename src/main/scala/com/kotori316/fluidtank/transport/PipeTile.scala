package com.kotori316.fluidtank.transport

import com.kotori316.fluidtank.ModObjects
import net.minecraft.tileentity.{ITickableTileEntity, TileEntity}

class PipeTile extends TileEntity(ModObjects.PIPE_TYPE) with ITickableTileEntity {
  override def tick(): Unit = {
    if (!world.isRemote) {

    }
  }
}
