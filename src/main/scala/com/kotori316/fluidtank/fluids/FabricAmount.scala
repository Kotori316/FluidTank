package com.kotori316.fluidtank.fluids

import cats.implicits._
import cats.{Group, Hash, Order}

case class FabricAmount(amount: Long) extends AnyVal {
  def toForge: Long = VariantUtil.convertFabricAmountToForge(amount)

  def +(that: FabricAmount): FabricAmount = FabricAmount(this.amount + that.amount)

  def -(that: FabricAmount): FabricAmount = FabricAmount(this.amount - that.amount)
}

object FabricAmount {
  val BUCKET: FabricAmount = FabricAmount(VariantUtil.convertForgeAmountToFabric(FluidAmount.AMOUNT_BUCKET))

  def fromForge(forgeAmount: Long): FabricAmount = FabricAmount(VariantUtil.convertForgeAmountToFabric(forgeAmount))

  implicit val typeInstance: Hash[FabricAmount] with Group[FabricAmount] with Order[FabricAmount] =
    new Hash[FabricAmount] with Group[FabricAmount] with Order[FabricAmount] {
      override def hash(x: FabricAmount): Int = x.##

      override def inverse(a: FabricAmount): FabricAmount = FabricAmount(-a.amount)

      override val empty: FabricAmount = FabricAmount(0L)

      override def eqv(x: FabricAmount, y: FabricAmount): Boolean = x.amount === y.amount

      override def combine(x: FabricAmount, y: FabricAmount): FabricAmount = x + y

      override def remove(a: FabricAmount, b: FabricAmount): FabricAmount = a - b

      override def compare(x: FabricAmount, y: FabricAmount): Int = x.amount compare y.amount
    }
}
