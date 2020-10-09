package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.tiles.TileTank
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.{TileEntityRenderer, TileEntityRendererDispatcher}
import net.minecraft.client.renderer.{IRenderTypeBuffer, RenderType}
import net.minecraft.inventory.container.PlayerContainer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

@OnlyIn(Dist.CLIENT)
class RenderTank(d: TileEntityRendererDispatcher) extends TileEntityRenderer[TileTank](d) {

  override def render(te: TileTank, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, light: Int, otherLight: Int): Unit = {
    Minecraft.getInstance.getProfiler.startSection("RenderTank")
    if (te.hasContent) {
      matrix.push()
      val b = buffer.getBuffer(RenderType.getCutout)
      val tank = te.internalTank
      if (tank.box != null) {
        val resource = RenderTank.textureName(te)
        val texture = Minecraft.getInstance.getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(resource)
        val color = RenderTank.color(te)

        val value = Box.LightValue(light).overrideBlock(te.internalTank.getFluid.fluid.getAttributes.getLuminosity(te.internalTank.getFluid.toStack))
        tank.box.render(b, matrix, texture, color >> 24 & 0xFF, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)(value)
      }
      matrix.pop()
    }
    Minecraft.getInstance.getProfiler.endSection()
  }
}

object RenderTank {
  private def textureName(tile: TileTank) = {
    val tank = tile.internalTank
    val (world, pos) = worldAndPos(tile)
    tank.getFluid.fluid.getAttributes.getStillTexture(world, pos)
  }

  private def color(tile: TileTank) = {
    val fluidAmount = tile.internalTank.getFluid
    val (world, pos) = worldAndPos(tile)
    fluidAmount.fluid.getAttributes.getColor(world, pos)
  }

  private[this] def worldAndPos(tileTank: TileTank): (World, BlockPos) = {
    if (tileTank.hasWorld) (tileTank.getWorld, tileTank.getPos)
    else (Minecraft.getInstance().world, Minecraft.getInstance().player.getPosition)
  }

}
