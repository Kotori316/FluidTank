package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.tiles.TileTank
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.tags.FluidTags
import net.minecraft.util.ResourceLocation
import net.minecraft.world.biome.BiomeColors
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.client.model.animation.TileEntityRendererFast

@OnlyIn(Dist.CLIENT)
class RenderTank extends TileEntityRendererFast[TileTank] {

  override def renderTileEntityFast(te: TileTank, x: Double, y: Double, z: Double, partialTicks: Float,
                                    destroyStage: Int, buffer: BufferBuilder): Unit = {
    Minecraft.getInstance.getProfiler.startSection("RenderTank")
    if (te.hasContent) {
      val tank = te.tank
      if (tank.box != null) {
        val resource = RenderTank.textureName(te)
        val texture = Minecraft.getInstance.getTextureMap.getSprite(resource)
        val color = RenderTank.color(te)
        val brightness = if (te.hasWorld) te.getWorld.getCombinedLight(te.getPos, if (tank.fluid.fluid.isIn(FluidTags.LAVA)) 15 else 0)
        else 0x00f000f0
        buffer.setTranslation(x, y, z)
        tank.box.render(buffer, texture, color >> 24 & 0xFF, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)(new Box.LightValue(brightness))
      }
    }

    Minecraft.getInstance.getProfiler.endSection()
  }

  /**
   * Need here to make public.<br>
   * For [[com.kotori316.fluidtank.render.RenderItemTank]]
   */
  override def bindTexture(location: ResourceLocation): Unit = super.bindTexture(location)
}

object RenderTank {
  private def textureName(tile: TileTank) = {
    val tank: TileTank#Tank = tile.tank
    val world = if (tile.hasWorld) tile.getWorld else Minecraft.getInstance().world
    val pos = if (tile.hasWorld) tile.getPos else Minecraft.getInstance().player.getPosition
    tank.fluid.fluid.getAttributes.getStill(world, pos)
  }

  private def color(tile: TileTank) = {
    val fluidAmount = tile.tank.fluid
    val world = if (tile.hasWorld) tile.getWorld else Minecraft.getInstance().world
    val pos = if (tile.hasWorld) tile.getPos else Minecraft.getInstance().player.getPosition
    if (fluidAmount.fluid.isIn(FluidTags.WATER)) {
      BiomeColors.getWaterColor(world, pos) | 0xFF000000
    } else {
      fluidAmount.fluid.getAttributes.getColor(world, pos)
    }
  }
}