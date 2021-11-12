package com.kotori316.fluidtank

import com.mojang.blaze3d.vertex.{PoseStack, VertexConsumer}
import com.mojang.math.Vector4f

package object render {

  implicit class MatrixHelper(private val mat: PoseStack) extends AnyVal {
    def push(): Unit = {
      mat.pushPose()
    }

    def offset(x: Double, y: Double, z: Double): Unit = {
      mat.translate(x, y, z)
    }

    def scale(x: Float, y: Float, z: Float): Unit = {
      mat.scale(x, y, z)
    }

    def scale(f: Float): Unit = {
      mat.scale(f, f, f)
    }

    def pop(): Unit = {
      mat.popPose()
    }
  }

  private[this] final val vector4f = new Vector4f()

  private def getPosVector(x: Float, y: Float, z: Float, matrix: PoseStack): Vector4f = {
    val matrix4f = matrix.last().pose()

    vector4f.set(x, y, z, 1.0F)
    vector4f.transform(matrix4f)
    vector4f
  }

  class Wrapper(val buffer: VertexConsumer) extends AnyVal {
    def pos(x: Double, y: Double, z: Double, matrix: PoseStack): Wrapper = {
      val vector4f = getPosVector(x.toFloat, y.toFloat, z.toFloat, matrix)
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
    def lightmap(l1: Int, l2: Int): Wrapper = {
      buffer.overlayCoords(10, 10).uv2(l1, l2)
      this
    }

    def endVertex(): Unit = {
      buffer.normal(0, 1, 0).endVertex()
    }
  }

}
