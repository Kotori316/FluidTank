package com.kotori316.fluidtank.render

import java.awt.Color

import com.kotori316.fluidtank.transport.{PipeBlock, PipeTile}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.fluid.Fluids
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.client.model.animation.TileEntityRendererFast

@OnlyIn(Dist.CLIENT)
class RenderPipe extends TileEntityRendererFast[PipeTile] {
  override def renderTileEntityFast(te: PipeTile, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, buffer: BufferBuilder): Unit = {
    Minecraft.getInstance.getProfiler.startSection("RenderPipe")

    val texture = Minecraft.getInstance.getTextureMap.getSprite(Fluids.WATER.getAttributes.getStillTexture)
    buffer.setTranslation(x, y, z)
    val time = te.getWorld.getGameTime
    val color = Color.HSBtoRGB((time % 200).toFloat / 200f, 1f, 1f)
    RenderPipe.BOX_AABB.render(buffer, texture, 128, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)
    if (te.getBlockState.get(PipeBlock.NORTH).hasConnection)
      RenderPipe.North_AABB.render(buffer, texture, 128, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)
    if (te.getBlockState.get(PipeBlock.SOUTH).hasConnection)
      RenderPipe.South_AABB.render(buffer, texture, 128, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)
    if (te.getBlockState.get(PipeBlock.WEST).hasConnection)
      RenderPipe.West_AABB.render(buffer, texture, 128, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)
    if (te.getBlockState.get(PipeBlock.EAST).hasConnection)
      RenderPipe.East_AABB.render(buffer, texture, 128, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)
    if (te.getBlockState.get(PipeBlock.UP).hasConnection)
      RenderPipe.UP_AABB.render(buffer, texture, 128, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)
    if (te.getBlockState.get(PipeBlock.DOWN).hasConnection)
      RenderPipe.Down_AABB.render(buffer, texture, 128, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)

    buffer.setTranslation(0, 0, 0)
    Minecraft.getInstance.getProfiler.endSection()
  }
}

object RenderPipe {
  private[this] final val d = 1d / 16d
  val BOX_AABB = Box.apply(
    startX = 8 * d, startY = 4 * d + 0.001, startZ = 8 * d,
    endX = 8 * d, endY = 12 * d - 0.001, endZ = 8 * d,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = true, endSide = true
  )
  val North_AABB = Box.apply(
    startX = 8 * d, startY = 8 * d, startZ = 0.001,
    endX = 8 * d, endY = 8 * d, endZ = 4 * d - 0.001,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
  val South_AABB = Box.apply(
    startX = 8 * d, startY = 8 * d, startZ = 12 * d + 0.001,
    endX = 8 * d, endY = 8 * d, endZ = 16 * d - 0.001,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
  val West_AABB = Box.apply(
    startX = 0.001, startY = 8 * d, startZ = 8 * d,
    endX = 4 * d - 0.001, endY = 8 * d, endZ = 8 * d,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
  val East_AABB = Box.apply(
    startX = 12 * d + 0.001, startY = 8 * d, startZ = 8 * d,
    endX = 16 * d - 0.001, endY = 8 * d, endZ = 8 * d,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
  val UP_AABB = Box.apply(
    startX = 8 * d, startY = 12 * d + 0.001, startZ = 8 * d,
    endX = 8 * d, endY = 16 * d - 0.001, endZ = 8 * d,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
  val Down_AABB = Box.apply(
    startX = 8 * d, startY = 0.001, startZ = 8 * d,
    endX = 8 * d, endY = 4 * d - 0.001, endZ = 8 * d,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
}