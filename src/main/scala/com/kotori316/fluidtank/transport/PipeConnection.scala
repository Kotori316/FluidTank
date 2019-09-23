package com.kotori316.fluidtank.transport

import com.kotori316.scala_lib.util.Norm
import net.minecraft.util.math.Vec3i

case class PipeConnection[A: Norm](poses: Set[A], updateFunc: (A, PipeConnection[A]) => Unit, isOutput: A => Boolean) {
  poses.foreach(p => updateFunc.apply(p, this))

  def outputs: Seq[A] = poses.filter(isOutput).toSeq

  def add(a: A): PipeConnection[A] = this.copy(poses = poses + a)

  def remove(a: A): Unit = {
    val empty = PipeConnection.empty(updateFunc, isOutput)
    poses.foreach(p => updateFunc.apply(p, empty))
  }

  def isEmpty: Boolean = poses.isEmpty

  override def toString: String = s"PipeConnection(${poses.size}, $poses)"
}

object PipeConnection {
  def empty[A: Norm](updateFunc: (A, PipeConnection[A]) => Unit, isOutput: A => Boolean): PipeConnection[A] = PipeConnection(Set.empty, updateFunc, isOutput)

  implicit val vec3iL2Norm: Norm[Vec3i] = v => Math.sqrt(v.getX * v.getX + v.getY * v.getY + v.getZ * v.getZ)
  val vec3iL1Norm: Norm[Vec3i] = v => v.getX + v.getY + v.getZ
}
