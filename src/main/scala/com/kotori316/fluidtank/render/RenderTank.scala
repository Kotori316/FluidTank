package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.tiles.TileTank
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.{TileEntityRenderer, TileEntityRendererDispatcher}
import net.minecraft.client.renderer.{IRenderTypeBuffer, RenderType}
import net.minecraft.inventory.container.PlayerContainer
import net.minecraft.tags.FluidTags
import net.minecraft.world.biome.BiomeColors
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

@OnlyIn(Dist.CLIENT)
class RenderTank(d: TileEntityRendererDispatcher) extends TileEntityRenderer[TileTank](d) {

  override def render(te: TileTank, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, light: Int, otherLight: Int): Unit = {
    Minecraft.getInstance.getProfiler.startSection("RenderTank")
    if (te.hasContent) {
      matrix.push()
      val b = buffer.getBuffer(RenderType.getCutout)
      val tank = te.tank
      if (tank.box != null) {
        val resource = RenderTank.textureName(te)
        val texture = Minecraft.getInstance.getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(resource)
        val color = RenderTank.color(te)

        val value = Box.LightValue(light).overrideBlock(te.tank.fluid.fluid.getAttributes.getLuminosity(te.tank.fluid.toStack))
        tank.box.render(b, matrix, texture, color >> 24 & 0xFF, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)(value)
      }
      matrix.pop()
    }
    Minecraft.getInstance.getProfiler.endSection()
  }
}

object RenderTank {
  private def textureName(tile: TileTank) = {
    val tank: TileTank#Tank = tile.tank
    val world = if (tile.hasWorld) tile.getWorld else Minecraft.getInstance().world
    val pos = if (tile.hasWorld) tile.getPos else Minecraft.getInstance().player.getPosition
    tank.fluid.fluid.getAttributes.getStillTexture(world, pos)
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
