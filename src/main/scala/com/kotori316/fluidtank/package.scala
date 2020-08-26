package com.kotori316

import cats._
import cats.data._
import com.kotori316.scala_lib.util.{CapConverter, Neighbor}
import com.mojang.serialization.DataResult
import net.minecraft.fluid.Fluid
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fluids.FluidStack

package object fluidtank extends CapConverter {

  implicit val showPos: Show[BlockPos] = pos => s"(${pos.getX}, ${pos.getY}, ${pos.getZ})"
  implicit val eqPos: Eq[BlockPos] = Eq.fromUniversalEquals
  implicit val hashFluid: Hash[Fluid] = Hash.fromUniversalHashCode
  implicit val eqCompoundNbt: Eq[CompoundNBT] = Eq.fromUniversalEquals
  implicit val hashFluidStack: Hash[FluidStack] = new Hash[FluidStack] {
    override def hash(x: FluidStack): Int = x.##

    override def eqv(x: FluidStack, y: FluidStack): Boolean = x isFluidStackIdentical y
  }

  val directions: List[Direction] = Direction.values().toList
  val evalExtractor: Eval ~> Id = new (Eval ~> Id) {
    override def apply[A](fa: Eval[A]): A = fa.value
  }

  implicit final val NeighborOfBlockPos: Neighbor[BlockPos] = (origin: BlockPos) => Set(
    origin.up, origin.down, origin.north, origin.east, origin.south, origin.west
  )

  implicit class EitherToResult[A](private val either: Either[String, A]) extends AnyVal {
    def toResult: DataResult[A] = either match {
      case Left(value) => DataResult.error(value)
      case Right(value) => DataResult.success(value)
    }
  }

  implicit class IorToResult[A](private val ior: Ior[String, A]) extends AnyVal {
    def toResult: DataResult[A] = ior match {
      case Ior.Left(a) => DataResult.error(a)
      case Ior.Right(b) => DataResult.success(b)
      case Ior.Both(a, b) => DataResult.error(a, b)
    }
  }

}
