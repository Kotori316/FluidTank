package com.kotori316.fluidtank.integration

import cats.implicits.catsSyntaxEq
import com.kotori316.fluidtank.fluids.{FluidAmount, GenericAccess, GenericAmount}
import mekanism.api.MekanismAPI
import mekanism.api.chemical.gas.{Gas, GasStack}
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation

import scala.reflect.ClassTag

package object mekanism_gas {
  implicit val GenericAccessGas: GenericAccess[Gas] = GenericAccessGasImpl
  type GasAmount = GenericAmount[Gas]

  implicit class GasAmountExtension(private val amount: GasAmount) extends AnyVal {
    def toStack: GasStack = {
      if (GasAmount.EMPTY === amount) GasStack.EMPTY
      else amount.c.getStack(amount.amount)
    }
  }
}

private object GenericAccessGasImpl extends GenericAccess[Gas] {
  override def isEmpty(a: Gas): Boolean = a.isEmptyType

  override def isGaseous(a: Gas): Boolean = true

  override def getKey(a: Gas): ResourceLocation = a.getRegistryName

  override def empty: Gas = MekanismAPI.EMPTY_GAS

  override def write(amount: GenericAmount[Gas], tag: CompoundTag): CompoundTag = {
    amount.c.write(tag)
    tag.putLong(FluidAmount.NBT_amount, amount.amount)
    amount.nbt.foreach(n => tag.put(FluidAmount.NBT_tag, n))

    tag
  }

  override def classTag: ClassTag[Gas] = implicitly[ClassTag[Gas]]
}
