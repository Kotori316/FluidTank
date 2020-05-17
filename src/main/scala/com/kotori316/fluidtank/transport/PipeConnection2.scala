package com.kotori316.fluidtank.transport

import java.util.Objects

import com.kotori316.scala_lib.util.Neighbor
import com.kotori316.scala_lib.util.Neighbor._

class PipeConnection2[A](val poses: Set[A], isOutput: A => Boolean)(implicit private val neighbor: Neighbor[A]) {

  def outputs(from: A): Seq[A] =
    from.withInDistance(poses.size - 1, poses, includeOrigin = true)
      .filter { case (a, _) => isOutput(a) }
      .toSeq
      .sortBy(_._2)
      .map(_._1)

  def copy(poses: Set[A] = this.poses, isOut: A => Boolean = isOutput): PipeConnection2[A] = {
    if (poses == this.poses && isOut == this.isOutput) this
    else new PipeConnection2(poses, isOut)
  }

  override def toString: String = s"PipeConnection{${poses.size}, $poses}"

  def canEqual(other: Any): Boolean = other.isInstanceOf[PipeConnection2[_]]

  override def equals(other: Any): Boolean = other match {
    case that: PipeConnection2[_] =>
      (that canEqual this) && poses == that.poses && this.neighbor == that.neighbor
    case _ => false
  }

  override def hashCode(): Int = Objects.hash(poses, neighbor)

  def isEmpty: Boolean = poses.isEmpty

  def foreach(function: A => Unit): Unit = poses.foreach(function)
}

object PipeConnection2 {
  def empty[A: Neighbor](isOutput: A => Boolean): PipeConnection2[A] = new PipeConnection2[A](Set.empty, isOutput)

  /**
   * new pos must be next to a pos in the connection.
   */
  def add[A](connection: PipeConnection2[A], a: A): PipeConnection2[A] = {
    if (!connection.isEmpty && !connection.poses.flatMap(p => connection.neighbor.next(p)).contains(a))
      throw new IllegalArgumentException(s"Pos $a is not next to a pos in the connection: $connection.")
    connection.copy(connection.poses + a)
  }

}
