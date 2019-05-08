package com.kotori316

import cats.Show
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidStack

import scala.collection.AbstractIterator

package object fluidtank {

  implicit class FluidStackHelper(val fluidStack: FluidStack) extends AnyVal {

    def copyWithAmount(amount: Int): FluidStack = {
      if (fluidStack == null)
        null
      else
        new FluidStack(fluidStack, amount)
    }

    def setAmount(amount: Int): FluidStack = {
      fluidStack.amount = amount
      fluidStack
    }

    def isEmpty: Boolean = {
      fluidStack == null || fluidStack.amount <= 0
    }
  }

  implicit class LazyOptional2Stream[T](val opt: LazyOptional[T]) extends AnyVal {
    def asScalaIterator = {
      if (opt.isPresent) {
        new AbstractIterator[T] {
          private var evaluated = false

          override def hasNext = !evaluated

          override def next() = {
            if (hasNext) {
              evaluated = true
              opt.orElseThrow(() => new AssertionError("Lazy Optional doesn't provide value."))
            } else {
              Iterator.empty.next()
            }
          }
        }
      } else {
        Iterator.empty
      }
    }

    def asScala = {
      if (opt.isPresent) {
        Some(opt.orElseThrow(() => new AssertionError("Lazy Optional doesn't provide value.")))
      } else {
        None
      }
    }

    def or(x: => LazyOptional[T]) = {
      if (opt.isPresent) {
        opt
      } else {
        x
      }
    }
  }

  implicit val showPos:Show[BlockPos] = pos => s"(${pos.getX}, ${pos.getY}, ${pos.getZ})"
}
