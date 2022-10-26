package com.kotori316.fluidtank.fluids

import cats.implicits.catsSyntaxEq
import com.kotori316.fluidtank._
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.material.{Fluid, Fluids}
import net.minecraftforge.registries.ForgeRegistries

import scala.reflect.ClassTag

trait GenericAccess[A] {

  def isEmpty(a: A): Boolean

  def isGaseous(a: A): Boolean

  def getKey(a: A): ResourceLocation

  def empty: A

  def write(amount: GenericAmount[A], tag: CompoundTag): CompoundTag

  def classTag: ClassTag[A]
}

object GenericAccess {
  private object FluidAccess extends GenericAccess[Fluid] {

    override def isEmpty(fluid: Fluid): Boolean = fluid === Fluids.EMPTY

    override def isGaseous(fluid: Fluid): Boolean = fluid.getAttributes.isGaseous

    override def getKey(fluid: Fluid): ResourceLocation = ForgeRegistries.FLUIDS.getKey(fluid)

    override def empty: Fluid = Fluids.EMPTY

    override def write(amount: GenericAmount[Fluid], tag: CompoundTag): CompoundTag = {
      import com.kotori316.fluidtank.fluids.FluidAmount._

      val fluidNBT = new CompoundTag()
      fluidNBT.putString(NBT_fluid, FluidAmount.registry.getKey(amount.c).toString)
      fluidNBT.putLong(NBT_amount, amount.amount)
      amount.nbt.foreach(fluidNBT.put(NBT_tag, _))

      tag merge fluidNBT
    }

    override def classTag: ClassTag[Fluid] = implicitly[ClassTag[Fluid]]
  }

  implicit val fluidAccess: GenericAccess[Fluid] = FluidAccess
}
