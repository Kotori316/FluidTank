package com.kotori316

import cats._
import com.kotori316.fluidtank.tiles.Tier
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.Fluid

package object fluidtank {

  implicit final val showPos: Show[BlockPos] = pos => s"(${pos.getX}, ${pos.getY}, ${pos.getZ})"
  implicit final val eqPos: Eq[BlockPos] = Eq.fromUniversalEquals
  implicit final val hashFluid: Hash[Fluid] = Hash.fromUniversalHashCode
  implicit final val hashTier: Hash[Tier] = Hash.fromUniversalHashCode
  implicit final val eqCompoundNbt: Eq[CompoundTag] = Eq.fromUniversalEquals

  final val directions: List[Direction] = Direction.values().toList
  final val evalExtractor: Eval ~> Id = new (Eval ~> Id) {
    override def apply[A](fa: Eval[A]): A = fa.value
  }

  implicit class BlockPosHelper(private val pos: BlockPos) extends AnyVal {
    /**
     * Performs the same operation as `offset` in old mapping, now `relative`.
     * This method can be used to avoid "ambiguous reference to overloaded definition".
     *
     * @param direction the direction.
     * @return the pos moved 1 to the direction.
     */
    def offset(direction: Direction): BlockPos = pos relative direction
  }
}
