package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.FluidTank
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraftforge.client.model.animation.FastTESR
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class RenderTank extends FastTESR[TileTank] {
    lazy val blockRenderer = Minecraft.getMinecraft.getBlockRendererDispatcher

    override def renderTileEntityFast(te: TileTank, x: Double, y: Double, z: Double, partialTicks: Float,
                                      destroyStage: Int, partial: Float, buffer: BufferBuilder) = {
        Minecraft.getMinecraft.mcProfiler.startSection("RenderTank")
        if (te.hasContent) {
            val tank = te.tank
            if (tank.box != null) {
                val texture = Minecraft.getMinecraft.getTextureMapBlocks.getAtlasSprite(tank.getFluid.getFluid.getStill.toString)
                val brightness = if (te.hasWorld) te.getWorld.getCombinedLight(te.getPos, tank.getFluid.getFluid.getLuminosity(tank.getFluid))
                else 0x00f000f0
                buffer.setTranslation(x, y, z)
                tank.box.render(buffer, texture)(Box.LightValue(brightness))
            }
        }
        val state = FluidTank.BLOCK_TANKS.get(te.tier.rank - 1).getStateFromMeta(te.tier.meta)
        val model = blockRenderer.getModelForState(state)
        val pos = te.getPos
        buffer.setTranslation(x - pos.getX, y - pos.getY, z - pos.getZ)

        blockRenderer.getBlockModelRenderer.renderModel(Minecraft.getMinecraft.world, model, state, pos, buffer, false)

        Minecraft.getMinecraft.mcProfiler.endSection()
    }
}
