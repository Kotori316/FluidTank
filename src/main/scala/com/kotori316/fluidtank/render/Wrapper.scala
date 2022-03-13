package com.kotori316.fluidtank.render

import com.mojang.blaze3d.vertex.{PoseStack, VertexConsumer}
import com.mojang.math.Vector4f

case class Wrapper(buffer: VertexConsumer) {
  def pos(x: Double, y: Double, z: Double, matrix: PoseStack): Wrapper = {
    val vector4f = Wrapper.getPosVector(x.toFloat, y.toFloat, z.toFloat, matrix)
    buffer.vertex(vector4f.x, vector4f.y, vector4f.z)
    this
  }

  def color(red: Int, green: Int, blue: Int, alpha: Int): Wrapper = {
    buffer.color(red, green, blue, alpha)
    this
  }

  def tex(u: Float, v: Float): Wrapper = {
    buffer.uv(u, v)
    this
  }

  //noinspection SpellCheckingInspection
  def lightmap(sky: Int, block: Int): Wrapper = {
    buffer.overlayCoords(10, 10).uv2(block, sky)
    this
  }

  def lightMap(light: Int, overlay: Int): Wrapper = {
    buffer.overlayCoords(overlay).uv2(light)
    this
  }

  def endVertex(): Unit = {
    buffer.normal(0, 1, 0).endVertex()
  }
}

object Wrapper {
  private final val vector4f = new Vector4f

  private final def getPosVector(x: Float, y: Float, z: Float, matrix: PoseStack): Vector4f = {
    val matrix4f = matrix.last.pose
    vector4f.set(x, y, z, 1.0F)
    vector4f.transform(matrix4f)
    vector4f
  }
}
