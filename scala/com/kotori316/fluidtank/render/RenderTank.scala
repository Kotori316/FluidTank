package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.tiles.TileTank
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.animation.FastTESR
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class RenderTank extends FastTESR[TileTank] {

  override def renderTileEntityFast(te: TileTank, x: Double, y: Double, z: Double, partialTicks: Float,
                                    destroyStage: Int, partial: Float, buffer: BufferBuilder): Unit = {
    Minecraft.getMinecraft.mcProfiler.startSection("RenderTank")
    if (te.hasContent) {
      val tank = te.tank
      if (tank.box != null) {
        val texture = Minecraft.getMinecraft.getTextureMapBlocks.getAtlasSprite(tank.getFluid.getFluid.getStill.toString)
        val color = if (tank.getFluid == null) 0 else tank.getFluid.getFluid.getColor(tank.getFluid)
        val brightness = if (te.hasWorld) te.getWorld.getCombinedLight(te.getPos, tank.getFluid.getFluid.getLuminosity(tank.getFluid))
        else 0x00f000f0
        buffer.setTranslation(x, y, z)
        tank.box.render(buffer, texture, color >> 24 & 0xFF, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)(new Box.LightValue(brightness))
        buffer.setTranslation(0, 0, 0)
      }
    }

    Minecraft.getMinecraft.mcProfiler.endSection()
  }

  /**
    * Need here to make public.<br>
    * For [[com.kotori316.fluidtank.render.RenderItemTank]]
    */
  override def bindTexture(location: ResourceLocation): Unit = super.bindTexture(location)
}
