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
import net.minecraftforge.fluids.FluidAttributes

@OnlyIn(Dist.CLIENT)
class RenderTank(d: TileEntityRendererDispatcher) extends TileEntityRenderer[TileTank](d) {

  override def render(te: TileTank, partialTicks: Float, matrix: MatrixStack, buffer: IRenderTypeBuffer, light: Int, otherLight: Int): Unit = {
    Minecraft.getInstance.getProfiler.startSection("RenderTank")
    if (te.hasContent) {
      matrix.push()
      val b = buffer.getBuffer(RenderType.getTranslucent)
      val tank = te.internalTank
      if (tank.box != null) {
        val resource = RenderTank.textureName(te)
        val texture = Minecraft.getInstance.getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(resource)
        val color = RenderTank.color(te)

        val value = Box.LightValue(light).overrideBlock(te.internalTank.getFluid.fluid.getAttributes.getLuminosity(te.internalTank.getFluid.toStack))
        val alpha = if ((color >> 24 & 0xFF) > 0) color >> 24 & 0xFF else 0xFF
        tank.box.render(b, matrix, texture, alpha, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)(value)
      }
      matrix.pop()
    }
    Minecraft.getInstance.getProfiler.endSection()
  }
}

object RenderTank {
  private def textureName(tile: TileTank) = {
    val world = getTankWorld(tile)
    val pos = getTankPos(tile)
    tile.internalTank.getFluid.fluid.getAttributes.getStillTexture(world, pos)
  }

  private def color(tile: TileTank) = {
    val fluidAmount = tile.internalTank.getFluid
    val attributes = fluidAmount.fluid.getAttributes
    val normal = attributes.getColor
    if (attributes.getClass == classOf[FluidAttributes]) {
      normal
    } else {
      val stackColor = attributes.getColor(fluidAmount.toStack)
      if (normal == stackColor) {
        val world = getTankWorld(tile)
        val pos = getTankPos(tile)
        val worldColor = attributes.getColor(world, pos)
        worldColor
      } else {
        stackColor
      }
    }
  }

  private final def getTankWorld(tileTank: TileTank): World = {
    if (tileTank.hasWorld) tileTank.getWorld else Minecraft.getInstance.world
  }

  private final def getTankPos(tileTank: TileTank): BlockPos = {
    if (tileTank.hasWorld) tileTank.getPos else Minecraft.getInstance.player.getPosition
  }

}
