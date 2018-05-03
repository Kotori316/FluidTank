package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.tiles.TileTank
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{BufferBuilder, GlStateManager, RenderHelper, Tessellator}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
class RenderTank extends TileEntitySpecialRenderer[TileTank] {
    lazy val blockRenderer = Minecraft.getMinecraft.getBlockRendererDispatcher

    override def render(te: TileTank, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, partial: Float): Unit = {
        val tessellator = Tessellator.getInstance
        val buffer = tessellator.getBuffer
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)

        RenderHelper.disableStandardItemLighting()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableBlend()
        //        GlStateManager.disableCull()
        if (Minecraft.isAmbientOcclusionEnabled)
            GlStateManager.shadeModel(GL11.GL_SMOOTH)
        else
            GlStateManager.shadeModel(GL11.GL_FLAT)
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK)

        renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, partial, buffer)

        buffer.setTranslation(0, 0, 0)
        tessellator.draw()
        RenderHelper.enableStandardItemLighting()
    }

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
