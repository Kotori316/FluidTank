package com.kotori316.fluidtank.tiles

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraftforge.client.model.animation.FastTESR
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class RenderTank extends FastTESR[TileTank] {

    override def renderTileEntityFast(te: TileTank, x: Double, y: Double, z: Double, partialTicks: Float,
                                      destroyStage: Int, partial: Float, buffer: BufferBuilder) = {
        Minecraft.getMinecraft.mcProfiler.startSection("RenderTank")
        if (te.hasContent) {
            val tank = te.tank
            if (tank.box != null) {
                val texture = Minecraft.getMinecraft.getTextureMapBlocks.getAtlasSprite(tank.getFluid.getFluid.getStill.toString)
                buffer.setTranslation(x, y, z)
                tank.box.render(buffer, texture)
            }
        }
        Minecraft.getMinecraft.mcProfiler.endSection()
    }
}
