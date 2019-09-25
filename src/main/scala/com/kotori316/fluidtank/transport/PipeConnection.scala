package com.kotori316.fluidtank.transport

import cats._
import cats.implicits._
import cats.kernel.CommutativeGroup
import com.kotori316.scala_lib.util.Norm
import com.kotori316.scala_lib.util.Norm._
import net.minecraft.util.math.{BlockPos, Vec3i}

case class PipeConnection[A](poses: Set[A], updateFunc: (A, PipeConnection[A]) => Unit, isOutput: A => Boolean)
                            (implicit norm: Norm[A], group: Group[A]) {
  poses.foreach(p => updateFunc.apply(p, this))

  def outputs: Seq[A] = poses.filter(isOutput).toSeq

  def outputSorted(origin: A): List[A] = outputs.sortBy(p => (p |-| origin).norm).toList

  def add(a: A): PipeConnection[A] = this.copy(poses = poses + a)

  def remove(a: A): PipeConnection[A] = {
    val empty = PipeConnection.empty(updateFunc, isOutput)
    poses.find(_ == a).foreach(p => updateFunc(p, empty))
    this.copy(poses - a)
  }

  def reset(): Unit = {
    val empty = PipeConnection.empty(updateFunc, isOutput)
    poses.foreach(p => updateFunc.apply(p, empty))
  }

  def isEmpty: Boolean = poses.isEmpty

  override def toString: String = s"PipeConnection(${poses.size}, $poses)"
}

object PipeConnection {
  def empty[A](updateFunc: (A, PipeConnection[A]) => Unit, isOutput: A => Boolean)(implicit norm: Norm[A], group: Group[A]): PipeConnection[A] = PipeConnection(Set.empty, updateFunc, isOutput)

  implicit val vec3iL2Norm: Norm[Vec3i] = v => Math.sqrt(v.getX * v.getX + v.getY * v.getY + v.getZ * v.getZ)
  val vec3iL1Norm: Norm[Vec3i] = v => v.getX + v.getY + v.getZ
  implicit val posGroup: CommutativeGroup[BlockPos] = new CommutativeGroup[BlockPos] {
    override def inverse(a: BlockPos): BlockPos = new BlockPos(-a.getX, -a.getY, -a.getZ)

    override def empty: BlockPos = BlockPos.ZERO

    override def remove(x: BlockPos, y: BlockPos): BlockPos = new BlockPos(x.getX - y.getX, x.getY - y.getY, x.getZ - y.getZ)

    override def combine(x: BlockPos, y: BlockPos): BlockPos = new BlockPos(x.getX + y.getX, x.getY + y.getY, x.getZ + y.getZ)
  }
  implicit val vec3iGroup: CommutativeGroup[Vec3i] = posGroup.imap[Vec3i](identity)(new BlockPos(_))
}
