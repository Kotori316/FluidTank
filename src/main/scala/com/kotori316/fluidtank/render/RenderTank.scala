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
    Minecraft.getInstance.profiler.startSection("RenderTank")
    if (te.hasContent) {
      val tank = te.tank
      if (tank.box != null) {
        val resource = RenderTank.textureName(tank)
        val texture = Minecraft.getInstance.getTextureMap.getSprite(resource)
        val color = RenderTank.color(te)
        //if (tank.getFluid == null) 0 else tank.getFluid.getFluid.getColor(tank.getFluid)
        val brightness = if (te.hasWorld) te.getWorld.getCombinedLight(te.getPos, if (tank.fluid.fluid.isIn(FluidTags.LAVA)) 15 else 0)
        else 0x00f000f0
        buffer.setTranslation(x, y, z)
        tank.box.render(buffer, texture, color >> 24 & 0xFF, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)(new Box.LightValue(brightness))
      }
    }

    Minecraft.getInstance.profiler.endSection()
  }

  /**
    * Need here to make public.<br>
    * For [[com.kotori316.fluidtank.render.RenderItemTank]]
    */
  override def bindTexture(location: ResourceLocation): Unit = super.bindTexture(location)
}

object RenderTank {
  val LAVA_LOCATION = new ResourceLocation("minecraft", "block/lava_still")
  val WATER_LOCATION = new ResourceLocation("minecraft", "block/water_still")

  private def textureName(tank: TileTank#Tank) = {
    if (tank.fluid.fluid.isIn(FluidTags.LAVA)) LAVA_LOCATION else WATER_LOCATION
  }

  private def color(tile: TileTank) = {
    if (tile.tank.fluid.fluid.isIn(FluidTags.LAVA)) 0xFFFFFFFF else {
      val world = if (tile.hasWorld) tile.getWorld else Minecraft.getInstance().world
      val pos = if (tile.hasWorld) tile.getPos else Minecraft.getInstance().player.getPosition
      BiomeColors.getWaterColor(world, pos) | 0xFF000000
    }
  }
}