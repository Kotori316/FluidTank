package com.kotori316.fluidtank.render

import java.awt.Color

import com.kotori316.fluidtank.network.ClientProxy
import com.kotori316.fluidtank.transport.{PipeBlock, PipeTile}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
/*
@OnlyIn(Dist.CLIENT)
class RenderPipe extends TileEntityRendererFast[PipeTile] {
  override def renderTileEntityFast(te: PipeTile, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, buffer: BufferBuilder): Unit = {
    import RenderPipe.d
    val maxD = 12 * d - 0.01
    val minD = 4 * d + 0.01
    Minecraft.getInstance.getProfiler.startSection("RenderPipe")

    val texture = ClientProxy.whiteTexture
    val minU = texture.getMinU
    val minV = texture.getMinV
    val maxU = texture.getMaxU
    val maxV = texture.getMaxV
    val light = implicitly[Box.LightValue]
    buffer.setTranslation(x, y, z)
    val time = te.getWorld.getGameTime
    val color = Color.HSBtoRGB((time % RenderPipe.duration).toFloat / RenderPipe.duration, 1f, 1f)
    val red = color >> 16 & 0xFF
    val green = color >> 8 & 0xFF
    val blue = color >> 0 & 0xFF
    val alpha = 240
    //    RenderPipe.BOX_AABB.render(buffer, texture, 128, red, green, blue)
    if (te.getBlockState.get(PipeBlock.NORTH).hasConnection) {
      RenderPipe.North_AABB.render(buffer, texture, alpha, red, green, blue)
    } else {
      buffer.pos(maxD, maxD, minD).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, minD, minD).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, minD).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, maxD, minD).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }
    if (te.getBlockState.get(PipeBlock.SOUTH).hasConnection) {
      RenderPipe.South_AABB.render(buffer, texture, alpha, red, green, blue)
    } else {
      buffer.pos(minD, maxD, maxD).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, maxD).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, minD, maxD).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, maxD, maxD).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }
    if (te.getBlockState.get(PipeBlock.WEST).hasConnection) {
      RenderPipe.West_AABB.render(buffer, texture, alpha, red, green, blue)
    } else {
      buffer.pos(minD, maxD, minD).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, minD).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, maxD).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, maxD, maxD).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }
    if (te.getBlockState.get(PipeBlock.EAST).hasConnection) {
      RenderPipe.East_AABB.render(buffer, texture, alpha, red, green, blue)
    } else {
      buffer.pos(maxD, maxD, maxD).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, minD, maxD).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, minD, minD).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, maxD, minD).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }
    if (te.getBlockState.get(PipeBlock.UP).hasConnection) {
      RenderPipe.UP_AABB.render(buffer, texture, alpha, red, green, blue)
    } else {
      buffer.pos(minD, maxD, minD).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, maxD, maxD).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, maxD, maxD).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, maxD, minD).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }
    if (te.getBlockState.get(PipeBlock.DOWN).hasConnection) {
      RenderPipe.Down_AABB.render(buffer, texture, alpha, red, green, blue)
    } else {
      buffer.pos(maxD, minD, minD).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, minD, maxD).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, maxD).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, minD).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }

    buffer.setTranslation(0, 0, 0)
    Minecraft.getInstance.getProfiler.endSection()
  }
}
*/
object RenderPipe {
  private final val d = 1d / 16d
  final val duration = 200
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
