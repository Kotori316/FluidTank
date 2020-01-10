package com.kotori316.fluidtank

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.renderer.{Vector3f, Vector4f}
import net.minecraft.util.Direction

package object render {

  implicit class MatrixHelper(private val mat: MatrixStack) extends AnyVal {
    def push(): Unit = {
      mat.func_227860_a_()
    }

    def offset(x: Double, y: Double, z: Double): Unit = {
      mat.func_227861_a_(x, y, z)
    }

    def scale(x: Float, y: Float, z: Float): Unit = {
      mat.func_227862_a_(x, y, z)
    }

    def scale(f: Float): Unit = {
      mat.func_227862_a_(f, f, f)
    }

    def pop(): Unit = {
      mat.func_227865_b_()
    }
  }

  private[this] final val vector3f = new Vector3f()
  private[this] final val vector4f = new Vector4f()

  private def getPosVector(x: Float, y: Float, z: Float, matrix: MatrixStack): Vector4f = {
    val vec3i = Direction.UP.getDirectionVec
    vector3f.set(vec3i.getX, vec3i.getY, vec3i.getZ)
    val matrix4f = matrix.func_227866_c_.func_227870_a_
    vector3f.func_229188_a_(matrix.func_227866_c_.func_227872_b_)

    vector4f.set(x, y, z, 1.0F)
    vector4f.func_229372_a_(matrix4f)
    vector4f
  }

  class Wrapper(val buffer: IVertexBuilder) extends AnyVal {
    def pos(x: Double, y: Double, z: Double, matrix: MatrixStack): Wrapper = {
      val vector4f = getPosVector(x.toFloat, y.toFloat, z.toFloat, matrix)
      buffer.func_225582_a_(vector4f.getX, vector4f.getY, vector4f.getZ)
      this
    }

    def color(red: Int, green: Int, blue: Int, alpha: Int): Wrapper = {
      buffer.func_225586_a_(red, green, blue, alpha)
      this
    }

    def tex(u: Float, v: Float): Wrapper = {
      buffer.func_225583_a_(u, v)
      this
    }

    //noinspection SpellCheckingInspection
    def lightmap(l1: Int, l2: Int): Wrapper = {
      buffer.func_225585_a_(10, 10).func_225587_b_(l1, l2)
      this
    }

    def endVertex(): Unit = {
      buffer.func_225584_a_(0, 1, 0).endVertex()
    }
  }

}
