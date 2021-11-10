package com.kotori316.fluidtank.render
/*
import java.awt.Color

import com.kotori316.fluidtank.network.ClientProxy
import com.kotori316.fluidtank.transport.{PipeBlock, PipeTile}
import com.mojang.blaze3d.matrix.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.{TileEntityRenderer, TileEntityRendererDispatcher}
import net.minecraft.client.renderer.{IRenderTypeBuffer, RenderType}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

@OnlyIn(Dist.CLIENT)
class RenderPipe(d: TileEntityRendererDispatcher) extends TileEntityRenderer[PipeTile](d) {

  override def func_225616_a_(te: PipeTile, partialTicks: Float, matrixStack: PoseStack, renderTypeBuffer: IRenderTypeBuffer, light: Int, otherLight: Int): Unit = {

    val maxD = 12 * RenderPipe.d - 0.01
    val minD = 4 * RenderPipe.d + 0.01
    Minecraft.getInstance.getProfiler.startSection("RenderPipe")
    matrixStack.push()

    val texture = ClientProxy.whiteTexture
    val minU = texture.getU0
    val minV = texture.getV0
    val maxU = texture.getU1
    val maxV = texture.getV1
    val light = implicitly[Box.LightValue]
    val buffer = new Wrapper(renderTypeBuffer.getBuffer(RenderType.func_228643_e_()))
    val time = te.getLevel.getGameTime
    val color = Color.HSBtoRGB((time % RenderPipe.duration).toFloat / RenderPipe.duration, 1f, 1f)
    val red = color >> 16 & 0xFF
    val green = color >> 8 & 0xFF
    val blue = color >> 0 & 0xFF
    val alpha = 240
    //    RenderPipe.BOX_AABB.render(buffer, texture, 128, red, green, blue)
    if (te.getBlockState.get(PipeBlock.NORTH).hasConnection) {
      RenderPipe.North_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      buffer.pos(maxD, maxD, minD, matrixStack).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, minD, minD, matrixStack).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, minD, matrixStack).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, maxD, minD, matrixStack).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }
    if (te.getBlockState.get(PipeBlock.SOUTH).hasConnection) {
      RenderPipe.South_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      buffer.pos(minD, maxD, maxD, matrixStack).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, maxD, matrixStack).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, minD, maxD, matrixStack).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, maxD, maxD, matrixStack).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }
    if (te.getBlockState.get(PipeBlock.WEST).hasConnection) {
      RenderPipe.West_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      buffer.pos(minD, maxD, minD, matrixStack).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, minD, matrixStack).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, maxD, matrixStack).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, maxD, maxD, matrixStack).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }
    if (te.getBlockState.get(PipeBlock.EAST).hasConnection) {
      RenderPipe.East_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      buffer.pos(maxD, maxD, maxD, matrixStack).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, minD, maxD, matrixStack).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, minD, minD, matrixStack).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, maxD, minD, matrixStack).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }
    if (te.getBlockState.get(PipeBlock.UP).hasConnection) {
      RenderPipe.UP_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      buffer.pos(minD, maxD, minD, matrixStack).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, maxD, maxD, matrixStack).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, maxD, maxD, matrixStack).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, maxD, minD, matrixStack).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }
    if (te.getBlockState.get(PipeBlock.DOWN).hasConnection) {
      RenderPipe.Down_AABB.render(buffer.buffer, matrixStack, texture, alpha, red, green, blue)
    } else {
      buffer.pos(maxD, minD, minD, matrixStack).color(red, green, blue, alpha).tex(minU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(maxD, minD, maxD, matrixStack).color(red, green, blue, alpha).tex(maxU, minV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, maxD, matrixStack).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(light.l1, light.l2).endVertex()
      buffer.pos(minD, minD, minD, matrixStack).color(red, green, blue, alpha).tex(minU, maxV).lightmap(light.l1, light.l2).endVertex()
    }

    matrixStack.pop()
    Minecraft.getInstance.getProfiler.endSection()
  }
}

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
*/