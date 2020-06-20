package com.kotori316.fluidtank.test

import cats.implicits._
import com.kotori316.fluidtank._
import net.minecraft.fluid.Fluids
import net.minecraft.item.{ItemStack, Items}
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

  @Test
  def empty(): Unit = {
    val tag = Some {
      val a = new CompoundNBT()
      a.putInt("e", 1)
      a.putInt("4", 2)
      a
    }
    assertAll(
      () => assertTrue(FluidAmount.EMPTY.isEmpty),
      () => assertTrue(FluidAmount.EMPTY.copy(nbt = tag).isEmpty),
      () => assertTrue(FluidAmount(Fluids.EMPTY, 10000, None).isEmpty),
      () => assertTrue(FluidAmount(Fluids.EMPTY, 10000, tag).isEmpty),
      () => assertTrue(FluidAmount(Fluids.LAVA, 0, None).isEmpty),
      () => assertTrue(FluidAmount.BUCKET_LAVA.setAmount(0).isEmpty),
      () => assertTrue((FluidAmount.BUCKET_WATER - FluidAmount.BUCKET_WATER).isEmpty),
      () => assertTrue(FluidAmount.fromItem(new ItemStack(Items.BUCKET)).isEmpty),
    )
  }

  @Test
  def adder(): Unit = {
    {
      val a = FluidAmount.BUCKET_WATER
      assertEquals(FluidAmount.AMOUNT_BUCKET, a.amount)
      assertEquals(FluidAmount.AMOUNT_BUCKET * 2, (a + a).amount)
      assertEquals(FluidAmount.AMOUNT_BUCKET * 3, (a + a + a).amount)
      assertEquals(FluidAmount.AMOUNT_BUCKET, a.amount)
    }
    locally {
      val wl = FluidAmount.BUCKET_WATER + FluidAmount.BUCKET_LAVA
      assertTrue(FluidAmount.BUCKET_WATER.setAmount(FluidAmount.AMOUNT_BUCKET * 2) === wl)
      assertEquals(Fluids.WATER, wl.fluid)
    }
    locally {
      val lw = FluidAmount.BUCKET_LAVA + FluidAmount.BUCKET_WATER
      assertTrue(FluidAmount.BUCKET_LAVA.setAmount(FluidAmount.AMOUNT_BUCKET * 2) === lw)
      assertEquals(Fluids.LAVA, lw.fluid)
    }
  }
}
