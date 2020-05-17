package com.kotori316.fluidtank

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.{Vector3f, Vector4f}
import net.minecraft.util.Direction

package object render {

  implicit class MatrixHelper(private val mat: MatrixStack) extends AnyVal {
    def push(): Unit = {
      mat.push()
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
      mat.pop()
    }
  }

  private[this] final val vector3f = new Vector3f()
  private[this] final val vector4f = new Vector4f()

  private def getPosVector(x: Float, y: Float, z: Float, matrix: MatrixStack): Vector4f = {
    val vec3i = Direction.UP.getDirectionVec
    vector3f.set(vec3i.getX.toFloat, vec3i.getY.toFloat, vec3i.getZ.toFloat)
    val matrix4f = matrix.getLast.getMatrix
    vector3f.transform(matrix.getLast.getNormal)

    vector4f.set(x, y, z, 1.0F)
    vector4f.transform(matrix4f)
    vector4f
  }

  class Wrapper(val buffer: IVertexBuilder) extends AnyVal {
    def pos(x: Double, y: Double, z: Double, matrix: MatrixStack): Wrapper = {
      val vector4f = getPosVector(x.toFloat, y.toFloat, z.toFloat, matrix)
      buffer.pos(vector4f.getX, vector4f.getY, vector4f.getZ)
      this
    }

    def color(red: Int, green: Int, blue: Int, alpha: Int): Wrapper = {
      buffer.color(red, green, blue, alpha)
      this
    }

    def tex(u: Float, v: Float): Wrapper = {
      buffer.tex(u, v)
      this
    }

    //noinspection SpellCheckingInspection
    def lightmap(sky: Int, block: Int): Wrapper = {
      buffer.overlay(10, 10).lightmap(block, sky)
      this
    }

    def endVertex(): Unit = {
      buffer.normal(0, 1, 0).endVertex()
    }
  }

}
