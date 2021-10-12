package com.kotori316

import java.lang.reflect.Field

import cats._
import cats.data._
import com.kotori316.fluidtank.tiles.Tier
import com.mojang.serialization.DataResult
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack

import scala.jdk.OptionConverters._
import scala.util.chaining._

package object fluidtank extends CapConverter {

  implicit final val showPos: Show[BlockPos] = pos => s"(${pos.getX}, ${pos.getY}, ${pos.getZ})"
  implicit final val eqPos: Eq[BlockPos] = Eq.fromUniversalEquals
  implicit final val hashFluid: Hash[Fluid] = Hash.fromUniversalHashCode
  implicit final val hashTier: Hash[Tier] = Hash.fromUniversalHashCode
  implicit final val eqCompoundNbt: Eq[CompoundTag] = Eq.fromUniversalEquals
  implicit final val hashFluidStack: Hash[FluidStack] = new Hash[FluidStack] {
    override def hash(x: FluidStack): Int = x.##

    override def eqv(x: FluidStack, y: FluidStack): Boolean = x isFluidStackIdentical y
  }
  private final val FluidStackAmountField: Field = classOf[FluidStack].getDeclaredField("amount").tap(_.setAccessible(true))
  implicit final val showFluidStack: Show[FluidStack] = Show.show { stack =>
    val amount = FluidStackAmountField.getInt(stack)
    if (stack.hasTag) s"${stack.getTranslationKey}@$amount(${stack.getTag})"
    else s"${stack.getTranslationKey}@$amount"
  }

  final val directions: List[Direction] = Direction.values().toList
  final val evalExtractor: Eval ~> Id = new (Eval ~> Id) {
    override def apply[A](fa: Eval[A]): A = fa.value
  }
}
