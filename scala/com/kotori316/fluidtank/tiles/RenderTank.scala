package com.kotori316.fluidtank.tiles

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraftforge.client.model.animation.FastTESR
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class RenderTank extends FastTESR[TileTank] {
    val d = 1d / 16d
    val a = 0.001

    override def renderTileEntityFast(te: TileTank, x: Double, y: Double, z: Double, partialTicks: Float,
                                      destroyStage: Int, partial: Float, buffer: BufferBuilder) = {
        Minecraft.getMinecraft.mcProfiler.startSection("RenderTank")
        if (te.hasContent) {
            val tank = te.tank
            val percent = tank.getFluidAmount.toDouble / tank.getCapacity.toDouble
            if (percent > a) {
                val texture = Minecraft.getMinecraft.getTextureMapBlocks.getAtlasSprite(tank.getFluid.getFluid.getStill.toString)
                var maxY = 0d
                var minY = 0d
                if (tank.getFluid.getFluid.isGaseous(tank.getFluid)) {
                    maxY = 1d - a
                    minY = 1d - percent + a
                } else {
                    minY = a
                    maxY = percent - a
                }
                buffer.setTranslation(x, y, z)
                Box(d * 8, minY, d * 8, d * 8, maxY, d * 8, d * 12 - a, percent, d * 12 - a, firstSide = true, endSide = true).render(buffer, texture)
            }
        }
        Minecraft.getMinecraft.mcProfiler.endSection()
    }
}
