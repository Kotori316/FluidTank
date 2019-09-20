package com.kotori316.fluidtank.transport

import com.kotori316.scala_lib.util.Norm
import net.minecraft.util.math.Vec3i

case class PipeConnection[A: Norm](poses: Set[A], updateFunc: (A, PipeConnection[A]) => Unit) {
  poses.foreach(p => updateFunc.apply(p, this))

  def add(a: A) = PipeConnection(poses + a, updateFunc)

  def remove(a: A): Unit = {
    val empty = PipeConnection.empty(updateFunc)
    poses.foreach(p => updateFunc.apply(p, empty))
  }

  def isEmpty = poses.isEmpty
}

object PipeConnection {
  def empty[A: Norm](updateFunc: (A, PipeConnection[A]) => Unit): PipeConnection[A] = PipeConnection(Set.empty, updateFunc)

  implicit val vec3iL2Norm: Norm[Vec3i] = v => Math.sqrt(v.getX * v.getX + v.getY * v.getY + v.getZ * v.getZ)
  val vec3iL1Norm: Norm[Vec3i] = v => v.getX + v.getY + v.getZ
}
