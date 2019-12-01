package com.kotori316

import cats._
import cats.data._
import cats.implicits._
import net.minecraft.fluid.Fluid
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.{LazyOptional, NonNullSupplier}
import net.minecraftforge.fluids.FluidStack


package object fluidtank {

  type Cap[T] = OptionT[Eval, T]

  object Cap {
    def make[T](obj: T): Cap[T] = {
      OptionT.fromOption[Eval](Option(obj))
    }

    def asJava[A](cap: Cap[A]): LazyOptional[A] = {
      cap.value.value.foldl(LazyOptional.empty[A]()) { case (_, a) => LazyOptional.of[A](() => a) }
    }

    def empty[A]: Cap[A] = {
      OptionT.none
    }
  }

  implicit class CapHelper[T](val capability: Capability[T]) extends AnyVal {
    /**
     * @tparam F dummy parameter to satisfy compiler. It should be type parameter of {{{ICapabilityProvider#getCapability}}}.
     */
    def make[F](toCheckCapability: Capability[_], instance: T): Cap[F] = {
      if (this.capability == toCheckCapability)
        Cap.make[F](instance.asInstanceOf[F])
      else
        Cap.empty[F]
    }
  }

  def transform0[T](cap: LazyOptional[T]) = Eval.always {
    if (cap.isPresent) {
      cap.orElseThrow(thrower).some
    } else {
      None
    }
  }

  implicit class AsScalaLO[T](val cap: LazyOptional[T]) extends AnyVal {
    def asScala: Cap[T] = OptionT(transform0[T](cap))
  }

  private val thrower: NonNullSupplier[AssertionError] = () =>
    new AssertionError(
      "LazyOptional has no content " +
        "though it returned true when isPresent is called.")

  implicit val showPos: Show[BlockPos] = pos => s"(${pos.getX}, ${pos.getY}, ${pos.getZ})"
  implicit val eqPos: Eq[BlockPos] = Eq.fromUniversalEquals
  implicit val hashFluid: Hash[Fluid] = Hash.fromUniversalHashCode
  implicit val eqCompoundNbt: Eq[CompoundNBT] = Eq.fromUniversalEquals
  implicit val hashFluidStack: Hash[FluidStack] = new Hash[FluidStack] {
    override def hash(x: FluidStack): Int = x.##

    override def eqv(x: FluidStack, y: FluidStack): Boolean = x isFluidStackIdentical y
  }
}
