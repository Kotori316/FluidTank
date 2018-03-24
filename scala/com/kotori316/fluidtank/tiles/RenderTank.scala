package com.kotori316.fluidtank.tiles

import net.minecraft.client.renderer.BufferBuilder
import net.minecraftforge.client.model.animation.FastTESR

class RenderTank extends FastTESR[TileTank] {
    override def renderTileEntityFast(te: TileTank, x: Double, y: Double, z: Double, partialTicks: Float,
                                      destroyStage: Int, partial: Float, buffer: BufferBuilder) = {

    }
}
