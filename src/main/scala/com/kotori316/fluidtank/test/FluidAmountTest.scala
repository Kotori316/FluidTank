package com.kotori316.fluidtank.test

import cats.implicits._
import com.kotori316.fluidtank._
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.CompoundNBT
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class FluidAmountTest {
  @Test
  def equiv(): Unit = {
    val a = FluidAmount(Fluids.WATER, 1000, None)
    assertTrue(a === a, "Equal to itself")
    val b = FluidAmount(Fluids.WATER, 1000, None)
    assertTrue(a === b, "Eq")
    assertEquals(a, b, "Eq2")
    assertTrue(a fluidEqual b)
    assertTrue(b fluidEqual a)
  }

  @Test
  def eqNbt(): Unit = {
    val nbt1 = new CompoundNBT()
    nbt1.putBoolean("b", true)
    nbt1.putString("name", "name")
    val nbt2 = new CompoundNBT()
    nbt2.putBoolean("b", false)
    nbt2.putString("name", "tag")
    assertNotEquals(nbt1, nbt2)

    val a = FluidAmount(Fluids.WATER, 1000, Some(nbt1))
    val b = FluidAmount.BUCKET_WATER
    assertTrue(a =!= b, "Not eq due to nbt tag.")
  }
}
