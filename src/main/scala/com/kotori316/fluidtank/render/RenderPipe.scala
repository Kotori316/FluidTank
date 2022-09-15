package com.kotori316.fluidtank.render

import java.awt.Color

import com.kotori316.fluidtank.FluidTankClientInit
import com.kotori316.fluidtank.transport.{PipeBlock, PipeTileBase}
import com.mojang.blaze3d.vertex.PoseStack
import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.{MultiBufferSource, RenderType}

@Environment(EnvType.CLIENT)
class RenderPipe extends BlockEntityRenderer[PipeTileBase] {
  var useColor: Boolean = false

  override def render(te: PipeTileBase, partialTicks: Float, matrixStack: PoseStack, renderTypeBuffer: MultiBufferSource, light: Int, otherLight: Int): Unit = {

    val maxD = 12 * RenderPipe.d - 0.01
    val minD = 4 * RenderPipe.d + 0.01
    Minecraft.getInstance.getProfiler.push("RenderPipe")
    matrixStack.pushPose()

    val texture: TextureAtlasSprite = FluidTankClientInit.SPRITES.getWhite
    val minU = texture.getU0
    val minV = texture.getV0
    val maxU = texture.getU1
    val maxV = texture.getV1
    implicit val lightValue: Box.LightValue = Box.LightValue(light)
    val buffer = new Wrapper(renderTypeBuffer.getBuffer(RenderType.translucent))
    val time = te.getLevel.getGameTime
    val color = if (useColor) te.getColor else Color.HSBtoRGB((time % RenderPipe.duration).toFloat / RenderPipe.duration, 1f, 1f)
    val red = color >> 16 & 0xFF
    val green = color >> 8 & 0xFF
    val blue = color >> 0 & 0xFF
    val alpha = Math.max(color >> 24 & 0xFF, 128) // Dummy code.
    //    RenderPipe.BOX_AABB.render(buffer, texture, 128, red, green, blue)

    def drawWhite(p: ((Double, Double, Double), (Double, Double, Double), (Double, Double, Double), (Double, Double, Double))): Unit = {
      buffer.pos(p._1._1, p._1._2, p._1._3, matrixStack).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightValue.l1, lightValue.l2).endVertex()
      buffer.pos(p._2._1, p._2._2, p._2._3, matrixStack).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightValue.l1, lightValue.l2).endVertex()
      buffer.pos(p._3._1, p._3._2, p._3._3, matrixStack).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightValue.l1, lightValue.l2).endVertex()
      buffer.pos(p._4._1, p._4._2, p._4._3, matrixStack).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightValue.l1, lightValue.l2).endVertex()
    }

    if (te.getBlockState.getValue(PipeBlock.NORTH).hasConnection) {
      RenderPipe.North_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      drawWhite(
        (maxD, maxD, minD),
        (maxD, minD, minD),
        (minD, minD, minD),
        (minD, maxD, minD))
    }
    if (te.getBlockState.getValue(PipeBlock.SOUTH).hasConnection) {
      RenderPipe.South_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      drawWhite(
        (minD, maxD, maxD),
        (minD, minD, maxD),
        (maxD, minD, maxD),
        (maxD, maxD, maxD))
    }
    if (te.getBlockState.getValue(PipeBlock.WEST).hasConnection) {
      RenderPipe.West_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      drawWhite(
        (minD, maxD, minD),
        (minD, minD, minD),
        (minD, minD, maxD),
        (minD, maxD, maxD))
    }
    if (te.getBlockState.getValue(PipeBlock.EAST).hasConnection) {
      RenderPipe.East_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      drawWhite(
        (maxD, maxD, maxD),
        (maxD, minD, maxD),
        (maxD, minD, minD),
        (maxD, maxD, minD))
    }
    if (te.getBlockState.getValue(PipeBlock.UP).hasConnection) {
      RenderPipe.UP_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      drawWhite(
        (minD, maxD, minD),
        (minD, maxD, maxD),
        (maxD, maxD, maxD),
        (maxD, maxD, minD))
    }
    if (te.getBlockState.getValue(PipeBlock.DOWN).hasConnection) {
      RenderPipe.Down_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      drawWhite(
        (maxD, minD, minD),
        (maxD, minD, maxD),
        (minD, minD, maxD),
        (minD, minD, minD))
    }

    matrixStack.popPose()
    Minecraft.getInstance.getProfiler.pop()
  }
}

object RenderPipe {
  private final val d = 1d / 16d
  final val duration = 200
  val BOX_AABB: Box = Box.apply(
    startX = 8 * d, startY = 4 * d + 0.001, startZ = 8 * d,
    endX = 8 * d, endY = 12 * d - 0.001, endZ = 8 * d,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = true, endSide = true
  )
  val North_AABB: Box = Box.apply(
    startX = 8 * d, startY = 8 * d, startZ = 0.001,
    endX = 8 * d, endY = 8 * d, endZ = 4 * d - 0.001,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
  val South_AABB: Box = Box.apply(
    startX = 8 * d, startY = 8 * d, startZ = 12 * d + 0.001,
    endX = 8 * d, endY = 8 * d, endZ = 16 * d - 0.001,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
  val West_AABB: Box = Box.apply(
    startX = 0.001, startY = 8 * d, startZ = 8 * d,
    endX = 4 * d - 0.001, endY = 8 * d, endZ = 8 * d,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
  val East_AABB: Box = Box.apply(
    startX = 12 * d + 0.001, startY = 8 * d, startZ = 8 * d,
    endX = 16 * d - 0.001, endY = 8 * d, endZ = 8 * d,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
  val UP_AABB: Box = Box.apply(
    startX = 8 * d, startY = 12 * d + 0.001, startZ = 8 * d,
    endX = 8 * d, endY = 16 * d - 0.001, endZ = 8 * d,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
  val Down_AABB: Box = Box.apply(
    startX = 8 * d, startY = 0.001, startZ = 8 * d,
    endX = 8 * d, endY = 4 * d - 0.001, endZ = 8 * d,
    sizeX = d * 8 - 0.001, sizeY = d * 8 - 0.001, sizeZ = d * 8 - 0.001,
    firstSide = false, endSide = false
  )
}
