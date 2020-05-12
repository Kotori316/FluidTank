package com.kotori316.fluidtank.transport

case class PipeConnection[A](poses: Set[A], updateFunc: (A, PipeConnection[A]) => Unit, isOutput: A => Boolean) {
  poses.foreach(p => updateFunc.apply(p, this))

  def outputs: Seq[A] = poses.filter(isOutput).toSeq

  def add(a: A): PipeConnection[A] = this.copy(poses = poses + a)

  def reset(): Unit = {
    val empty = PipeConnection.empty(updateFunc, isOutput)
    poses.foreach(p => updateFunc.apply(p, empty))
  }

  def isEmpty: Boolean = poses.isEmpty

  override def toString: String = s"PipeConnection(${poses.size}, $poses)"
}

object PipeConnection {
  def empty[A](updateFunc: (A, PipeConnection[A]) => Unit, isOutput: A => Boolean): PipeConnection[A] = new EmptyConnection(updateFunc, isOutput)

  class EmptyConnection[A](updateFunc: (A, PipeConnection[A]) => Unit, isOutput: A => Boolean)
    extends PipeConnection[A](Set.empty, updateFunc, isOutput) {
    override def toString = "EmptyConnection"
  }
}
