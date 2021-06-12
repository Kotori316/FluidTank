package com.kotori316.fluidtank

import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.{Direction, Vec3f, Vector4f}

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

  private[this] final val vector3f = new Vec3f()
  private[this] final val vector4f = new Vector4f()

  private def getPosVector(x: Float, y: Float, z: Float, matrix: MatrixStack): Vector4f = {
    val vec3i = Direction.UP.getVector
    vector3f.set(vec3i.getX.toFloat, vec3i.getY.toFloat, vec3i.getZ.toFloat)
    val matrix4f = matrix.peek().getModel
    vector3f.transform(matrix.peek().getNormal)

    vector4f.set(x, y, z, 1.0F)
    vector4f.transform(matrix4f)
    vector4f
  }

  class Wrapper(val buffer: VertexConsumer) extends AnyVal {
    def pos(x: Double, y: Double, z: Double, matrix: MatrixStack): Wrapper = {
      val vector4f = getPosVector(x.toFloat, y.toFloat, z.toFloat, matrix)
      buffer.vertex(vector4f.getX, vector4f.getY, vector4f.getZ)
      this
    }

    def color(red: Int, green: Int, blue: Int, alpha: Int): Wrapper = {
      buffer.color(red, green, blue, alpha)
      this
    }

    def tex(u: Float, v: Float): Wrapper = {
      buffer.texture(u, v)
      this
    }

    //noinspection SpellCheckingInspection
    def lightmap(l1: Int, l2: Int): Wrapper = {
      buffer.overlay(10, 10).light(l1, l2)
      this
    }

    def endVertex(): Unit = {
      buffer.normal(0, 1, 0).next()
    }
  }

}
