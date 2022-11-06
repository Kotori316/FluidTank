package com.kotori316.fluidtank.integration.mekanism_gas

import cats.Hash
import mekanism.api.NBTConstants
import mekanism.api.chemical.gas.{Gas, GasStack}
import net.minecraft.nbt.CompoundTag

object GasAmount {

  // Gas Stack doesn't have NBT tag
  def apply(gas: Gas, amount: Long): GasAmount = new GasAmount(gas, amount, Option.empty)

  implicit val hashGas: Hash[Gas] = Hash.fromUniversalHashCode

  def fromStack(gasStack: GasStack): GasAmount = apply(gasStack.getRaw, gasStack.getAmount)

  def fromTag(storedTag: CompoundTag): GasAmount = {
    val gas = Gas.readFromNBT(storedTag)
    val amount = storedTag.getLong(NBTConstants.AMOUNT)
    apply(gas, amount)
  }

  final val EMPTY: GasAmount = fromStack(GasStack.EMPTY)
}
